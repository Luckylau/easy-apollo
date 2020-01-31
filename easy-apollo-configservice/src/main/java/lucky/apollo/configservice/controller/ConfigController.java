package lucky.apollo.configservice.controller;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.constant.ConfigConsts;
import lucky.apollo.common.entity.dto.ApolloConfigDTO;
import lucky.apollo.common.entity.dto.ApolloNotificationMessageDTO;
import lucky.apollo.configservice.component.InstanceConfigAuditService;
import lucky.apollo.configservice.component.NamespaceService;
import lucky.apollo.configservice.service.ConfigService;
import lucky.apollo.core.entity.ReleasePO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author luckylau
 * @Date 2019/12/4
 */
@RestController
@RequestMapping("/configs")
@Slf4j
public class ConfigController {
    private static final Splitter X_FORWARDED_FOR_SPLITTER = Splitter.on(",").omitEmptyStrings()
            .trimResults();
    private final ConfigService configService;
    private final NamespaceService namespaceService;
    private final InstanceConfigAuditService instanceConfigAuditService;
    private final Gson gson;

    private static final Type configurationTypeReference = new TypeToken<Map<String, String>>() {
    }.getType();

    public ConfigController(
            final ConfigService configService,
            final NamespaceService namespaceService,
            final InstanceConfigAuditService instanceConfigAuditService,
            final Gson gson) {
        this.configService = configService;
        this.namespaceService = namespaceService;
        this.instanceConfigAuditService = instanceConfigAuditService;
        this.gson = gson;
    }

    @GetMapping(value = "/{appId}/{clusterName}/{namespace:.+}")
    public ApolloConfigDTO queryConfig(@PathVariable String appId, @PathVariable String clusterName,
                                       @PathVariable String namespace,
                                       @RequestParam(value = "dataCenter", required = false) String dataCenter,
                                       @RequestParam(value = "releaseKey", defaultValue = "-1") String clientSideReleaseKey,
                                       @RequestParam(value = "ip", required = false) String clientIp,
                                       @RequestParam(value = "messages", required = false) String messagesAsString,
                                       HttpServletRequest request, HttpServletResponse response) throws IOException {
        String originalNamespace = namespace;
        //strip out .properties suffix
        namespace = namespaceService.filterNamespaceName(namespace);
        //fix the character case issue, such as FX.apollo <-> fx.apollo
        namespace = namespaceService.normalizeNamespace(appId, namespace);

        if (Strings.isNullOrEmpty(clientIp)) {
            clientIp = tryToGetClientIp(request);
        }

        ApolloNotificationMessageDTO clientMessages = transformMessages(messagesAsString);

        List<ReleasePO> releases = Lists.newLinkedList();

        String appClusterNameLoaded = clusterName;
        if (!ConfigConsts.NO_APP_ID_PLACEHOLDER.equalsIgnoreCase(appId)) {
            ReleasePO currentAppRelease = configService.loadConfig(appId, clientIp, appId, clusterName, namespace,
                    dataCenter, clientMessages);

            if (currentAppRelease != null) {
                releases.add(currentAppRelease);
                //we have cluster search process, so the cluster name might be overridden
                appClusterNameLoaded = currentAppRelease.getClusterName();
            }
        }

        if (releases.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    String.format(
                            "Could not load configurations with appId: %s, clusterName: %s, namespace: %s",
                            appId, clusterName, originalNamespace));
            return null;
        }

        auditReleases(appId, clusterName, dataCenter, clientIp, releases);

        String mergedReleaseKey = releases.stream().map(ReleasePO::getReleaseKey)
                .collect(Collectors.joining(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR));

        if (mergedReleaseKey.equals(clientSideReleaseKey)) {
            // Client side configuration is the same with server side, return 304
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return null;
        }

        ApolloConfigDTO apolloConfig = new ApolloConfigDTO(appId, appClusterNameLoaded, originalNamespace,
                mergedReleaseKey);
        apolloConfig.setConfigurations(mergeReleaseConfigurations(releases));
        return apolloConfig;
    }


    /**
     * Merge configurations of releases.
     * Release in lower index override those in higher index
     */
    Map<String, String> mergeReleaseConfigurations(List<ReleasePO> releases) {
        Map<String, String> result = Maps.newHashMap();
        for (ReleasePO release : Lists.reverse(releases)) {
            result.putAll(gson.fromJson(release.getConfigurations(), configurationTypeReference));
        }
        return result;
    }

    private void auditReleases(String appId, String cluster, String dataCenter, String clientIp,
                               List<ReleasePO> releases) {
        if (Strings.isNullOrEmpty(clientIp)) {
            //no need to audit instance config when there is no ip
            return;
        }
        for (ReleasePO release : releases) {
            instanceConfigAuditService.audit(appId, cluster, dataCenter, clientIp, release.getAppId(),
                    release.getClusterName(),
                    release.getNamespaceName(), release.getReleaseKey());
        }
    }

    private String tryToGetClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-FORWARDED-FOR");
        if (!Strings.isNullOrEmpty(forwardedFor)) {
            return X_FORWARDED_FOR_SPLITTER.splitToList(forwardedFor).get(0);
        }
        return request.getRemoteAddr();
    }

    ApolloNotificationMessageDTO transformMessages(String messagesAsString) {
        ApolloNotificationMessageDTO notificationMessages = null;
        if (!Strings.isNullOrEmpty(messagesAsString)) {
            try {
                notificationMessages = gson.fromJson(messagesAsString, ApolloNotificationMessageDTO.class);
            } catch (Throwable ex) {
                log.error(ex.getMessage());
            }
        }

        return notificationMessages;
    }


}
