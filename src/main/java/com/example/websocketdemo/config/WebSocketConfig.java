package com.example.websocketdemo.config;

import com.example.websocketdemo.controller.VoteWebSocketHandler;
import com.example.websocketdemo.service.VoteService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(
                new VoteWebSocketHandler(new VoteService()),
                "/ws"
        ).addInterceptors(new LoginHandshakeInterceptor())
         .setAllowedOrigins("*");

    }

}
