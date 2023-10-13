package com.yoon.projectboard.dto;

public record ArticleUpdateDto(
        String title,                   //제목
        String content,                 //본문
        String hashtag                 //해시태그
) {
    public static ArticleUpdateDto of(String title, String content, String hashtag) {
        return new ArticleUpdateDto(title, content, hashtag);
    }
}
