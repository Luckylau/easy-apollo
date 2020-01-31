package lucky.apollo.configservice.controller;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.entity.dto.ApolloConfigDTO;
import lucky.apollo.common.utils.PropertiesUtil;
import lucky.apollo.configservice.component.NamespaceService;
import lucky.apollo.configservice.utils.WatchKeysUtil;
import lucky.apollo.core.entity.ReleaseMessagePO;
import lucky.apollo.core.grayReleaseRule.GrayReleaseRulesHolder;
import lucky.apollo.core.message.MessageTopic;
import lucky.apollo.core.message.ReleaseMessageListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author luckylau
 * @Date 2019/12/10
 */
@RestController
@RequestMapping("/configfiles")
@Slf4j
public class ConfigFileController implements ReleaseMessageListener {

    private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
    private static final Splitter X_FORWARDED_FOR_SPLITTER = Splitter.on(",").omitEmptyStrings()
            .trimResults();
    /**
     * // 50MB
     */
    private static final long MAX_CACHE_SIZE = 50 * 1024 * 1024;
    private static final long EXPIRE_AFTER_WRITE = 30;
    private final HttpHeaders propertiesResponseHeaders;
    private final HttpHeaders jsonResponseHeaders;
    private final ResponseEntity<String> NOT_FOUND_RESPONSE;
    private Cache<String, String> localCache;
    private final Multimap<String, String>
            watchedKeys2CacheKey = Multimaps.synchronizedSetMultimap(HashMultimap.create());
    private final Multimap<String, String>
            cacheKey2WatchedKeys = Multimaps.synchronizedSetMultimap(HashMultimap.create());
    private static final Gson gson = new Gson();

    private final ConfigController configController;
    private final NamespaceService namespaceService;
    private final WatchKeysUtil watchKeysUtil;
    private final GrayReleaseRulesHolder grayReleaseRulesHolder;

    public ConfigFileController(
            final ConfigController configController,
            final NamespaceService namespaceService,
            final WatchKeysUtil watchKeysUtil,
            final GrayReleaseRulesHolder grayReleaseRulesHolder) {
        localCache = CacheBuilder.newBuilder()
                .expireAfterWrite(EXPIRE_AFTER_WRITE, TimeUnit.MINUTES)
                .weigher((Weigher<String, String>) (key, value) -> value.length())
                .maximumWeight(MAX_CACHE_SIZE)
                .removalListener(notification -> {
                    String cacheKey = notification.getKey();
                    log.debug("removing cache key: {}", cacheKey);
                    if (!cacheKey2WatchedKeys.containsKey(cacheKey)) {
                        return;
                    }
                    //create a new list to avoid ConcurrentModificationException
                    List<String> watchedKeys = new ArrayList<>(cacheKey2WatchedKeys.get(cacheKey));
                    for (String watchedKey : watchedKeys) {
                        watchedKeys2CacheKey.remove(watchedKey, cacheKey);
                    }
                    cacheKey2WatchedKeys.removeAll(cacheKey);
                    log.debug("removed cache key: {}", cacheKey);
                })
                .build();
        propertiesResponseHeaders = new HttpHeaders();
        propertiesResponseHeaders.add("Content-Type", "text/plain;charset=UTF-8");
        jsonResponseHeaders = new HttpHeaders();
        jsonResponseHeaders.add("Content-Type", "application/json;charset=UTF-8");
        NOT_FOUND_RESPONSE = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        this.configController = configController;
        this.namespaceService = namespaceService;
        this.watchKeysUtil = watchKeysUtil;
        this.grayReleaseRulesHolder = grayReleaseRulesHolder;
    }

    @GetMapping(value = "/{appId}/{clusterName}/{namespace:.+}")
    public ResponseEntity<String> queryConfigAsProperties(@PathVariable String appId,
                                                          @PathVariable String clusterName,
                                                          @PathVariable String namespace,
                                                          @RequestParam(value = "dataCenter", required = false) String dataCenter,
                                                          @RequestParam(value = "ip", required = false) String clientIp,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response)
            throws IOException {

        String result =
                queryConfig(ConfigFileOutputFormat.PROPERTIES, appId, clusterName, namespace, dataCenter,
                        clientIp, request, response);

        if (result == null) {
            return NOT_FOUND_RESPONSE;
        }

        return new ResponseEntity<>(result, propertiesResponseHeaders, HttpStatus.OK);
    }

    @GetMapping(value = "/json/{appId}/{clusterName}/{namespace:.+}")
    public ResponseEntity<String> queryConfigAsJson(@PathVariable String appId,
                                                    @PathVariable String clusterName,
                                                    @PathVariable String namespace,
                                                    @RequestParam(value = "dataCenter", required = false) String dataCenter,
                                                    @RequestParam(value = "ip", required = false) String clientIp,
                                                    HttpServletRequest request,
                                                    HttpServletResponse response) throws IOException {

        String result =
                queryConfig(ConfigFileOutputFormat.JSON, appId, clusterName, namespace, dataCenter,
                        clientIp, request, response);

        if (result == null) {
            return NOT_FOUND_RESPONSE;
        }

        return new ResponseEntity<>(result, jsonResponseHeaders, HttpStatus.OK);
    }

    String queryConfig(ConfigFileOutputFormat outputFormat, String appId, String clusterName,
                       String namespace, String dataCenter, String clientIp,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        //strip out .properties suffix
        namespace = namespaceService.filterNamespaceName(namespace);
        //fix the character case issue, such as FX.apollo <-> fx.apollo
        namespace = namespaceService.normalizeNamespace(appId, namespace);

        if (Strings.isNullOrEmpty(clientIp)) {
            clientIp = tryToGetClientIp(request);
        }

        //1. check whether this client has gray release rules
        boolean hasGrayReleaseRule = grayReleaseRulesHolder.hasGrayReleaseRule(appId, clientIp,
                namespace);

        String cacheKey = assembleCacheKey(outputFormat, appId, clusterName, namespace, dataCenter);

        //2. try to load gray release and return
        if (hasGrayReleaseRule) {
            return loadConfig(outputFormat, appId, clusterName, namespace, dataCenter, clientIp,
                    request, response);
        }

        //3. if not gray release, check weather cache exists, if exists, return
        String result = localCache.getIfPresent(cacheKey);

        //4. if not exists, load from ConfigController
        if (Strings.isNullOrEmpty(result)) {
            result = loadConfig(outputFormat, appId, clusterName, namespace, dataCenter, clientIp,
                    request, response);

            if (result == null) {
                return null;
            }
            //5. Double check if this client needs to load gray release, if yes, load from db again
            //This step is mainly to avoid cache pollution
            if (grayReleaseRulesHolder.hasGrayReleaseRule(appId, clientIp, namespace)) {
                return loadConfig(outputFormat, appId, clusterName, namespace, dataCenter, clientIp,
                        request, response);
            }

            localCache.put(cacheKey, result);
            log.debug("adding cache for key: {}", cacheKey);

            Set<String> watchedKeys =
                    watchKeysUtil.assembleAllWatchKeys(appId, clusterName, namespace, dataCenter);

            for (String watchedKey : watchedKeys) {
                watchedKeys2CacheKey.put(watchedKey, cacheKey);
            }

            cacheKey2WatchedKeys.putAll(cacheKey, watchedKeys);
            log.debug("added cache for key: {}", cacheKey);
        } else {
            log.warn("ConfigFile.Cache.Hit, {}", cacheKey);
        }

        return result;
    }

    private String loadConfig(ConfigFileOutputFormat outputFormat, String appId, String clusterName,
                              String namespace, String dataCenter, String clientIp,
                              HttpServletRequest request,
                              HttpServletResponse response) throws IOException {
        ApolloConfigDTO apolloConfig = configController.queryConfig(appId, clusterName, namespace,
                dataCenter, "-1", clientIp, null, request, response);

        if (apolloConfig == null || apolloConfig.getConfigurations() == null) {
            return null;
        }

        String result = null;

        switch (outputFormat) {
            case PROPERTIES:
                Properties properties = new Properties();
                properties.putAll(apolloConfig.getConfigurations());
                result = PropertiesUtil.toString(properties);
                break;
            case JSON:
                result = gson.toJson(apolloConfig.getConfigurations());
                break;
            default:
                break;
        }

        return result;
    }

    String assembleCacheKey(ConfigFileOutputFormat outputFormat, String appId, String clusterName,
                            String namespace,
                            String dataCenter) {
        List<String> keyParts =
                Lists.newArrayList(outputFormat.getValue(), appId, clusterName, namespace);
        if (!Strings.isNullOrEmpty(dataCenter)) {
            keyParts.add(dataCenter);
        }
        return STRING_JOINER.join(keyParts);
    }

    @Override
    public void handleMessage(ReleaseMessagePO message, String channel) {
        log.info("message received - channel: {}, message: {}", channel, message);

        String content = message.getMessage();
        if (!MessageTopic.APOLLO_RELEASE_TOPIC.equals(channel) || Strings.isNullOrEmpty(content)) {
            return;
        }

        if (!watchedKeys2CacheKey.containsKey(content)) {
            return;
        }

        //create a new list to avoid ConcurrentModificationException
        List<String> cacheKeys = new ArrayList<>(watchedKeys2CacheKey.get(content));

        for (String cacheKey : cacheKeys) {
            log.debug("invalidate cache key: {}", cacheKey);
            localCache.invalidate(cacheKey);
        }
    }

    enum ConfigFileOutputFormat {
        /**
         *
         */
        PROPERTIES("properties"),
        /**
         *
         */
        JSON("json");

        private String value;

        ConfigFileOutputFormat(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private String tryToGetClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-FORWARDED-FOR");
        if (!Strings.isNullOrEmpty(forwardedFor)) {
            return X_FORWARDED_FOR_SPLITTER.splitToList(forwardedFor).get(0);
        }
        return request.getRemoteAddr();
    }


}
