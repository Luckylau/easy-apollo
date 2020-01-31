package lucky.apollo.adminservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
@RestController
@RequestMapping(path = "/")
public class IndexController {

    @GetMapping
    public String index() {
        return "easy-apollo-adminservice";
    }
}