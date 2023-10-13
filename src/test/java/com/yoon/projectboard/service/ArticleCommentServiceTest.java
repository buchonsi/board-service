package com.yoon.projectboard.service;

import com.yoon.projectboard.domain.Article;
import com.yoon.projectboard.domain.ArticleComment;
import com.yoon.projectboard.dto.ArticleCommentDto;
import com.yoon.projectboard.repository.ArticleCommentRepository;
import com.yoon.projectboard.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("비지니스 로직 - 댓글")
@ExtendWith(MockitoExtension.class)
class ArticleCommentServiceTest {

    @InjectMocks
    private ArticleCommentService sut;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private ArticleCommentRepository articleCommentRepository;

    @DisplayName("게시글 ID로 조회하면, 해당하는 댓글 리스트를 반환한다.")
    @Test
    void givenSearchingArticleId_whenSearchingComments_thenReturnsComments() {
        //given
        Long articleId = 1L;
        given(articleRepository.findById(articleId)).willReturn(Optional.of(
                Article.of("title", "content", "#java"))
        );

        //when
        List<ArticleCommentDto> articleComments = sut.searchArticleComment(articleId);

        //then
        assertThat(articleComments).isNotNull();
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("댓글 정보를 입력하면, 댓글을 저장한다.")
    @Test
    void givenArticleCommentInfo_whenSavingArticleComment_thenSavesArticleComment() {
        //given
        ArticleCommentDto articleCommentDto = ArticleCommentDto.of("content", LocalDateTime.now(), "Yoon", LocalDateTime.now(), "Yoon");
        given(articleCommentRepository.save(any(ArticleComment.class))).willReturn(null);

        //when
        sut.saveArticleComment(articleCommentDto);

        //then
        then(articleCommentRepository).should().save(any(ArticleComment.class));
    }

    @DisplayName("댓글 ID와 수정 정보를 입력하면, 댓글을 수정한다.")
    @Test
    void givenArticleCommentIdAndInfo_whenUpdatingArticleComment_thenUpdatesArticleComment() {
        //given
        given(articleCommentRepository.save(any(ArticleComment.class))).willReturn(null);

        //when
        sut.updateArticleComment(1L, "content");

        //then
        then(articleCommentRepository).should().save(any(ArticleComment.class));
    }

    @DisplayName("댓글 ID를 입력 하면, 댓글을 삭제 한다.")
    @Test
    void givenArticleCommentId_whenDeletingArticleComment_thenDeletesArticleComment() {
        //given
        willDoNothing().given(articleCommentRepository).delete(any(ArticleComment.class));

        //when
        sut.deleteArticleComment(1L);

        //then
        then(articleCommentRepository).should().delete(any(ArticleComment.class));
    }
}
