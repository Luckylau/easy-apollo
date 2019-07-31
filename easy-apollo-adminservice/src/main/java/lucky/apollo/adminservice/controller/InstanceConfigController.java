package lucky.apollo.adminservice.controller;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lucky.apollo.common.entity.dto.InstanceConfigDTO;
import lucky.apollo.common.entity.dto.InstanceDTO;
import lucky.apollo.common.entity.dto.PageDTO;
import lucky.apollo.common.entity.dto.ReleaseDTO;
import lucky.apollo.common.exception.NotFoundException;
import lucky.apollo.common.utils.BeanUtils;
import lucky.apollo.core.entity.InstanceConfigPO;
import lucky.apollo.core.entity.InstancePO;
import lucky.apollo.core.entity.ReleasePO;
import lucky.apollo.core.service.InstanceService;
import lucky.apollo.core.service.ReleaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
@RestController
@RequestMapping("/instances")
public class InstanceConfigController {
    private static final Splitter RELEASES_SPLITTER = Splitter.on(",").omitEmptyStrings()
            .trimResults();
    private final ReleaseService releaseService;
    private final InstanceService instanceService;

    public InstanceConfigController(final ReleaseService releaseService, final InstanceService instanceService) {
        this.releaseService = releaseService;
        this.instanceService = instanceService;
    }

    @GetMapping("/by-release")
    public PageDTO<InstanceDTO> getByRelease(@RequestParam("releaseId") long releaseId,
                                             Pageable pageable) {
        ReleasePO release = releaseService.findOne(releaseId);
        if (release == null) {
            throw new NotFoundException(String.format("release not found for %s", releaseId));
        }
        Page<InstanceConfigPO> instanceConfigsPage = instanceService.findActiveInstanceConfigsByReleaseKey
                (release.getReleaseKey(), pageable);

        List<InstanceDTO> instanceDTOs = Collections.emptyList();

        if (instanceConfigsPage.hasContent()) {
            Multimap<Long, InstanceConfigPO> instanceConfigMap = HashMultimap.create();
            Set<String> otherReleaseKeys = Sets.newHashSet();

            for (InstanceConfigPO instanceConfig : instanceConfigsPage.getContent()) {
                instanceConfigMap.put(instanceConfig.getInstanceId(), instanceConfig);
                otherReleaseKeys.add(instanceConfig.getReleaseKey());
            }

            Set<Long> instanceIds = instanceConfigMap.keySet();

            List<InstancePO> instances = instanceService.findInstancesByIds(instanceIds);

            if (!CollectionUtils.isEmpty(instances)) {
                instanceDTOs = BeanUtils.batchTransformWithIgnoreNull(InstanceDTO.class, instances);
            }

            for (InstanceDTO instanceDTO : instanceDTOs) {
                Collection<InstanceConfigPO> configs = instanceConfigMap.get(instanceDTO.getId());
                List<InstanceConfigDTO> configDTOs = configs.stream().map(instanceConfig -> {
                    InstanceConfigDTO instanceConfigDTO = new InstanceConfigDTO();
                    //to save some space
                    instanceConfigDTO.setRelease(null);
                    instanceConfigDTO.setReleaseDeliveryTime(instanceConfig.getReleaseDeliveryTime());
                    instanceConfigDTO.setDataChangeLastModifiedTime(instanceConfig
                            .getDataChangeLastModifiedTime());
                    return instanceConfigDTO;
                }).collect(Collectors.toList());
                instanceDTO.setConfigs(configDTOs);
            }
        }

        return new PageDTO<>(instanceDTOs, pageable, instanceConfigsPage.getTotalElements());
    }

    @GetMapping("/by-namespace-and-releases-not-in")
    public List<InstanceDTO> getByReleasesNotIn(@RequestParam("appId") String appId,
                                                @RequestParam("clusterName") String clusterName,
                                                @RequestParam("namespaceName") String namespaceName,
                                                @RequestParam("releaseIds") String releaseIds) {
        Set<Long> releaseIdSet = RELEASES_SPLITTER.splitToList(releaseIds).stream().map(Long::parseLong)
                .collect(Collectors.toSet());

        List<ReleasePO> releases = releaseService.findByReleaseIds(releaseIdSet);

        if (CollectionUtils.isEmpty(releases)) {
            throw new NotFoundException(String.format("releases not found for %s", releaseIds));
        }

        Set<String> releaseKeys = releases.stream().map(ReleasePO::getReleaseKey).collect(Collectors
                .toSet());

        List<InstanceConfigPO> instanceConfigs = instanceService
                .findInstanceConfigsByNamespaceWithReleaseKeysNotIn(appId, clusterName, namespaceName,
                        releaseKeys);

        Multimap<Long, InstanceConfigPO> instanceConfigMap = HashMultimap.create();
        Set<String> otherReleaseKeys = Sets.newHashSet();

        for (InstanceConfigPO instanceConfig : instanceConfigs) {
            instanceConfigMap.put(instanceConfig.getInstanceId(), instanceConfig);
            otherReleaseKeys.add(instanceConfig.getReleaseKey());
        }

        List<InstancePO> instances = instanceService.findInstancesByIds(instanceConfigMap.keySet());

        if (CollectionUtils.isEmpty(instances)) {
            return Collections.emptyList();
        }

        List<InstanceDTO> instanceDTOs = BeanUtils.batchTransformWithIgnoreNull(InstanceDTO.class, instances);

        List<ReleasePO> otherReleases = releaseService.findByReleaseKeys(otherReleaseKeys);
        Map<String, ReleaseDTO> releaseMap = Maps.newHashMap();

        for (ReleasePO release : otherReleases) {
            //unset configurations to save space
            release.setConfigurations(null);
            ReleaseDTO releaseDTO = BeanUtils.transformWithIgnoreNull(ReleaseDTO.class, release);
            releaseMap.put(release.getReleaseKey(), releaseDTO);
        }

        for (InstanceDTO instanceDTO : instanceDTOs) {
            Collection<InstanceConfigPO> configs = instanceConfigMap.get(instanceDTO.getId());
            List<InstanceConfigDTO> configDTOs = configs.stream().map(instanceConfig -> {
                InstanceConfigDTO instanceConfigDTO = new InstanceConfigDTO();
                instanceConfigDTO.setRelease(releaseMap.get(instanceConfig.getReleaseKey()));
                instanceConfigDTO.setReleaseDeliveryTime(instanceConfig.getReleaseDeliveryTime());
                instanceConfigDTO.setDataChangeLastModifiedTime(instanceConfig
                        .getDataChangeLastModifiedTime());
                return instanceConfigDTO;
            }).collect(Collectors.toList());
            instanceDTO.setConfigs(configDTOs);
        }

        return instanceDTOs;
    }

    @GetMapping("/by-namespace")
    public PageDTO<InstanceDTO> getInstancesByNamespace(
            @RequestParam("appId") String appId, @RequestParam("clusterName") String clusterName,
            @RequestParam("namespaceName") String namespaceName,
            @RequestParam(value = "instanceAppId", required = false) String instanceAppId,
            Pageable pageable) {
        Page<InstancePO> instances;
        if (Strings.isNullOrEmpty(instanceAppId)) {
            instances = instanceService.findInstancesByNamespace(appId, clusterName,
                    namespaceName, pageable);
        } else {
            instances = instanceService.findInstancesByNamespaceAndInstanceAppId(instanceAppId, appId,
                    clusterName, namespaceName, pageable);
        }

        List<InstanceDTO> instanceDTOs = BeanUtils.batchTransformWithIgnoreNull(InstanceDTO.class, instances.getContent());
        return new PageDTO<>(instanceDTOs, pageable, instances.getTotalElements());
    }

    @GetMapping("/by-namespace/count")
    public long getInstancesCountByNamespace(@RequestParam("appId") String appId,
                                             @RequestParam("clusterName") String clusterName,
                                             @RequestParam("namespaceName") String namespaceName) {
        Page<InstancePO> instances = instanceService.findInstancesByNamespace(appId, clusterName,
                namespaceName, PageRequest.of(0, 1));
        return instances.getTotalElements();
    }
}