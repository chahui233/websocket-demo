package com.example.websocketdemo.config;

import com.example.websocketdemo.controller.VoteWebSocketHandler;
import com.example.websocketdemo.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private VoteService voteService;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(
                new VoteWebSocketHandler(voteService),
                "/ws"
                ).addInterceptors(new LoginHandshakeInterceptor())
                .setAllowedOrigins("*");
    }

}