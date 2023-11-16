package com.yoon.projectboard.config;

import com.yoon.projectboard.dto.UserAccountDto;
import com.yoon.projectboard.service.UserAccountService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@Import(SecurityConfig.class)
public class TestSecurityConfig {

    @MockBean
    private UserAccountService userAccountService;

    @BeforeTestMethod
    public void securitySetup() {
        String userId = "yoonSecurity";
        given(userAccountService.searchUser(userId)).willReturn(Optional.of(createUserAccountDto()));
        given(userAccountService.saveUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .willReturn(createUserAccountDto());
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "yoonSecurity",
                "pwSecurity",
                "yoonSecurity@naver.com",
                "Yoon-Security",
                "yoon Security memo"
        );
    }
}
