package com.yoon.projectboard.service;

import com.yoon.projectboard.dto.ArticleCommentDto;
import com.yoon.projectboard.repository.ArticleCommentRepository;
import com.yoon.projectboard.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class ArticleCommentService {

    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository articleCommentRepository;

    @Transactional(readOnly = true)
    public List<ArticleCommentDto> searchArticleComment(Long articleId) {
        return List.of();
    }

    public void saveArticleComment(ArticleCommentDto articleCommentDto) {

    }

    public void updateArticleComment(long articleCommentId, String content) {
    }

    public void deleteArticleComment(long articleCommentId) {
    }
}
