package lucky.apollo.core.service;

import lucky.apollo.core.entity.CommitPO;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
public interface CommitService {

    CommitPO save(CommitPO commit);

    List<CommitPO> find(String appId, String clusterName, String namespaceName, Pageable page);

    int batchDelete(String appId, String clusterName, String namespaceName, String operator);

}
