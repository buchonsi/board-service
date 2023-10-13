package com.yoon.projectboard.dto;

import java.time.LocalDateTime;

public record ArticleCommentDto(
        String content,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy
) {
    public static ArticleCommentDto of(String content, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
        return new ArticleCommentDto(content, createdAt, createdBy, modifiedAt, modifiedBy);
    }
}
