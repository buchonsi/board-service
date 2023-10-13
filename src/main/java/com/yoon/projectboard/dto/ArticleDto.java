package com.yoon.projectboard.dto;

import java.time.LocalDateTime;

public record ArticleDto(
        String title,                   //제목
        String content,                 //본문
        String hashtag,                 //해시태그
        LocalDateTime createdAt,        //생성일시
        String createdBy               //생성자
) {
    public static ArticleDto of(String title, String content, String hashtag, LocalDateTime createdAt, String createdBy) {
        return new ArticleDto(title, content, hashtag, createdAt, createdBy);
    }
}
