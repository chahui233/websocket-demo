package com.example.websocketdemo.mapper;

import com.example.websocketdemo.model.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("select * from user where username=#{username}")
    User findByUsername(String username);

    @Insert("insert into user(username,password,role) values(#{username},#{password},'user')")
    void insert(User user);

}