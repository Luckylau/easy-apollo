package lucky.apollo.service.impl;

import com.google.common.collect.Lists;
import lucky.apollo.entity.bo.UserInfo;
import lucky.apollo.entity.po.UserPO;
import lucky.apollo.repository.UserRepository;
import lucky.apollo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author luckylau
 * @Date 2019/7/11
 */
@Service
public class SpringSecurityUserServiceImpl implements UserService {
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

    @Override
    @Transactional(rollbackFor = Exception.class)
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

    @Override
    public List<UserInfo> searchUsers(String keyword, int offset, int limit) {
        List<UserPO> userPOs;
        if (StringUtils.isEmpty(keyword)) {
            userPOs = userRepository.findFirst20ByEnabled(1);
        } else {
            userPOs = userRepository.findByUsernameLikeAndEnabled("%" + keyword + "%", 1);
        }
        List<UserInfo> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(userPOs)) {
            return result;
        }

        result.addAll(userPOs.stream().map(UserPO::toUserInfo).collect(Collectors.toList()));
        return result;
    }

    @Override
    public String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        if (principal instanceof Principal) {
            return ((Principal) principal).getName();
        }
        return String.valueOf(principal);
    }
}