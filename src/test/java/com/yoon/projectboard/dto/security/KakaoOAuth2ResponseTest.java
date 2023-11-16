package com.yoon.projectboard.dto.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTO - Kakao OAuth 2.0 인증 응답 데이터 테스트")
class KakaoOAuth2ResponseTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @DisplayName("인증 결과를 Map(deserialized json)으로 받으면, 카카오 인증 응답 객체로 변환한다.")
    @Test
    void givenMapFromJson_whenInstantiating_thenReturnsKakaoResponseObject() throws JsonProcessingException {
        //given
        String serializedResponse = """
                {
                    "id": 1234567890,
                    "connected_at": "2023-11-14T00:12:34Z",
                    "properties": {
                        "nickname": "yoon"
                    },
                    "kakao_account": {
                        "profile_nickname_needs_agreement": false,
                        "profile": {
                            "nickname": "yoon"
                        },
                        "has_email": true,
                        "email_needs_agreement": false,
                        "is_email_valid": true,
                        "is_email_verified": true,
                        "email": "yoon@naver.com"
                    }
                }
                """;
        Map<String, Object> attributes = mapper.readValue(serializedResponse, new TypeReference<>() {});

        //when
        KakaoOAuth2Response result = KakaoOAuth2Response.from(attributes);

        //then
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", 1234567890L)
                .hasFieldOrPropertyWithValue("connectedAt", ZonedDateTime.of(
                                        2023, 11, 14, 0, 12, 34, 0, ZoneOffset.UTC
                                )
                                .withZoneSameInstant(ZoneId.systemDefault())
                                .toLocalDateTime()
                )
                .hasFieldOrPropertyWithValue("kakaoAccount.profile.nickname", "yoon")
                .hasFieldOrPropertyWithValue("kakaoAccount.email", "yoon@naver.com");
    }
}
