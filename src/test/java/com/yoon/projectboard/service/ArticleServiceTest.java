package com.yoon.projectboard.service;

import com.yoon.projectboard.domain.Article;
import com.yoon.projectboard.domain.UserAccount;
import com.yoon.projectboard.domain.type.SearchType;
import com.yoon.projectboard.dto.ArticleDto;
import com.yoon.projectboard.dto.ArticleWithCommentsDto;
import com.yoon.projectboard.dto.UserAccountDto;
import com.yoon.projectboard.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("비지니스 로직 - 게시글")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @InjectMocks
    private ArticleService sut;

    @Mock
    private ArticleRepository articleRepository;

    @DisplayName("검색어 없이 게시글을 검색하면, 게시글 페이지를 반환한다.")
    @Test
    void givenNoSearchParameters_whenSearchingArticles_thenReturnsArticlePage() {
        //given
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findAll(pageable)).willReturn(Page.empty());

        //when
        Page<ArticleDto> articles = sut.searchArticles(null, null, pageable);

        //then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findAll(pageable);
    }

    @DisplayName("검색어와 함께 게시글을 검색하면, 게시글 페이지를 반환한다.")
    @Test
    void givenSearchParameters_whenSearchingArticles_thenReturnsArticlePage() {
        //given
        SearchType searchType = SearchType.TITLE;
        String keyword = "title";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByTitleContaining(keyword, pageable)).willReturn(Page.empty());

        //when
        Page<ArticleDto> articles = sut.searchArticles(searchType, keyword, pageable);

        //then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findByTitleContaining(keyword, pageable);
    }

    @DisplayName("검색어 없이 게시글을 해시태그 검색하면, 빈 페이지를 반환한다.")
    @Test
    void givenNoSearchParameters_whenSearchingArticlesViaHashtag_thenReturnsEmptyPage() {
        //given
        Pageable pageable = Pageable.ofSize(20);

        //when
        Page<ArticleDto> articles = sut.searchArticlesViaHashtag(null, pageable);

        //then
        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(articleRepository).shouldHaveNoInteractions();
    }

    @DisplayName("게시글을 해시태그 검색하면, 게시글 페이지를 반환한다.")
    @Test
    void givenHashtag_whenSearchingArticlesViaHashtag_thenReturnsArticlesPage() {
        //given
        String hashtag = "#java";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByHashtag(hashtag, pageable)).willReturn(Page.empty(pageable));

        //when
        Page<ArticleDto> articles = sut.searchArticlesViaHashtag(hashtag, pageable);

        //then
        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(articleRepository).should().findByHashtag(hashtag, pageable);
    }

    @DisplayName("게시글을 조회하면, 게시글을 반환한다.")
    @Test
    void givenArticleId_whenSearchingArticle_thenReturnsArticle() {
        //given
        Long articleId = 1L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        //when
        ArticleWithCommentsDto articleWithCommentsDto = sut.getArticle(articleId);

        //then
        assertThat(articleWithCommentsDto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtag", article.getHashtag());
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("없는 게시글을 조회하면, 예외를 던진다.")
    @Test
    void givenNonexistentArticleId_whenSearchingArticle_thenThrowsException() {
        //given
        Long articleId = 0L;
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());

        //when
        Throwable throwable = catchThrowable(() -> sut.getArticle(articleId));

        //then
        assertThat(throwable)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("게시글이 없습니다 - articleId: " + articleId);
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("게시글 정보를 입력하면, 게시글을 생성한다.")
    @Test
    void givenArticleInfo_whenSavingArticle_thenSavesArticle() {
        //given

        ArticleDto articleDto = createArticleDto();
        given(articleRepository.save(any(Article.class))).willReturn(createArticle());

        //when
        sut.saveArticle(articleDto);

        //then
        then(articleRepository).should().save(any(Article.class));
    }

    @DisplayName("게시글의 수정 정보를 입력하면, 게시글을 수정한다.")
    @Test
    void givenModifiedArticleInfo_whenUpdatingArticle_thenUpdatesArticle() {
        //given
        Article article = createArticle();
        ArticleDto articleDto = createArticleDto("new title", "new content", "#SpringBoot");
        given(articleRepository.getReferenceById(articleDto.id())).willReturn(article);

        //when
        sut.updateArticle(articleDto);

        //then
        assertThat(article)
                .hasFieldOrPropertyWithValue("title", articleDto.title())
                .hasFieldOrPropertyWithValue("content", articleDto.content())
                .hasFieldOrPropertyWithValue("hashtag", articleDto.hashtag());
        then(articleRepository).should().getReferenceById(articleDto.id());
    }

    @DisplayName("없는 게시글의 수정 정보를 입력하면, 경고 로그를 찍고 아무 것도 하지 않는다.")
    @Test
    void givenNonexistentArticleInfo_whenUpdatingArticle_thenLogsWarningAndDoesNothing() {
        //given
        ArticleDto articleDto = createArticleDto("new title", "new content", "#SpringBoot");
        given(articleRepository.getReferenceById(articleDto.id())).willThrow(EntityNotFoundException.class);

        //when
        sut.updateArticle(articleDto);

        //then
        then(articleRepository).should().getReferenceById(articleDto.id());
    }

    @DisplayName("게시글의 ID를 입력하면, 게시글을 삭제한다.")
    @Test
    void givenArticleId_whenDeletingArticle_thenDeletesArticle() {
        //given
        Long articleId = 1L;
        willDoNothing().given(articleRepository).deleteById(articleId);

        //when
        sut.deleteArticle(1L);

        //then
        then(articleRepository).should().deleteById(articleId);
    }

    @DisplayName("게시글 수를 조회하면, 게시글 수를 반환한다.")
    @Test
    void givenNothing_whenCountingArticle_thenReturnsArticleCount() {
        //given
        Long expected = 0L;
        given(articleRepository.count()).willReturn(expected);

        //when
        Long actual = sut.getArticleCount();

        //then
        assertThat(actual).isEqualTo(expected);
        then(articleRepository).should().count();
    }

    @DisplayName("해시태그를 조회하면, 유니크 해시태그 리스트를 반환한다.")
    @Test
    void givenNothing_whenSearchingHashtag_thenReturnsHashtags() {
        //given
        List<String> expectedHashtags = List.of("#java", "#spring", "#boot");
        given(articleRepository.findAllDistinctHashtags()).willReturn(expectedHashtags);

        //when
        List<String> actualHashtag = sut.getHashtag();

        //then
        assertThat(actualHashtag).isEqualTo(expectedHashtags);
        then(articleRepository).should().findAllDistinctHashtags();
    }

    private Article createArticle() {
        return Article.of(
                createUserAccount(),
                "title",
                "content",
                "#java"
        );
    }

    private ArticleDto createArticleDto() {
        return createArticleDto("title", "content", "#java");
    }

    private ArticleDto createArticleDto(String title, String content, String hashtag) {
        return ArticleDto.of(
                1L,
                createUserAccountDto(),
                title,
                content,
                hashtag,
                LocalDateTime.now(),
                "yoon",
                LocalDateTime.now(),
                "yoon"
        );
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

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "yoontest",
                "password",
                "yoontest@naver.com",
                "yoon",
                "yoon\'s memo",
                LocalDateTime.now(),
                "yoon",
                LocalDateTime.now(),
                "yoon"
        );
    }
}
