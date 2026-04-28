package com.example.boardgames.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CentrifugoService {

    private static final Logger log = LoggerFactory.getLogger(CentrifugoService.class);

    @Value("${centrifugo.api.url}")
    private String apiUrl;

    @Value("${centrifugo.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // Публикует data в канал channel через Centrifugo HTTP API
    public void publish(String channel, Object data) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "apikey " + apiKey);

            Map<String, Object> params = new HashMap<>();
            params.put("channel", channel);
            params.put("data", data);

            Map<String, Object> body = new HashMap<>();
            body.put("method", "publish");
            body.put("params", params);

            restTemplate.postForObject(apiUrl, new HttpEntity<>(body, headers), String.class);
            log.info("Published to channel '{}': {}", channel, data);
        } catch (Exception e) {
            // Centrifugo недоступен — игра уже сохранена в БД, продолжаем без WebSocket-уведомления
            log.warn("Centrifugo publish failed: {}", e.getMessage());
        }
    }
}
