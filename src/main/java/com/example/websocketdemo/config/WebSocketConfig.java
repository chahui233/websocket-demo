package com.example.websocketdemo.config;

import com.example.websocketdemo.controller.VoteWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final VoteWebSocketHandler voteWebSocketHandler;

    public WebSocketConfig(VoteWebSocketHandler voteWebSocketHandler) {
        this.voteWebSocketHandler = voteWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(voteWebSocketHandler, "/vote")
                .setAllowedOrigins("*");
    }
}
