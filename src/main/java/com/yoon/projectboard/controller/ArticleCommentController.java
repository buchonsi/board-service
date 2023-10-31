package com.yoon.projectboard.controller;

import com.yoon.projectboard.dto.UserAccountDto;
import com.yoon.projectboard.dto.request.ArticleCommentRequest;
import com.yoon.projectboard.service.ArticleCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/comments")
@Controller
public class ArticleCommentController {

    private final ArticleCommentService articleCommentService;

    @PostMapping("/new")
    public String postNewArticleComment(ArticleCommentRequest articleCommentRequest) {
        //TODO : 인증 정보를 넣어 줘야 함.
        articleCommentService.saveArticleComment(articleCommentRequest.toDto(
                UserAccountDto.of(
                        "yoon",
                        "yoon123",
                        "yoon@naver.com",
                        "Yoon",
                        "memo")
                )
        );
        return "redirect:/articles/" + articleCommentRequest.articleId();
    }

    @PostMapping("{commentId}/delete")
    public String deleteArticleComment(@PathVariable Long commentId, Long articleId) {
        articleCommentService.deleteArticleComment(commentId);
        return "redirect:/articles/" + articleId;
    }
}
