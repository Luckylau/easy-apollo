package lucky.apollo.service;

import lucky.apollo.entity.bo.UserInfo;
import lucky.apollo.entity.po.UserPO;
import lucky.apollo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author luckylau
 * @Date 2019/7/11
 */
@Service
public class SpringSecurityUserService implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserInfo findByusername(String username) {
        UserPO userPo = userRepository.findByUsername(username);
        return userPo != null ? userPo.toUserInfo() : null;
    }
}
