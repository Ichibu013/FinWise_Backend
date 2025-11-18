package com.fintech.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
//                        .setAllowedOrigins("*")
                                .setAllowedOriginPatterns("*");

        // Enable SockJS to support environments/browsers that do not support native WebSocket
        // This also matches typical client usage with SockJS libraries.
        registry
                .addEndpoint("/websocket")
                .setAllowedOrigins("*")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setSuppressCors(true);
    }
}
