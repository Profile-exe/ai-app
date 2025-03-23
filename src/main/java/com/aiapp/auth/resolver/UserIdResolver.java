package com.aiapp.auth.resolver;

import com.aiapp.auth.exception.AccessDeniedException;
import com.aiapp.auth.jwt.service.JwtTokenProvider;
import com.aiapp.user.entity.Role;
import com.aiapp.user.entity.User;
import com.aiapp.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class UserIdResolver implements HandlerMethodArgumentResolver {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserId.class);
    }

    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        String token = jwtTokenProvider.extractToken(webRequest.getHeader(HttpHeaders.AUTHORIZATION));
        UserId userId = parameter.getParameterAnnotation(UserId.class);

        // required == true 이므로 memberId를 추출해 MemberRole을 확인
        Long userIdFromAccessToken = jwtTokenProvider.getUserIdFromAccessToken(token);

        checkMemberRole(userIdFromAccessToken, userId.allowedRoles());
        return userIdFromAccessToken;
    }

    private void checkMemberRole(Long userId, Role[] roles) {
        User user = userService.getUserByIdOrThrow(userId);
        if (!List.of(roles).contains(user.getRole())) {
            throw AccessDeniedException.EXCEPTION;
        }
    }
}
