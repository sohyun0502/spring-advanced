package org.example.expert.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class UserRoleCheckAop {

    private final ObjectMapper objectMapper;

    @Around("execution(* org.example.expert.domain.user.service.UserAdminService.*(..))")
    public Object userAdminApiLogging(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        Long userId = (Long) request.getAttribute("userId");
        LocalDateTime requestTime = LocalDateTime.now();
        String requestUrl = request.getRequestURI();
        String requestBodyJson = convertToJson(joinPoint.getArgs()); // 요청 RequestBody

        log.info("""
                [ADMIN API 요청]
                사용자 ID: {}
                요청 시각: {}
                요청 URL: {}
                요청 RequestBody(JSON): {}
                """,
                userId,
                requestTime,
                requestUrl,
                requestBodyJson
        );

        // 메서드 시행
        Object result = joinPoint.proceed();

        String responseBodyJson = convertToJson(result); // 응답 ResponseBody

        log.info("""
                [ADMIN API 응답]
                사용자 ID: {}
                요청 URL: {}
                응답 ResponseBody(JSON): {}
                """,
                userId,
                requestUrl,
                responseBodyJson
        );

        return result;
    }

    @Around("execution(* org.example.expert.domain.comment.service.CommentAdminService.*(..))")
    public Object commentAdminApiLogging(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        Long userId = (Long) request.getAttribute("userId");
        LocalDateTime requestTime = LocalDateTime.now();
        String requestUrl = request.getRequestURI();
        String requestBodyJson = convertToJson(joinPoint.getArgs()); // 요청 RequestBody

        log.info("""
                [ADMIN API 요청]
                사용자 ID: {}
                요청 시각: {}
                요청 URL: {}
                요청 RequestBody(JSON): {}
                """,
                userId,
                requestTime,
                requestUrl,
                requestBodyJson
        );

        // 메서드 시행
        Object result = joinPoint.proceed();

        String responseBodyJson = convertToJson(result); // 응답 ResponseBody

        log.info("""
                [ADMIN API 응답]
                사용자 ID: {}
                요청 URL: {}
                응답 ResponseBody(JSON): {}
                """,
                userId,
                requestUrl,
                responseBodyJson
        );

        return result;
    }

    /**
     * 객체를 JSON 문자열로 변환
     */
    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "JSON 변환 실패";
        }
    }
}
