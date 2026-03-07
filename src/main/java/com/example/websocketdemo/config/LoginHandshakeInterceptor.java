package com.example.websocketdemo.config;

import com.example.websocketdemo.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class LoginHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }

        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            if (response instanceof ServletServerHttpResponse servletResponse) {
                servletResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
            }
            return false;
        }

        User user = (User) session.getAttribute("user");

        if (user == null) {
            if (response instanceof ServletServerHttpResponse servletResponse) {
                servletResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
            }
            return false;
        }

        attributes.put("username", user.getUsername());
        attributes.put("role", user.getRole());

        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
    }
}
