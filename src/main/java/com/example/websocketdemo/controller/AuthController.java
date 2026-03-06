package com.example.websocketdemo.controller;

import com.example.websocketdemo.model.User;
import com.example.websocketdemo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public String login(String username,String password,HttpSession session){

        User user=userService.login(username,password);

        if(user==null){
            return "fail";
        }

        session.setAttribute("user",user);

        return user.getRole();
    }

    @PostMapping("/register")
    public String register(User user){

        if(userService.register(user)){
            return "success";
        }

        return "exist";
    }

}