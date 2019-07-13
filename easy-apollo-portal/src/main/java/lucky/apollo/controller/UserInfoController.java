package lucky.apollo.controller;

import lucky.apollo.entity.bo.UserInfo;
import lucky.apollo.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * @Author luckylau
 * @Date 2019/7/11
 */
@RestController
@RequestMapping("/user")
public class UserInfoController {
    private final UserService userService;

    public UserInfoController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public UserInfo getUserByUserId(@PathVariable String username) {
        return userService.findByusername(username);
    }

    @GetMapping()
    public UserInfo getUser() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(getCurrentUsername());
        return userInfo;
    }

    private String getCurrentUsername() {
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
