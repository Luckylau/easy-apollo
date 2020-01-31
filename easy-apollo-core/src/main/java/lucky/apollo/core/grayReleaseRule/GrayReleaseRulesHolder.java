package lucky.apollo.core.grayReleaseRule;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.constant.NamespaceBranchStatus;
import lucky.apollo.common.entity.dto.GrayReleaseRuleItemDTO;
import lucky.apollo.common.utils.ApolloThreadFactory;
import lucky.apollo.core.config.ServiceConfig;
import lucky.apollo.core.entity.GrayReleaseRulePO;
import lucky.apollo.core.entity.ReleaseMessagePO;
import lucky.apollo.core.message.MessageTopic;
import lucky.apollo.core.message.ReleaseMessageListener;
import lucky.apollo.core.repository.GrayReleaseRuleRepository;
import lucky.apollo.core.utils.GrayReleaseRuleItemTransformer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author luckylau
 * @Date 2019/12/4
 */
@Slf4j
@Service
public class GrayReleaseRulesHolder implements ReleaseMessageListener, InitializingBean {

    private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
    private static final Splitter STRING_SPLITTER =
            Splitter.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR).omitEmptyStrings();

    /**
     * an auto increment version to indicate the age of rules
     */
    private AtomicLong loadVersion;

    private ScheduledExecutorService executorService;

    @Autowired
    private GrayReleaseRuleRepository grayReleaseRuleRepository;

    private int databaseScanInterval;


    /**
     * store configAppId+configCluster+configNamespace -> GrayReleaseRuleCache map
     */
    private Multimap<String, GrayReleaseRuleCache> grayReleaseRuleCache;

    /**
     * store clientAppId+clientNamespace+ip -> ruleId map
     */
    private Multimap<String, Long> reversedGrayReleaseRuleCache;

    @Autowired
    private ServiceConfig serviceConfig;


    public GrayReleaseRulesHolder() {
        loadVersion = new AtomicLong();
        grayReleaseRuleCache = Multimaps.synchronizedSetMultimap(HashMultimap.create());
        reversedGrayReleaseRuleCache = Multimaps.synchronizedSetMultimap(HashMultimap.create());
        executorService = new ScheduledThreadPoolExecutor(1, ApolloThreadFactory
                .create("GrayReleaseRulesHolder", true));
    }

    /**
     * Check whether there are gray release rules for the clientAppId, clientIp, namespace
     * combination. Please note that even there are gray release rules, it doesn't mean it will always
     * load gray releases. Because gray release rules actually apply to one more dimension - cluster.
     */
    public boolean hasGrayReleaseRule(String clientAppId, String clientIp, String namespaceName) {
        return reversedGrayReleaseRuleCache.containsKey(assembleReversedGrayReleaseRuleKey(clientAppId,
                namespaceName, clientIp)) || reversedGrayReleaseRuleCache.containsKey
                (assembleReversedGrayReleaseRuleKey(clientAppId, namespaceName, GrayReleaseRuleItemDTO
                        .ALL_IP));
    }

    @Override
    public void handleMessage(ReleaseMessagePO message, String channel) {
        log.info("message received - channel: {}, message: {}", channel, message);
        String releaseMessage = message.getMessage();
        if (!MessageTopic.APOLLO_RELEASE_TOPIC.equals(channel) || Strings.isNullOrEmpty(releaseMessage)) {
            return;
        }
        List<String> keys = STRING_SPLITTER.splitToList(releaseMessage);
        //message should be appId+cluster+namespace
        if (keys.size() != 3) {
            log.error("message format invalid - {}", releaseMessage);
            return;
        }
        String appId = keys.get(0);
        String cluster = keys.get(1);
        String namespace = keys.get(2);

        List<GrayReleaseRulePO> rules = grayReleaseRuleRepository
                .findByAppIdAndClusterNameAndNamespaceName(appId, cluster, namespace);

        mergeGrayReleaseRules(rules);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        populateDataBaseInterval();
        //force sync load for the first time
        periodicScanRules();
        executorService.scheduleWithFixedDelay(this::periodicScanRules,
                getDatabaseScanIntervalSecond(), getDatabaseScanIntervalSecond(), getDatabaseScanTimeUnit()
        );
    }

    private TimeUnit getDatabaseScanTimeUnit() {
        return TimeUnit.SECONDS;
    }

    private int getDatabaseScanIntervalSecond() {
        return databaseScanInterval;
    }

    private void periodicScanRules() {
        try {
            loadVersion.incrementAndGet();
            scanGrayReleaseRules();
        } catch (Throwable ex) {
            log.error("Scan gray release rule failed", ex);
        }
    }

    private void scanGrayReleaseRules() {
        long maxIdScanned = 0;
        boolean hasMore = true;

        while (hasMore && !Thread.currentThread().isInterrupted()) {
            List<GrayReleaseRulePO> grayReleaseRules = grayReleaseRuleRepository
                    .findFirst500ByIdGreaterThanOrderByIdAsc(maxIdScanned);
            if (CollectionUtils.isEmpty(grayReleaseRules)) {
                break;
            }
            mergeGrayReleaseRules(grayReleaseRules);
            int rulesScanned = grayReleaseRules.size();
            maxIdScanned = grayReleaseRules.get(rulesScanned - 1).getId();
            //batch is 500
            hasMore = rulesScanned == 500;
        }
    }

    private void mergeGrayReleaseRules(List<GrayReleaseRulePO> grayReleaseRules) {
        if (CollectionUtils.isEmpty(grayReleaseRules)) {
            return;
        }
        for (GrayReleaseRulePO grayReleaseRule : grayReleaseRules) {
            if (grayReleaseRule.getReleaseId() == null || grayReleaseRule.getReleaseId() == 0) {
                //filter rules with no release id, i.e. never released
                continue;
            }
            String key = assembleGrayReleaseRuleKey(grayReleaseRule.getAppId(), grayReleaseRule
                    .getClusterName(), grayReleaseRule.getNamespaceName());
            //create a new list to avoid ConcurrentModificationException
            List<GrayReleaseRuleCache> rules = Lists.newArrayList(grayReleaseRuleCache.get(key));
            GrayReleaseRuleCache oldRule = null;
            for (GrayReleaseRuleCache ruleCache : rules) {
                if (ruleCache.getBranchName().equals(grayReleaseRule.getBranchName())) {
                    oldRule = ruleCache;
                    break;
                }
            }

            //if old rule is null and new rule's branch status is not active, ignore
            if (oldRule == null && grayReleaseRule.getBranchStatus() != NamespaceBranchStatus.ACTIVE) {
                continue;
            }

            //use id comparison to avoid synchronization
            if (oldRule == null || grayReleaseRule.getId() > oldRule.getRuleId()) {
                addCache(key, transformRuleToRuleCache(grayReleaseRule));
                if (oldRule != null) {
                    removeCache(key, oldRule);
                }
            } else {
                if (oldRule.getBranchStatus() == NamespaceBranchStatus.ACTIVE) {
                    //update load version
                    oldRule.setLoadVersion(loadVersion.get());
                } else if ((loadVersion.get() - oldRule.getLoadVersion()) > 1) {
                    //remove outdated inactive branch rule after 2 update cycles
                    removeCache(key, oldRule);
                }
            }
        }
    }

    private void removeCache(String key, GrayReleaseRuleCache ruleCache) {
        grayReleaseRuleCache.remove(key, ruleCache);
        for (GrayReleaseRuleItemDTO ruleItemDTO : ruleCache.getRuleItems()) {
            for (String clientIp : ruleItemDTO.getClientIpList()) {
                reversedGrayReleaseRuleCache.remove(assembleReversedGrayReleaseRuleKey(ruleItemDTO
                        .getClientAppId(), ruleCache.getNamespaceName(), clientIp), ruleCache.getRuleId());
            }
        }
    }

    private GrayReleaseRuleCache transformRuleToRuleCache(GrayReleaseRulePO grayReleaseRule) {
        Set<GrayReleaseRuleItemDTO> ruleItems;
        try {
            ruleItems = GrayReleaseRuleItemTransformer.batchTransformFromJSON(grayReleaseRule.getRules());
        } catch (Throwable ex) {
            ruleItems = Sets.newHashSet();
            log.error("parse rule for gray release rule {} failed", grayReleaseRule.getId(), ex);
        }

        return new GrayReleaseRuleCache(grayReleaseRule.getId(),
                grayReleaseRule.getBranchName(), grayReleaseRule.getNamespaceName(), grayReleaseRule
                .getReleaseId(), grayReleaseRule.getBranchStatus(), loadVersion.get(), ruleItems);
    }

    private void addCache(String key, GrayReleaseRuleCache ruleCache) {
        if (ruleCache.getBranchStatus() == NamespaceBranchStatus.ACTIVE) {
            for (GrayReleaseRuleItemDTO ruleItemDTO : ruleCache.getRuleItems()) {
                for (String clientIp : ruleItemDTO.getClientIpList()) {
                    reversedGrayReleaseRuleCache.put(assembleReversedGrayReleaseRuleKey(ruleItemDTO
                            .getClientAppId(), ruleCache.getNamespaceName(), clientIp), ruleCache.getRuleId());
                }
            }
        }
        grayReleaseRuleCache.put(key, ruleCache);
    }

    private String assembleReversedGrayReleaseRuleKey(String clientAppId, String
            clientNamespaceName, String clientIp) {
        return STRING_JOINER.join(clientAppId, clientNamespaceName, clientIp);
    }

    private void populateDataBaseInterval() {
        databaseScanInterval = serviceConfig.grayReleaseRuleScanInterval();
    }

    public Long findReleaseIdFromGrayReleaseRule(String clientAppId, String clientIp, String
            configAppId, String configCluster, String configNamespaceName) {
        String key = assembleGrayReleaseRuleKey(configAppId, configCluster, configNamespaceName);
        if (!grayReleaseRuleCache.containsKey(key)) {
            return null;
        }
        //create a new list to avoid ConcurrentModificationException
        List<GrayReleaseRuleCache> rules = Lists.newArrayList(grayReleaseRuleCache.get(key));
        for (GrayReleaseRuleCache rule : rules) {
            //check branch status
            if (rule.getBranchStatus() != NamespaceBranchStatus.ACTIVE) {
                continue;
            }
            if (rule.matches(clientAppId, clientIp)) {
                return rule.getReleaseId();
            }
        }
        return null;
    }

    private String assembleGrayReleaseRuleKey(String configAppId, String configCluster, String
            configNamespaceName) {
        return STRING_JOINER.join(configAppId, configCluster, configNamespaceName);
    }
}
