package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.ReplyRequestDTO;
import org.scit4bits.tonarinetserver.dto.ReplyResponseDTO;
import org.scit4bits.tonarinetserver.entity.Reply;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.ArticleRepository;
import org.scit4bits.tonarinetserver.repository.ReplyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 댓글 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final NotificationService notificationService;
    private final ArticleRepository articleRepository;

    /**
     * 새로운 댓글을 생성하고 게시글 작성자에게 알림을 보냅니다.
     * @param request 댓글 생성 요청 정보
     * @param creator 작성자 정보
     * @return 생성된 댓글 정보
     */
    public ReplyResponseDTO createReply(ReplyRequestDTO request, User creator) {
        log.info("게시글 {}에 대한 댓글 생성, 작성자: {}", request.getArticleId(), creator.getId());

        Reply reply = Reply.builder()
                .contents(request.getContents())
                .createdById(creator.getId())
                .articleId(request.getArticleId())
                .build();

        Reply savedReply = replyRepository.save(reply);
        log.info("댓글 생성 완료, ID: {}", savedReply.getId());

        // 게시글 작성자에게 알림 발송
        articleRepository.findById(request.getArticleId()).ifPresent(article -> {
            if (!article.getCreatedById().equals(creator.getId())) {
                notificationService.addNotification(article.getCreatedById(),
                        "{\"messageType\": \"newReplyToArticle\", \"articleTitle\": \"" + article.getTitle() + "\", \"userName\": \"" + creator.getNickname() + "\"}",
                        "/board/view/" + article.getId());
                log.info("게시글 작성자에게 알림 발송: {}", article.getCreatedById());
            } else {
                log.info("댓글 작성자가 게시글 작성자와 동일하여 알림을 보내지 않습니다.");
            }
        });


        return ReplyResponseDTO.fromEntity(savedReply);
    }

    /**
     * 모든 댓글 목록을 조회합니다.
     * @return ReplyResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ReplyResponseDTO> getAllReplies() {
        log.info("모든 댓글 조회");
        return replyRepository.findAll().stream()
                .map(ReplyResponseDTO::fromEntity)
                .toList();
    }

    /**
     * ID로 특정 댓글을 조회합니다.
     * @param id 조회할 댓글 ID
     * @return ReplyResponseDTO
     */
    @Transactional(readOnly = true)
    public ReplyResponseDTO getReplyById(Integer id) {
        log.info("ID로 댓글 조회: {}", id);
        Reply reply = replyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다. ID: " + id));
        return ReplyResponseDTO.fromEntity(reply);
    }

    /**
     * 특정 게시글의 모든 댓글을 조회합니다.
     * @param articleId 게시글 ID
     * @return ReplyResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ReplyResponseDTO> getRepliesByArticleId(Integer articleId) {
        log.info("게시글 {}의 댓글 조회", articleId);
        return replyRepository.findByArticleIdOrderByCreatedAtAsc(articleId).stream()
                .map(ReplyResponseDTO::fromEntity)
                .toList();
    }

    /**
     * 댓글을 수정합니다.
     * @param id 수정할 댓글 ID
     * @param request 댓글 수정 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 수정된 댓글 정보
     */
    public ReplyResponseDTO updateReply(Integer id, ReplyRequestDTO request, User user) {
        log.info("댓글 수정 - ID: {}, 사용자: {}", id, user.getId());

        Reply reply = replyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다. ID: " + id));

        // 사용자가 작성자이거나 관리자인지 확인
        if (!reply.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("댓글 작성자 또는 관리자만 수정할 수 있습니다.");
        }

        reply.setContents(request.getContents() != null && !request.getContents().trim().isEmpty()
                ? request.getContents() : reply.getContents());
        reply.setArticleId(request.getArticleId() != null ? request.getArticleId() : reply.getArticleId());

        Reply savedReply = replyRepository.save(reply);
        log.info("댓글 수정 완료");
        return ReplyResponseDTO.fromEntity(savedReply);
    }

    /**
     * 댓글을 삭제합니다.
     * @param id 삭제할 댓글 ID
     * @param user 현재 로그인한 사용자 정보
     */
    public void deleteReply(Integer id, User user) {
        log.info("댓글 삭제 - ID: {}, 사용자: {}", id, user.getId());

        Reply reply = replyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다. ID: " + id));

        // 사용자가 작성자이거나 관리자인지 확인
        if (!reply.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("댓글 작성자 또는 관리자만 삭제할 수 있습니다.");
        }

        replyRepository.deleteById(id);
        log.info("댓글 삭제 완료");
    }

    /**
     * 댓글을 검색합니다.
     * @param searchBy 검색 기준
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 ReplyResponseDTO
     */
    @Transactional(readOnly = true)
    public PagedResponse<ReplyResponseDTO> searchReplies(String searchBy, String search, Integer page,
                                                         Integer pageSize, String sortBy, String sortDirection) {
        log.info("댓글 검색 - 기준: {}, 검색어: {}, 페이지: {}, 크기: {}, 정렬: {}:{}",
                searchBy, search, page, pageSize, sortBy, sortDirection);

        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        String direction = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";

        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        String entityFieldName = switch (sortByField.toLowerCase()) {
            case "id" -> "id";
            case "created" -> "createdAt";
            case "article" -> "articleId";
            case "creator" -> "createdById";
            default -> "id";
        };

        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);

        Page<Reply> replyPage;

        if (search == null || search.trim().isEmpty()) {
            replyPage = replyRepository.findAll(pageable);
        } else {
            switch (searchBy.toLowerCase()) {
                case "all":
                    replyPage = replyRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
                case "id":
                    try {
                        Integer searchId = Integer.parseInt(search.trim());
                        replyPage = replyRepository.findById(searchId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 ID 형식으로 검색: {}", search);
                        replyPage = Page.empty(pageable);
                    }
                    break;
                case "contents":
                    replyPage = replyRepository.findByContentsContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "creator":
                    try {
                        Integer creatorId = Integer.parseInt(search.trim());
                        replyPage = replyRepository.findByCreatedById(creatorId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 작성자 ID 형식으로 검색: {}", search);
                        replyPage = Page.empty(pageable);
                    }
                    break;
                case "article":
                    try {
                        Integer articleId = Integer.parseInt(search.trim());
                        replyPage = replyRepository.findByArticleId(articleId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 게시글 ID 형식으로 검색: {}", search);
                        replyPage = Page.empty(pageable);
                    }
                    break;
                default:
                    log.warn("알 수 없는 검색 기준: {}. 'all'을 기본값으로 사용합니다.", searchBy);
                    replyPage = replyRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
            }
        }

        List<ReplyResponseDTO> result = replyPage.getContent().stream()
                .map(ReplyResponseDTO::fromEntity)
                .toList();

        log.info("총 {}개의 댓글 중 {}개를 찾았습니다.", replyPage.getTotalElements(), result.size());
        return new PagedResponse<>(result, pageNum, pageSizeNum, replyPage.getTotalElements(), replyPage.getTotalPages());
    }
}
