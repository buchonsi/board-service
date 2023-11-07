package com.yoon.projectboard.service;

import com.yoon.projectboard.domain.Article;
import com.yoon.projectboard.domain.Hashtag;
import com.yoon.projectboard.domain.UserAccount;
import com.yoon.projectboard.domain.constant.SearchType;
import com.yoon.projectboard.dto.ArticleDto;
import com.yoon.projectboard.dto.ArticleWithCommentsDto;
import com.yoon.projectboard.dto.HashtagDto;
import com.yoon.projectboard.dto.UserAccountDto;
import com.yoon.projectboard.repository.ArticleRepository;
import com.yoon.projectboard.repository.HashtagRepository;
import com.yoon.projectboard.repository.UserAccountRepository;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("비지니스 로직 - 게시글")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @InjectMocks
    private ArticleService sut;

    @Mock
    private HashtagService hashtagService;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private HashtagRepository hashtagRepository;

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
        then(hashtagRepository).shouldHaveNoInteractions();
        then(articleRepository).shouldHaveNoInteractions();
    }

    @DisplayName("없는 해시태그를 검색하면, 빈 페이지를 반환한다.")
    @Test
    void givenNonexistentHashtag_whenSearchingArticlesViaHashtag_thenReturnsEmptyPage() {
        //given
        String hashtagName = "없는 해시태그";
        Pageable pageable = Pageable.ofSize(30);
        given(articleRepository.findByHashtagNames(List.of(hashtagName), pageable)).willReturn(new PageImpl<>(List.of(), pageable, 0));

        //when
        Page<ArticleDto> articles = sut.searchArticlesViaHashtag(hashtagName, pageable);

        //then
        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(articleRepository).should().findByHashtagNames(List.of(hashtagName), pageable);
    }

    @DisplayName("게시글을 해시태그 검색하면, 게시글 페이지를 반환한다.")
    @Test
    void givenHashtag_whenSearchingArticlesViaHashtag_thenReturnsArticlesPage() {
        //given
        String hashtagName = "java";
        Pageable pageable = Pageable.ofSize(20);
        Article expectedArticle = createArticle();
        given(articleRepository.findByHashtagNames(List.of(hashtagName), pageable)).willReturn(new PageImpl<>(List.of(expectedArticle), pageable, 1));

        //when
        Page<ArticleDto> articles = sut.searchArticlesViaHashtag(hashtagName, pageable);

        //then
        assertThat(articles).isEqualTo(new PageImpl<>(List.of(ArticleDto.from(expectedArticle)), pageable, 1));
        then(articleRepository).should().findByHashtagNames(List.of(hashtagName), pageable);
    }

    @DisplayName("게시글 ID로 조회 하면, 댓글 달린 게시글을 반환한다.")
    @Test
    void givenArticleId_whenSearchingArticleWithComments_thenReturnsArticleWithComments() {
        //given
        Long articleId = 1L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        //when
        ArticleWithCommentsDto articleWithCommentsDto = sut.getArticleWithComments(articleId);

        //then
        assertThat(articleWithCommentsDto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtagDtos", article.getHashtags().stream()
                        .map(HashtagDto::from)
                        .collect(Collectors.toUnmodifiableSet())
                );
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("댓글 달린 게시글이 없으면, 예외를 던진다")
    @Test
    void givenNonexistentArticleId_whenSearchingArticleWithComments_thenThrowsException() {
        //given
        Long articleId = 0L;
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());

        //when
        Throwable t = catchThrowable(() -> sut.getArticleWithComments(articleId));

        //then
        assertThat(t)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("게시글이 없습니다 - articleId: " + articleId);
        then(articleRepository).should().findById(articleId);
    }


    @DisplayName("게시글을 조회하면, 게시글을 반환한다.")
    @Test
    void givenArticleId_whenSearchingArticle_thenReturnsArticle() {
        //given
        Long articleId = 1L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        //when
        ArticleDto articleDto = sut.getArticle(articleId);

        //then
        assertThat(articleDto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtagDtos", article.getHashtags().stream()
                        .map(HashtagDto::from)
                        .collect(Collectors.toUnmodifiableSet())
                );
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("게시글이 없으면, 예외를 던진다.")
    @Test
    void givenNonexistentArticleId_whenSearchingArticle_thenThrowsException() {
        //given
        Long articleId = 0L;
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());

        //when
        Throwable throwable = catchThrowable(() -> sut.getArticleWithComments(articleId));

        //then
        assertThat(throwable)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("게시글이 없습니다 - articleId: " + articleId);
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("게시글 정보를 입력하면, 본문에서 해시태그 정보를 추출하여 해시태그 정보가 포함된 게시글을 생성한다.")
    @Test
    void givenArticleInfo_whenSavingArticle_thenExtractsHashtagsFromContentAndSavesArticleWithExtractedHashtags() {
        //given

        ArticleDto articleDto = createArticleDto();
        Set<String> expectedHashtagNames = Set.of("java", "spring");
        Set<Hashtag> expectedHashtags = new HashSet<>();
        expectedHashtags.add(createHashtag("java"));

        given(userAccountRepository.getReferenceById(articleDto.userAccountDto().userId())).willReturn(createUserAccount());
        given(hashtagService.parseHashtagNames(articleDto.content())).willReturn(expectedHashtagNames);
        given(hashtagService.findHashtagsByNames(expectedHashtagNames)).willReturn(expectedHashtags);
        given(articleRepository.save(any(Article.class))).willReturn(createArticle());

        //when
        sut.saveArticle(articleDto);

        //then
        then(userAccountRepository).should().getReferenceById(articleDto.userAccountDto().userId());
        then(hashtagService).should().parseHashtagNames(articleDto.content());
        then(hashtagService).should().findHashtagsByNames(expectedHashtagNames);
        then(articleRepository).should().save(any(Article.class));
    }

    @DisplayName("게시글의 수정 정보를 입력하면, 게시글을 수정한다.")
    @Test
    void givenModifiedArticleInfo_whenUpdatingArticle_thenUpdatesArticle() {
        //given
        Article article = createArticle();
        ArticleDto articleDto = createArticleDto("new title", "new content");
        Set<String> expectedHashtagNames = Set.of("springboot");
        Set<Hashtag> expectedHashtags = new HashSet<>();

        given(articleRepository.getReferenceById(articleDto.id())).willReturn(article);
        given(userAccountRepository.getReferenceById(articleDto.userAccountDto().userId())).willReturn(articleDto.userAccountDto().toEntity());
        willDoNothing().given(articleRepository).flush();
        willDoNothing().given(hashtagService).deleteHashtagWithoutArticles(any());
        given(hashtagService.parseHashtagNames(articleDto.content())).willReturn(expectedHashtagNames);
        given(hashtagService.findHashtagsByNames(expectedHashtagNames)).willReturn(expectedHashtags);

        //when
        sut.updateArticle(articleDto.id(), articleDto);

        //then
        assertThat(article)
                .hasFieldOrPropertyWithValue("title", articleDto.title())
                .hasFieldOrPropertyWithValue("content", articleDto.content())
                .extracting("hashtags", as(InstanceOfAssertFactories.COLLECTION))
                .hasSize(1)
                .extracting("hashtagName")
                .containsExactly("springboot");
        then(articleRepository).should().getReferenceById(articleDto.id());
        then(userAccountRepository).should().getReferenceById(articleDto.userAccountDto().userId());
        then(articleRepository).should().flush();
        then(hashtagService).should(times(2)).deleteHashtagWithoutArticles(any());
        then(hashtagService).should().parseHashtagNames(articleDto.content());
        then(hashtagService).should().findHashtagsByNames(expectedHashtagNames);
    }

    @DisplayName("없는 게시글의 수정 정보를 입력하면, 경고 로그를 찍고 아무 것도 하지 않는다.")
    @Test
    void givenNonexistentArticleInfo_whenUpdatingArticle_thenLogsWarningAndDoesNothing() {
        //given
        ArticleDto articleDto = createArticleDto("new title", "new content");
        given(articleRepository.getReferenceById(articleDto.id())).willThrow(EntityNotFoundException.class);

        //when
        sut.updateArticle(articleDto.id(), articleDto);

        //then
        then(articleRepository).should().getReferenceById(articleDto.id());
        then(userAccountRepository).shouldHaveNoInteractions();
        then(hashtagService).shouldHaveNoInteractions();
    }

    @DisplayName("게시글 작성자가 아닌 사람이 수정 정보를 입력하면, 아무 것도 하지 않는다.")
    @Test
    void givenModifiedArticleInfoWithDifferentUser_whenUpdatingArticle_thenDoesNothing() {
        //given
        Long differentArticleId = 22L;
        Article differentArticle = createArticle(differentArticleId);
        differentArticle.setUserAccount(createUserAccount("Kim"));
        ArticleDto articleDto = createArticleDto("new title", "new content");

        given(articleRepository.getReferenceById(differentArticleId)).willReturn(differentArticle);
        given(userAccountRepository.getReferenceById(articleDto.userAccountDto().userId())).willReturn(articleDto.userAccountDto().toEntity());

        //when
        sut.updateArticle(differentArticleId, articleDto);

        //then
        then(articleRepository).should().getReferenceById(differentArticleId);
        then(userAccountRepository).should().getReferenceById(articleDto.userAccountDto().userId());
        then(hashtagService).shouldHaveNoInteractions();
    }

    @DisplayName("게시글의 ID를 입력하면, 게시글을 삭제한다.")
    @Test
    void givenArticleId_whenDeletingArticle_thenDeletesArticle() {
        //given
        Long articleId = 1L;
        String userId = "yoon";

        given(articleRepository.getReferenceById(articleId)).willReturn(createArticle());
        willDoNothing().given(articleRepository).deleteByIdAndUserAccount_UserId(articleId, userId);
        willDoNothing().given(articleRepository).flush();
        willDoNothing().given(hashtagService).deleteHashtagWithoutArticles(any());

        //when
        sut.deleteArticle(1L, userId);

        //then
        then(articleRepository).should().getReferenceById(articleId);
        then(articleRepository).should().deleteByIdAndUserAccount_UserId(articleId, userId);
        then(articleRepository).should().flush();
        then(hashtagService).should().deleteHashtagWithoutArticles(any());
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
        Article article = createArticle();
        List<String> expectedHashtags = List.of("java", "spring", "boot");
        given(hashtagRepository.findAllHashtagNames()).willReturn(expectedHashtags);

        //when
        List<String> actualHashtag = sut.getHashtag();

        //then
        assertThat(actualHashtag).isEqualTo(expectedHashtags);
        then(hashtagRepository).should().findAllHashtagNames();
    }

    private Article createArticle() {
        return createArticle(1L);
    }

    private Article createArticle(Long articleId) {
        Article article = Article.of(
                createUserAccount(),
                "title",
                "content"
        );
        article.addHashtags(Set.of(
                createHashtag(1L, "java"),
                createHashtag(2L, "spring")
        ));
        ReflectionTestUtils.setField(article, "id", articleId);

        return article;
    }

    private Hashtag createHashtag(String hashtagName) {
        return createHashtag(1L, hashtagName);
    }

    private Hashtag createHashtag(Long hashtagId, String hashtagName) {
        Hashtag hashtag = Hashtag.of(hashtagName);
        ReflectionTestUtils.setField(hashtag, "id", hashtagId);

        return hashtag;
    }

    private HashtagDto createHashtagDto() {
        return HashtagDto.of("java");
    }

    private ArticleDto createArticleDto() {
        return createArticleDto("title", "content");
    }

    private ArticleDto createArticleDto(String title, String content) {
        return ArticleDto.of(
                1L,
                createUserAccountDto(),
                title,
                content,
                null,
                LocalDateTime.now(),
                "yoon",
                LocalDateTime.now(),
                "yoon"
        );
    }

    private UserAccount createUserAccount() {
        return createUserAccount("yoontest");
    }

    private UserAccount createUserAccount(String userId) {
        return UserAccount.of(
                userId,
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
