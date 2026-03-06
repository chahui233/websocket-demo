package com.example.websocketdemo.service;

import com.example.websocketdemo.mapper.UserMapper;
import com.example.websocketdemo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User login(String username,String password){

        User user=userMapper.findByUsername(username);

        if(user!=null && user.getPassword().equals(password)){
            return user;
        }

        return null;

    }

    public boolean register(User user){

        if(userMapper.findByUsername(user.getUsername())!=null){
            return false;
        }

        userMapper.insert(user);

        return true;

    }

}