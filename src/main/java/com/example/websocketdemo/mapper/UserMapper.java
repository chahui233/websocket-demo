package com.example.websocketdemo.mapper;

import com.example.websocketdemo.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User findByUsername(@Param("username") String username);

    void insertUser(User user);

}
