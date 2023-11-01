package com.yoon.projectboard.config;

import com.yoon.projectboard.domain.UserAccount;
import com.yoon.projectboard.repository.UserAccountRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

import java.util.Optional;

import static org.mockito.BDDMockito.given;

@Import(SecurityConfig.class)
public class TestSecurityConfig {

    @MockBean
    private UserAccountRepository userAccountRepository;

    @BeforeTestMethod
    public void securitySetup() {
        String userId = "yoonSecurity";
        given(userAccountRepository.findById(userId)).willReturn(Optional.of(UserAccount.of(
                "yoonSecurity",
                "pwSecurity",
                "yoonSecurity@naver.com",
                "Yoon-Security",
                "yoon Security memo"
        )));
    }
}
