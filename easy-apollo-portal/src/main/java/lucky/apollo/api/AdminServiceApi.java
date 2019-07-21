package lucky.apollo.api;

import lucky.apollo.entity.dto.AppDTO;
import lucky.apollo.entity.dto.CommitDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/17
 */
@Service
public class AdminServiceApi {

    public AppDTO loadApp(String appId) {
        return null;
    }

    public AppDTO createApp(AppDTO app) {
        return null;
    }

    public void updateApp(AppDTO app) {
    }

    public void deleteApp(String appId, String operator) {

    }

    public List<CommitDTO> find(String appId, String namespaceName, int page, int size) {
        return null;
    }
}