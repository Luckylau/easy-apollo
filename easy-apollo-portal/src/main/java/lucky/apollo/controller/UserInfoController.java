package lucky.apollo.controller;

import lucky.apollo.entity.bo.UserInfo;
import lucky.apollo.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
