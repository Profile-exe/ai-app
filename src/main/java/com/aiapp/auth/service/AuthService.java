package com.aiapp.auth.service;

import com.aiapp.auth.dto.RegisterResposne;
import com.aiapp.auth.jwt.AuthToken;
import com.aiapp.auth.jwt.service.AuthTokenService;
import com.aiapp.user.entity.User;
import com.aiapp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final AuthTokenService authTokenService;

    @Transactional
    public RegisterResposne signUp(String email, String password, String name) {
        userService.register(email, password, name);
        return new RegisterResposne("회원가입이 완료되었습니다.");
    }

    @Transactional
    public AuthToken login(String email, String password) {
        User user = userService.findByEmailAndPassword(email, password);
        return authTokenService.generateAuthToken(user.getId());
    }

    @Transactional
    public AuthToken reissue(String refreshToken) {
        return authTokenService.reissue(refreshToken);
    }
}
