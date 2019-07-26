package lucky.apollo.portal.controller;


import lucky.apollo.common.exception.BadRequestException;
import lucky.apollo.portal.entity.bo.UserInfo;
import lucky.apollo.portal.entity.po.UserPO;
import lucky.apollo.portal.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/11
 */
@RestController
public class UserInfoController {
    private final UserService userService;

    public UserInfoController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/{username}")
    public UserInfo getUserByUserId(@PathVariable String username) {
        return userService.findByusername(username);
    }

    @GetMapping("/user")
    public UserInfo getUser() {
        return userService.getCurrentUser();
    }

    @PreAuthorize(value = "@PermissionValidator.isSuperAdmin()")
    @PostMapping("/user")
    public void createOrUpdateUser(@RequestBody UserPO user) {
        if (StringUtils.isEmpty(user.getUsername()) || StringUtils.isEmpty(user.getPassword())) {
            throw new BadRequestException("Username and password can not be empty.");
        }
        userService.createOrUpdate(user);
    }

    @GetMapping("/users")
    public List<UserInfo> searchUsersByKeyword(@RequestParam(value = "keyword") String keyword,
                                               @RequestParam(value = "offset", defaultValue = "0") int offset,
                                               @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return userService.searchUsers(keyword, offset, limit);
    }
}