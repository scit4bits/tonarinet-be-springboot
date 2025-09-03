package org.scit4bits.tonarinetserver.service;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.ArticleDTO;
import org.scit4bits.tonarinetserver.dto.BoardWriteRequestDTO;
import org.scit4bits.tonarinetserver.dto.FileAttachmentRequestDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.entity.Article;
import org.scit4bits.tonarinetserver.entity.Board;
import org.scit4bits.tonarinetserver.entity.FileAttachment.FileType;
import org.scit4bits.tonarinetserver.entity.Organization;
import org.scit4bits.tonarinetserver.entity.Tag;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.ArticleRepository;
import org.scit4bits.tonarinetserver.repository.BoardRepository;
import org.scit4bits.tonarinetserver.repository.OrganizationRepository;
import org.scit4bits.tonarinetserver.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    public Article getArticleById(Integer articleId) {
        return articleRepository.findById(articleId).orElse(null);
    }

    public void createArticle(User user, Integer boardId, BoardWriteRequestDTO request, List<MultipartFile> files) {
        Board board = boardRepository.findById(boardId).get();

        if(board.getCountryCode() != null){
            if(!userCountryService.checkUserCountryAccess(user.getId(), board.getCountryCode())) {
                // Handle insufficient permissions
                log.debug("User {} does not have access to country board {}", user.getId(), board.getCountryCode());
                throw new AccessDeniedException("User does not have permission to create articles in this board.");
            }
        }else if(board.getOrgId() != null){
            Organization organization = organizationRepository.findById(board.getOrgId()).get();
            if(!userRoleService.checkUsersRoleInOrg(user, organization, null)) {
                // Handle insufficient permissions
                log.debug("User {} does not have access to organization board {}", user.getId(), organization.getName());
                throw new AccessDeniedException("User does not have permission to create articles in this board.");
            }
            
            if(request.getCategory().equals("notice") && 
             !userRoleService.checkUsersRoleInOrg(user, organization, "admin")) {
                log.debug("User {} does not have access to create notice articles in organization {}", user.getId(), organization.getName());
                throw new AccessDeniedException("User does not have permission to create notice articles in this organization.");
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

        log.debug("Article created with ID: {}", savedArticle.getId());

        // Handle file attachments if necessary
        if (files != null && !files.isEmpty()) {
            log.debug("Handling file attachments for article: {}", savedArticle.getTitle());

            FileAttachmentRequestDTO requestDTO = new FileAttachmentRequestDTO();
            requestDTO.setArticleId(savedArticle.getId());
            requestDTO.setIsPrivate(false); // Default to public
            requestDTO.setType(FileType.ATTACHMENT);

            fileAttachmentService.uploadFiles(files, requestDTO, user);
        }

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            // Handle tags if your Article entity supports it
            // This is a placeholder; implement tag handling as needed
            log.debug("Tags provided: {}", request.getTags());
            for (String tag : request.getTags()) {
                Tag.TagId tagId = Tag.TagId.builder()
                    .tagName(tag)
                    .build();
                Tag tagEntity = new Tag();
                tagEntity.setId(tagId);
                tagEntity.setArticle(savedArticle);
                log.debug("Saving tag: {}", tagEntity);
                tagRepository.save(tagEntity);
            }
        }
    }



    public Article updateArticle(Integer articleId, Article article) {

        return articleRepository.save(article);
    }

    public void deleteArticle(User user, Integer articleId) {
        Article article = articleRepository.findById(articleId).get();
        if(!user.getIsAdmin() && !article.getCreatedById().equals(user.getId())){
            log.debug("User {} does not have permission to delete article {}", user.getId(), articleId);
            throw new AccessDeniedException("User does not have permission to delete this article.");
        }

        articleRepository.delete(article);
    }

    public ArticleDTO readArticle(User user, Integer articleId){
        Article article = articleRepository.findById(articleId).get();
        Board board = article.getBoard();

        if(!user.getIsAdmin()){
            if(board.getOrgId() != null && !userRoleService.checkUsersRoleInOrg(user, board.getOrganization(), null)) {
                log.debug("User {} does not have access to read articles in organization {}", user.getId(), board.getOrganization().getName());
                throw new AccessDeniedException("User does not have permission to read articles in this organization.");
            }
            if(board.getCountryCode() != null && !userCountryService.checkUserCountryAccess(user.getId(), board.getCountryCode())) {
                log.debug("User {} does not have access to read articles in country {}", user.getId(), board.getCountryCode());
                throw new AccessDeniedException("User does not have permission to read articles in this country.");
            }
        }

        if(article.getCategory().equals("counsel")){
            if(!user.getIsAdmin() && !article.getCreatedById().equals(user.getId()) && !userRoleService.checkUsersRoleInOrg(user, board.getOrganization(), "admin")) {
                log.debug("User {} does not have access to read notice/counsel articles in organization {}", user.getId(), board.getOrganization().getName());
                throw new AccessDeniedException("User does not have permission to read notice/counsel articles in this organization.");
            }
        }

        return ArticleDTO.fromEntity(article);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ArticleDTO> getHotArticles(User user, Integer boardId, Integer page, Integer pageSize) {
        log.info("Fetching hot articles for boardId: {}, page: {}, pageSize: {}", boardId, page, pageSize);

        // 기본값 설정
        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // 최신순 정렬
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);

        Page<Article> articlePage = articleRepository.findByBoardIdAndCategoryNotAndLikedByUsersCountGreaterThanEqual(boardId, "counsel", pageable);

        List<ArticleDTO> result = articlePage.getContent().stream()
                .map(ArticleDTO::fromEntity)
                .toList();
        
        log.info("Found {} hot articles out of {} total in board {} (excluding counsel articles)", result.size(), articlePage.getTotalElements(), boardId);
        return new PagedResponse<>(result, pageNum, pageSizeNum, articlePage.getTotalElements(), articlePage.getTotalPages());
    }

    /**
     * Search articles in a specific board, with optional category filtering
     * All filtering is performed at the database level for optimal performance
     */
    @Transactional(readOnly = true)
    public PagedResponse<ArticleDTO> searchArticles(User user, Integer boardId, String searchBy, String search, String category, Integer page, 
        Integer pageSize, String sortBy, String sortDirection) {

        log.info("Searching articles with boardId: {}, searchBy: {}, search: {}, category: {}, page: {}, pageSize: {}, sortBy: {}, sortDirection: {}",
                boardId, searchBy, search, category, page, pageSize, sortBy, sortDirection);
        
        // 기본값 설정
        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        String direction = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";
        
        // 정렬 방향 설정
        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        // sortBy 필드명 매핑
        String entityFieldName;
        switch (sortByField.toLowerCase()) {
            case "id":
                entityFieldName = "id";
                break;
            case "title":
                entityFieldName = "title";
                break;
            case "category":
                entityFieldName = "category";
                break;
            case "created":
                entityFieldName = "createdAt";
                break;
            case "updated":
                entityFieldName = "updatedAt";
                break;
            case "creator":
                entityFieldName = "createdById";
                break;
            default:
                entityFieldName = "id";
                break;
        }
        
        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);
        
        Page<Article> articlePage;
        
        // Category filtering logic
        boolean shouldFilterByCategory = category != null && !category.trim().isEmpty() && !category.equals("all");
        String categoryFilter = (category != null && shouldFilterByCategory) ? category.trim() : null;
        
        if (search == null || search.trim().isEmpty()) {
            // 검색어가 없으면 특정 게시판의 모든 게시글 조회
            if (shouldFilterByCategory) {
                // 특정 카테고리만 조회
                articlePage = articleRepository.findByBoardIdAndCategory(boardId, categoryFilter, pageable);
            } else {
                // 모든 게시글 조회 (counsel 카테고리 제외)
                articlePage = articleRepository.findByBoardIdAndCategoryNot(boardId, "counsel", pageable);
            }
        } else {
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
                        // ID 검색의 경우 단일 결과를 페이지로 변환
                        Article article = articleRepository.findById(searchId).orElse(null);
                        if (article != null && article.getBoardId().equals(boardId)) {
                            // 카테고리 필터링 적용
                            if (shouldFilterByCategory) {
                                if (article.getCategory().equals(categoryFilter)) {
                                    articlePage = new org.springframework.data.domain.PageImpl<>(
                                        List.of(article), pageable, 1);
                                } else {
                                    articlePage = Page.empty(pageable);
                                }
                            } else if (!article.getCategory().equals("counsel")) {
                                articlePage = new org.springframework.data.domain.PageImpl<>(
                                    List.of(article), pageable, 1);
                            } else {
                                articlePage = Page.empty(pageable);
                            }
                        } else {
                            articlePage = Page.empty(pageable);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Invalid ID format for search: {}", search);
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
                    // 카테고리 검색의 경우, category 파라미터가 있으면 해당 카테고리 내에서만 검색
                    if (shouldFilterByCategory) {
                        if (categoryFilter != null && categoryFilter.toLowerCase().contains(search.trim().toLowerCase())) {
                            articlePage = articleRepository.findByBoardIdAndCategory(boardId, categoryFilter, pageable);
                        } else {
                            articlePage = Page.empty(pageable);
                        }
                    } else {
                        // 카테고리 검색에서 "counsel" 검색을 방지
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
                        log.warn("Invalid creator ID format for search: {}", search);
                        articlePage = Page.empty(pageable);
                    }
                    break;
                default:
                    log.warn("Unknown searchBy parameter: {}. Using 'all' as default.", searchBy);
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
            String.format(" (filtered by category: %s)", categoryFilter) : 
            " (excluding counsel articles)";
        log.info("Found {} articles out of {} total in board {}{}", result.size(), articlePage.getTotalElements(), boardId, filterInfo);
        return new PagedResponse<>(result, pageNum, pageSizeNum, articlePage.getTotalElements(), articlePage.getTotalPages());
    }

    public boolean increaseArticleViews(User user, Integer articleId) {
        Article article = articleRepository.findById(articleId).get();

        article.setViews(article.getViews() + 1);
        articleRepository.save(article);
        return true;
    }
}
