package com.yoon.projectboard.service;

import com.yoon.projectboard.domain.Article;
import com.yoon.projectboard.domain.ArticleComment;
import com.yoon.projectboard.domain.Hashtag;
import com.yoon.projectboard.domain.UserAccount;
import com.yoon.projectboard.dto.ArticleCommentDto;
import com.yoon.projectboard.dto.UserAccountDto;
import com.yoon.projectboard.repository.ArticleCommentRepository;
import com.yoon.projectboard.repository.ArticleRepository;
import com.yoon.projectboard.repository.UserAccountRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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
    @Mock
    private UserAccountRepository userAccountRepository;

    @DisplayName("게시글 ID로 조회하면, 해당하는 댓글 리스트를 반환한다.")
    @Test
    void givenSearchingArticleId_whenSearchingComments_thenReturnsComments() {
        //given
        Long articleId = 1L;
        ArticleComment expectedParentComment = createArticleComment(1L, "parent content");
        ArticleComment expectedChildComment = createArticleComment(2L, "child content");
        expectedChildComment.setParentCommentId(expectedParentComment.getId());
        given(articleCommentRepository.findByArticle_Id(articleId)).willReturn(List.of(
                expectedParentComment,
                expectedChildComment
        ));

        //when
        List<ArticleCommentDto> actual = sut.searchArticleComments(articleId);
        System.out.println(actual);
        //then
        assertThat(actual).hasSize(2)
                .extracting("id", "articleId", "parentCommentId", "content")
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L, null, "parent content"),
                        tuple(2L, 1L, 1L, "child content")
                );
        then(articleCommentRepository).should().findByArticle_Id(articleId);
    }

    @DisplayName("댓글 정보를 입력하면, 댓글을 저장한다.")
    @Test
    void givenArticleCommentInfo_whenSavingArticleComment_thenSavesArticleComment() {
        //given
        ArticleCommentDto articleCommentDto = createArticleCommentDto("댓글");
        given(articleRepository.getReferenceById(articleCommentDto.articleId())).willReturn(createArticle());
        given(userAccountRepository.getReferenceById(articleCommentDto.userAccountDto().userId())).willReturn(createUserAccount());
        given(articleCommentRepository.save(any(ArticleComment.class))).willReturn(null);

        //when
        sut.saveArticleComment(articleCommentDto);

        //then
        then(articleRepository).should().getReferenceById(articleCommentDto.articleId());
        then(userAccountRepository).should().getReferenceById(articleCommentDto.userAccountDto().userId());
        then(articleCommentRepository).should(never()).getReferenceById(anyLong());
        then(articleCommentRepository).should().save(any(ArticleComment.class));
    }

    @DisplayName("댓글 저장을 시도했는데 맞는 게시글이 없으면, 경고 로그를 찍고 아무것도 안한다.")
    @Test
    void givenNonexistentArticle_whenSavingArticleComment_thenLogsSituationAndDoesNothing() {
        //given
        ArticleCommentDto articleCommentDto = createArticleCommentDto("댓글");
        given(articleRepository.getReferenceById(articleCommentDto.articleId())).willThrow(EntityNotFoundException.class);

        //when
        sut.saveArticleComment(articleCommentDto);

        //then
        then(articleRepository).should().getReferenceById(articleCommentDto.articleId());
        then(userAccountRepository).shouldHaveNoInteractions();
        then(articleCommentRepository).shouldHaveNoInteractions();
    }

    @DisplayName("부모 댓글 ID와 댓글 정보를 입력하면, 대댓글을 저장한다.")
    @Test
    void givenParentCommentIdAndArticleCommentInfo_whenSaving_thenSavesChildComment() {
        //given
        Long parentCommentId = 1L;
        ArticleComment parent = createArticleComment(parentCommentId, "댓글");
        ArticleCommentDto child = createArticleCommentDto(parentCommentId, "대댓글");
        given(articleRepository.getReferenceById(child.articleId())).willReturn(createArticle());
        given(userAccountRepository.getReferenceById(child.userAccountDto().userId())).willReturn(createUserAccount());
        given(articleCommentRepository.getReferenceById(child.parentCommentId())).willReturn(parent);

        //when
        sut.saveArticleComment(child);

        //then
        assertThat(child.parentCommentId()).isNotNull();
        then(articleRepository).should().getReferenceById(child.articleId());
        then(userAccountRepository).should().getReferenceById(child.userAccountDto().userId());
        then(articleCommentRepository).should().getReferenceById(child.parentCommentId());
        then(articleCommentRepository).should(never()).save(any(ArticleComment.class));
    }

    @Disabled("기능 삭제로 인하여 테스트 삭제")
    @DisplayName("댓글 정보를 입력하면, 댓글을 수정한다.")
    @Test
    void givenArticleCommentInfo_whenUpdatingArticleComment_thenUpdatesArticleComment() {
        //given
        String oldContent = "content";
        String updatedContent = "댓글";
        ArticleComment articleComment = createArticleComment(1L, oldContent);
        ArticleCommentDto articleCommentDto = createArticleCommentDto(updatedContent);
        given(articleCommentRepository.getReferenceById(articleCommentDto.id())).willReturn(articleComment);

        //when
        sut.updateArticleComment(articleCommentDto);

        //then
        assertThat(articleComment.getContent())
                .isNotEqualTo(oldContent)
                .isEqualTo(updatedContent);
        then(articleCommentRepository).should().getReferenceById(articleCommentDto.id());
    }

    @Disabled("기능 삭제로 인하여 테스트 삭제")
    @DisplayName("없는 댓글 정보를 수정하려고 하면, 경고 로그를 찍고 아무 것도 안 한다.")
    @Test
    void givenNonexistentArticleComment_whenUpdatingArticleComment_thenLogsWarningAndDoesNothing() {
        //given
        ArticleCommentDto articleCommentDto = createArticleCommentDto("댓글");
        given(articleCommentRepository.getReferenceById(articleCommentDto.id())).willThrow(EntityNotFoundException.class);

        //when
        sut.updateArticleComment(articleCommentDto);

        //then
        then(articleCommentRepository).should().getReferenceById(articleCommentDto.id());
    }

    @DisplayName("댓글 ID를 입력 하면, 댓글을 삭제 한다.")
    @Test
    void givenArticleCommentId_whenDeletingArticleComment_thenDeletesArticleComment() {
        //given
        Long articleCommentId = 1L;
        String userId = "yoon";
        willDoNothing().given(articleCommentRepository).deleteByIdAndUserAccount_UserId(articleCommentId, userId);

        //when
        sut.deleteArticleComment(articleCommentId, userId);

        //then
        then(articleCommentRepository).should().deleteByIdAndUserAccount_UserId(articleCommentId, userId);
    }

    private ArticleCommentDto createArticleCommentDto(String comment) {
        return createArticleCommentDto(null, comment);
    }

    private ArticleCommentDto createArticleCommentDto(Long parentCommentId, String comment) {
        return createArticleCommentDto(1L, parentCommentId, comment);
    }

    private ArticleCommentDto createArticleCommentDto(Long id, Long parentCommentId, String content) {
        return ArticleCommentDto.of(
                id,
                1L,
                createUserAccountDto(),
                parentCommentId,
                content,
                LocalDateTime.now(),
                "yoon",
                LocalDateTime.now(),
                "yoon"
        );
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "yoontest",
                "password",
                "yoontest@naver.com",
                "yoon",
                "yoon's memo",
                LocalDateTime.now(),
                "yoon",
                LocalDateTime.now(),
                "yoon"
        );
    }

    private ArticleComment createArticleComment(Long id, String content) {
        ArticleComment articleComment = ArticleComment.of(
                createArticle(),
                createUserAccount(),
                content
        );
        ReflectionTestUtils.setField(articleComment, "id", id);

        return articleComment;
    }

    private UserAccount createUserAccount() {
        return UserAccount.of(
                "yoontest",
                "password",
                "yoontest@naver.com",
                "yoon",
                null
        );
    }

    private Article createArticle() {
        Article article = Article.of(
                createUserAccount(),
                "title",
                "article_content"
        );
        ReflectionTestUtils.setField(article, "id", 1L);
        article.addHashtags(Set.of(createHashtag(article)));

        return article;
    }

    private Hashtag createHashtag(Article article) {
        return Hashtag.of("java");
    }
}
