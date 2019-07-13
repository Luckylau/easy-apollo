package lucky.apollo.service;

import lucky.apollo.entity.bo.UserInfo;
import lucky.apollo.entity.po.UserPO;
import lucky.apollo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/11
 */
@Service
public class SpringSecurityUserService implements UserService {
    @Autowired
    private UserRepository userRepository;

    private PasswordEncoder encoder = new BCryptPasswordEncoder();
    private List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));

    @Autowired
    private JdbcUserDetailsManager jdbcUserDetailsManager;

    @Override
    public UserInfo findByusername(String username) {
        UserPO userPo = userRepository.findByUsername(username);
        return userPo != null ? userPo.toUserInfo() : null;
    }

    @Transactional
    public void createOrUpdate(UserPO user) {
        String username = user.getUsername();

        User userDetails = new User(username, encoder.encode(user.getPassword()), authorities);

        if (jdbcUserDetailsManager.userExists(username)) {
            jdbcUserDetailsManager.updateUser(userDetails);
        } else {
            jdbcUserDetailsManager.createUser(userDetails);
        }

        UserPO managedUser = userRepository.findByUsername(username);
        managedUser.setEmail(user.getEmail());

        userRepository.save(managedUser);
    }
}
