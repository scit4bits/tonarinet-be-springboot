package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.ArticleDTO;
import org.scit4bits.tonarinetserver.dto.BoardWriteRequestDTO;
import org.scit4bits.tonarinetserver.dto.FileAttachmentRequestDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.entity.*;
import org.scit4bits.tonarinetserver.entity.FileAttachment.FileType;
import org.scit4bits.tonarinetserver.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 게시글 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;
    private final UserCountryService userCountryService;
    private final UserRoleService userRoleService;
    private final OrganizationRepository organizationRepository;
    private final TagRepository tagRepository;
    private final FileAttachmentService fileAttachmentService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * ID로 특정 게시글을 조회합니다.
     * @param articleId 조회할 게시글 ID
     * @return Article 엔티티
     */
    public Article getArticleById(Integer articleId) {
        return articleRepository.findById(articleId).orElse(null);
    }

    /**
     * 새로운 게시글을 생성합니다.
     * @param user 작성자 정보
     * @param boardId 게시판 ID
     * @param request 게시글 작성 요청 정보
     * @param files 첨부 파일 리스트
     */
    public void createArticle(User user, Integer boardId, BoardWriteRequestDTO request, List<MultipartFile> files) {
        Board board = boardRepository.findById(boardId).get();

        // 국가별 게시판 접근 권한 확인
        if (board.getCountryCode() != null) {
            if (!userCountryService.checkUserCountryAccess(user.getId(), board.getCountryCode())) {
                log.debug("사용자 {}가 국가 게시판 {}에 접근할 수 없습니다.", user.getId(), board.getCountryCode());
                throw new AccessDeniedException("이 게시판에 게시글을 작성할 권한이 없습니다.");
            }
        // 조직별 게시판 접근 권한 확인
        } else if (board.getOrgId() != null) {
            Organization organization = organizationRepository.findById(board.getOrgId()).get();
            if (!userRoleService.checkUsersRoleInOrg(user, organization, null)) {
                log.debug("사용자 {}가 조직 게시판 {}에 접근할 수 없습니다.", user.getId(), organization.getName());
                throw new AccessDeniedException("이 게시판에 게시글을 작성할 권한이 없습니다.");
            }

            // 공지사항 작성 권한 확인 (조직 관리자만 가능)
            if (request.getCategory().equals("notice") &&
                    !userRoleService.checkUsersRoleInOrg(user, organization, "admin")) {
                log.debug("사용자 {}가 조직 {}에 공지사항을 작성할 수 없습니다.", user.getId(), organization.getName());
                throw new AccessDeniedException("이 조직에 공지사항을 작성할 권한이 없습니다.");
            }
        }

        Article article = Article.builder()
                .category(request.getCategory())
                .title(request.getTitle())
                .contents(request.getContent())
                .createdById(user.getId())
                .boardId(boardId)
                .build();

        Article savedArticle = articleRepository.save(article);

        log.debug("게시글 생성 완료, ID: {}", savedArticle.getId());

        // 첨부 파일 처리
        if (files != null && !files.isEmpty()) {
            log.debug("게시글에 대한 첨부 파일 처리: {}", savedArticle.getTitle());

            FileAttachmentRequestDTO requestDTO = new FileAttachmentRequestDTO();
            requestDTO.setArticleId(savedArticle.getId());
            requestDTO.setIsPrivate(false); // 기본값은 공개
            requestDTO.setType(FileType.ATTACHMENT);

            fileAttachmentService.uploadFiles(files, requestDTO, user);
        }

        // 태그 처리
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            log.debug("제공된 태그: {}", request.getTags());
            for (String tag : request.getTags()) {
                Tag.TagId tagId = Tag.TagId.builder()
                        .tagName(tag)
                        .build();
                Tag tagEntity = new Tag();
                tagEntity.setId(tagId);
                tagEntity.setArticle(savedArticle);
                log.debug("태그 저장: {}", tagEntity);
                tagRepository.save(tagEntity);
            }
        }

        log.debug("게시글 카테고리: {}", savedArticle.getCategory());

        // 공지사항인 경우 알림 발송
        if (savedArticle.getCategory().equals("notice")) {
            log.debug("공지사항 게시글에 대한 알림 발송: {}", savedArticle.getTitle());
            if (board.getId() == 0) {
                // 전체 공지
                log.debug("전체 공지, 모든 사용자에게 알림");
                List<User> allUsers = userRepository.findAll();
                for (User u : allUsers) {
                    notificationService.addNotification(u.getId(), "{\"messageType\": \"newNotice\"}", "/board/view/" + savedArticle.getId());
                }
            } else if (board.getOrgId() != null) {
                // 조직 공지
                log.debug("조직 공지, 조직 {}의 모든 사용자에게 알림", board.getOrgId());
                Organization organization = organizationRepository.findById(board.getOrgId()).get();
                List<User> orgUsers = organization.getUsers();
                for (User u : orgUsers) {
                    notificationService.addNotification(u.getId(), "{\"messageType\": \"newOrgNotice\"}", "/board/view/" + savedArticle.getId());
                }
            }
        }
    }

    /**
     * 게시글을 수정합니다.
     * @param articleId 수정할 게시글 ID
     * @param article 수정할 내용이 담긴 Article 엔티티
     * @return 수정된 Article 엔티티
     */
    public Article updateArticle(Integer articleId, Article article) {
        return articleRepository.save(article);
    }

    /**
     * 게시글을 삭제합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param articleId 삭제할 게시글 ID
     */
    public void deleteArticle(User user, Integer articleId) {
        Article article = articleRepository.findById(articleId).get();
        // 관리자 또는 작성자만 삭제 가능
        if (!user.getIsAdmin() && !article.getCreatedById().equals(user.getId())) {
            log.debug("사용자 {}가 게시글 {}을(를) 삭제할 권한이 없습니다.", user.getId(), articleId);
            throw new AccessDeniedException("이 게시글을 삭제할 권한이 없습니다.");
        }

        articleRepository.delete(article);
    }

    /**
     * 게시글을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param articleId 조회할 게시글 ID
     * @return ArticleDTO
     */
    public ArticleDTO readArticle(User user, Integer articleId) {
        Article article = articleRepository.findById(articleId).get();
        Board board = article.getBoard();

        // 관리자가 아닌 경우 접근 권한 확인
        if (!user.getIsAdmin()) {
            if (board.getOrgId() != null && !userRoleService.checkUsersRoleInOrg(user, board.getOrganization(), null)) {
                log.debug("사용자 {}가 조직 {}의 게시글을 읽을 수 없습니다.", user.getId(), board.getOrganization().getName());
                throw new AccessDeniedException("이 조직의 게시글을 읽을 권한이 없습니다.");
            }
            if (board.getCountryCode() != null && !userCountryService.checkUserCountryAccess(user.getId(), board.getCountryCode())) {
                log.debug("사용자 {}가 국가 {}의 게시글을 읽을 수 없습니다.", user.getId(), board.getCountryCode());
                throw new AccessDeniedException("이 국가의 게시글을 읽을 권한이 없습니다.");
            }
        }

        // 상담 게시글인 경우 추가 권한 확인
        if (article.getCategory().equals("counsel")) {
            if (!user.getIsAdmin() && !article.getCreatedById().equals(user.getId()) && !userRoleService.checkUsersRoleInOrg(user, board.getOrganization(), "admin")) {
                log.debug("사용자 {}가 조직 {}의 상담/공지 게시글을 읽을 수 없습니다.", user.getId(), board.getOrganization().getName());
                throw new AccessDeniedException("이 조직의 상담/공지 게시글을 읽을 권한이 없습니다.");
            }
        }

        return ArticleDTO.fromEntity(article);
    }

    /**
     * 인기 게시글 목록을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param boardId 게시판 ID
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @return 페이징 처리된 ArticleDTO 리스트
     */
    @Transactional(readOnly = true)
    public PagedResponse<ArticleDTO> getHotArticles(User user, Integer boardId, Integer page, Integer pageSize) {
        log.info("인기 게시글 조회 - 게시판 ID: {}, 페이지: {}, 페이지 크기: {}", boardId, page, pageSize);

        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // 최신순 정렬
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);

        // 'counsel' 카테고리를 제외하고 '좋아요' 수가 일정 기준 이상인 게시글 조회
        Page<Article> articlePage = articleRepository.findByBoardIdAndCategoryNotAndLikedByUsersCountGreaterThanEqual(boardId, "counsel", pageable);

        List<ArticleDTO> result = articlePage.getContent().stream()
                .map(ArticleDTO::fromEntity)
                .toList();

        log.info("게시판 {}에서 총 {}개의 인기 게시글 중 {}개를 찾았습니다. (상담 게시글 제외)", boardId, articlePage.getTotalElements(), result.size());
        return new PagedResponse<>(result, pageNum, pageSizeNum, articlePage.getTotalElements(), articlePage.getTotalPages());
    }

    /**
     * 특정 게시판의 게시글을 검색합니다. (카테고리 필터링 포함)
     * 모든 필터링은 성능 최적화를 위해 데이터베이스 수준에서 수행됩니다.
     * @param user 현재 로그인한 사용자 정보
     * @param boardId 게시판 ID
     * @param searchBy 검색 기준
     * @param search 검색어
     * @param category 카테고리
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 ArticleDTO 리스트
     */
    @Transactional(readOnly = true)
    public PagedResponse<ArticleDTO> searchArticles(User user, Integer boardId, String searchBy, String search, String category, Integer page,
                                                    Integer pageSize, String sortBy, String sortDirection) {

        log.info("게시글 검색 - 게시판 ID: {}, 검색 기준: {}, 검색어: {}, 카테고리: {}, 페이지: {}, 페이지 크기: {}, 정렬: {}:{}.",
                boardId, searchBy, search, category, page, pageSize, sortBy, sortDirection);

        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        String direction = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";

        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // 정렬 필드명 매핑
        String entityFieldName = switch (sortByField.toLowerCase()) {
            case "id" -> "id";
            case "title" -> "title";
            case "category" -> "category";
            case "created" -> "createdAt";
            case "updated" -> "updatedAt";
            case "creator" -> "createdById";
            default -> "id";
        };

        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);

        Page<Article> articlePage;

        // 카테고리 필터링 로직
        boolean shouldFilterByCategory = category != null && !category.trim().isEmpty() && !category.equals("all");
        String categoryFilter = (category != null && shouldFilterByCategory) ? category.trim() : null;

        log.debug("카테고리 필터링 적용 여부: {}, 필터링 카테고리: {}", shouldFilterByCategory, categoryFilter);

        if (search == null || search.trim().isEmpty()) {
            // 검색어가 없는 경우
            if (shouldFilterByCategory) {
                articlePage = articleRepository.findByBoardIdAndCategory(boardId, categoryFilter, pageable);
            } else {
                articlePage = articleRepository.findByBoardIdAndCategoryNot(boardId, "counsel", pageable);
            }
        } else {
            // 검색어가 있는 경우
            switch (searchBy.toLowerCase()) {
                case "all":
                    if (shouldFilterByCategory) {
                        articlePage = articleRepository.findByBoardIdAndCategoryAndAllFieldsContaining(boardId, categoryFilter, search.trim(), pageable);
                    } else {
                        articlePage = articleRepository.findByBoardIdAndAllFieldsContainingExcludingCounsel(boardId, search.trim(), pageable);
                    }
                    break;
                case "id":
                    try {
                        Integer searchId = Integer.parseInt(search.trim());
                        Article article = articleRepository.findById(searchId).orElse(null);
                        if (article != null && article.getBoardId().equals(boardId)) {
                            if (shouldFilterByCategory) {
                                if (article.getCategory().equals(categoryFilter)) {
                                    articlePage = new org.springframework.data.domain.PageImpl<>(List.of(article), pageable, 1);
                                } else {
                                    articlePage = Page.empty(pageable);
                                }
                            } else if (!article.getCategory().equals("counsel")) {
                                articlePage = new org.springframework.data.domain.PageImpl<>(List.of(article), pageable, 1);
                            } else {
                                articlePage = Page.empty(pageable);
                            }
                        } else {
                            articlePage = Page.empty(pageable);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 ID 형식으로 검색: {}", search);
                        articlePage = Page.empty(pageable);
                    }
                    break;
                case "title":
                    if (shouldFilterByCategory) {
                        articlePage = articleRepository.findByBoardIdAndCategoryAndTitleContainingIgnoreCase(boardId, categoryFilter, search.trim(), pageable);
                    } else {
                        articlePage = articleRepository.findByBoardIdAndTitleContainingIgnoreCaseAndCategoryNot(boardId, search.trim(), "counsel", pageable);
                    }
                    break;
                case "contents":
                    if (shouldFilterByCategory) {
                        articlePage = articleRepository.findByBoardIdAndCategoryAndContentsContainingIgnoreCase(boardId, categoryFilter, search.trim(), pageable);
                    } else {
                        articlePage = articleRepository.findByBoardIdAndContentsContainingIgnoreCaseAndCategoryNot(boardId, search.trim(), "counsel", pageable);
                    }
                    break;
                case "category":
                    if (shouldFilterByCategory) {
                        if (categoryFilter != null && categoryFilter.toLowerCase().contains(search.trim().toLowerCase())) {
                            articlePage = articleRepository.findByBoardIdAndCategory(boardId, categoryFilter, pageable);
                        } else {
                            articlePage = Page.empty(pageable);
                        }
                    } else {
                        if ("counsel".equalsIgnoreCase(search.trim())) {
                            articlePage = Page.empty(pageable);
                        } else {
                            articlePage = articleRepository.findByBoardIdAndCategoryContainingIgnoreCaseAndCategoryNot(boardId, search.trim(), "counsel", pageable);
                        }
                    }
                    break;
                case "creator":
                    try {
                        Integer creatorId = Integer.parseInt(search.trim());
                        if (shouldFilterByCategory) {
                            articlePage = articleRepository.findByBoardIdAndCategoryAndCreatedById(boardId, categoryFilter, creatorId, pageable);
                        } else {
                            articlePage = articleRepository.findByBoardIdAndCreatedByIdAndCategoryNot(boardId, creatorId, "counsel", pageable);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 작성자 ID 형식으로 검색: {}", search);
                        articlePage = Page.empty(pageable);
                    }
                    break;
                default:
                    log.warn("알 수 없는 검색 기준: {}. 'all'을 기본값으로 사용합니다.", searchBy);
                    if (shouldFilterByCategory) {
                        articlePage = articleRepository.findByBoardIdAndCategoryAndAllFieldsContaining(boardId, categoryFilter, search.trim(), pageable);
                    } else {
                        articlePage = articleRepository.findByBoardIdAndAllFieldsContainingExcludingCounsel(boardId, search.trim(), pageable);
                    }
                    break;
            }
        }

        List<ArticleDTO> result = articlePage.getContent().stream()
                .map(ArticleDTO::fromEntity)
                .toList();

        String filterInfo = shouldFilterByCategory ?
                String.format(" (카테고리 필터: %s)", categoryFilter) :
                " (상담 게시글 제외)";
        log.info("게시판 {}에서 총 {}개의 게시글 중 {}개를 찾았습니다.{}", boardId, articlePage.getTotalElements(), result.size(), filterInfo);
        return new PagedResponse<>(result, pageNum, pageSizeNum, articlePage.getTotalElements(), articlePage.getTotalPages());
    }

    /**
     * 게시글 조회수를 증가시킵니다.
     * @param user 현재 로그인한 사용자 정보
     * @param articleId 조회수를 증가시킬 게시글 ID
     * @return 성공 여부
     */
    public boolean increaseArticleViews(User user, Integer articleId) {
        Article article = articleRepository.findById(articleId).get();

        article.setViews(article.getViews() + 1);
        articleRepository.save(article);
        return true;
    }
}
