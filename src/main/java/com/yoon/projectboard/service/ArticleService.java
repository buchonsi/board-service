package com.yoon.projectboard.service;

import com.yoon.projectboard.domain.type.SearchType;
import com.yoon.projectboard.dto.ArticleDto;
import com.yoon.projectboard.dto.ArticleUpdateDto;
import com.yoon.projectboard.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public Page<ArticleDto> searchArticles(SearchType searchType, String searchKeyword) {
        return Page.empty();
    }

    @Transactional(readOnly = true)
    public ArticleDto searchArticle(long articleId) {
        return null;
    }

    public void saveArticle(ArticleDto articleDto) {

    }

    public void updateArticle(long articleId, ArticleUpdateDto articleUpdateDtoof) {

    }

    public void deleteArticle(long articleId) {
    }
}
