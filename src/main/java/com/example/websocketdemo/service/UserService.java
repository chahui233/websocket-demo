package com.example.websocketdemo.service;

import com.example.websocketdemo.mapper.UserMapper;
import com.example.websocketdemo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserService {
    private static final String DEFAULT_ROLE = "user";

    @Autowired
    private UserMapper userMapper;

    public User login(String username,String password){

        User user=userMapper.findByUsername(username);

        if(user!=null && user.getPassword().equals(password)){
            ensureUserRole(user);
            return user;
        }

        return null;

    }

    public boolean register(User user){

        if(userMapper.findByUsername(user.getUsername())!=null){
            return false;
        }

        ensureUserRole(user);
        userMapper.insertUser(user);

        return true;

    }

    private void ensureUserRole(User user){
        if(!StringUtils.hasText(user.getRole())){
            user.setRole(DEFAULT_ROLE);
        }
    }

}