package com.example.websocketdemo.controller;

import com.example.websocketdemo.model.User;
import com.example.websocketdemo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

    @PostMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "success";
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session){
        User user = (User) session.getAttribute("user");

        if(user == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized");
        }

        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "role", user.getRole()
        ));
    }

}
