package com.ratel.es.controller;

import com.ratel.es.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @业务描述：
 * @package_name： com.ratel.es.controller
 * @project_name： springdata-es-action
 * @author： ratelfu@qq.com
 * @create_time： 2020-01-12 20:40
 * @copyright (c) ratelfu 版权所有
 */
@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @RequestMapping("queryString")
    public Object queryString(String key, @PageableDefault Pageable pageable){
        return userRepository.queryString(key, pageable);
    }
}
