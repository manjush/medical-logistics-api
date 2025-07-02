package com.medical.logistics.infrastructure.config;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        Instant start = Instant.now();
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        try {
            log.info("[{}] {} {} from {}",
                    requestId,
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    httpRequest.getRemoteAddr()
            );

            chain.doFilter(request, response);

        } finally {
            Duration duration = Duration.between(start, Instant.now());

            log.info("[{}] {} {} - Status: {} - Duration: {}ms",
                    requestId,
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    httpResponse.getStatus(),
                    duration.toMillis()
            );
        }
    }
}
