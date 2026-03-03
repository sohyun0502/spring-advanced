package org.example.expert.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class UserRoleCHeckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 인증된 사용자의 권한 가져오기
        String role = (String) request.getAttribute("userRole");

        // 권한 체크
        if (role == null || !role.equals("ADMIN")) {
            // response.sendError(HttpServletResponse.SC_FORBIDDEN, "ADMIN 권한이 필요합니다.");
            throw new AccessDeniedException("ADMIN 권한이 필요합니다.");
        }

        // 인증 성공 시 로깅
        log.info("ADMIN 접근 성공 - 시간: {}, URL: {}",
                LocalDateTime.now(),
                request.getRequestURI());

        return true;
    }
}
