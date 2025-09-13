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

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final NotificationService notificationService;
    private final ArticleRepository articleRepository;

    public ReplyResponseDTO createReply(ReplyRequestDTO request, User creator) {
        log.info("Creating reply for article: {} by user: {}", request.getArticleId(), creator.getId());

        Reply reply = Reply.builder()
                .contents(request.getContents())
                .createdById(creator.getId())
                .articleId(request.getArticleId())
                .build();

        Reply savedReply = replyRepository.save(reply);
        log.info("Reply created successfully with id: {}", savedReply.getId());

        // send notification to creator of the article
        articleRepository.findById(request.getArticleId()).ifPresent(article -> {
            if (!article.getCreatedById().equals(creator.getId())) {
                notificationService.addNotification(article.getCreatedById(),
                        "{\"messageType\": \"newReplyToArticle\", \"articleTitle\": \"" + article.getTitle() + "\", \"userName\": \"" + creator.getUsername() + "\"}",
                        "/board/view/" + article.getId());
                log.info("Notification sent to article creator: {}", article.getCreatedById());
            } else {
                log.info("No notification sent as the replier is the article creator.");
            }
        });


        return ReplyResponseDTO.fromEntity(savedReply);
    }

    @Transactional(readOnly = true)
    public List<ReplyResponseDTO> getAllReplies() {
        log.info("Fetching all replies");
        return replyRepository.findAll().stream()
                .map(ReplyResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReplyResponseDTO getReplyById(Integer id) {
        log.info("Fetching reply with id: {}", id);
        Reply reply = replyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reply not found with id: " + id));
        return ReplyResponseDTO.fromEntity(reply);
    }

    @Transactional(readOnly = true)
    public List<ReplyResponseDTO> getRepliesByArticleId(Integer articleId) {
        log.info("Fetching replies for article: {}", articleId);
        return replyRepository.findByArticleIdOrderByCreatedAtAsc(articleId).stream()
                .map(ReplyResponseDTO::fromEntity)
                .toList();
    }

    public ReplyResponseDTO updateReply(Integer id, ReplyRequestDTO request, User user) {
        log.info("Updating reply with id: {} by user: {}", id, user.getId());

        Reply reply = replyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reply not found with id: " + id));

        // Check if user is the creator or admin
        if (!reply.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("Only the reply creator or admin can update the reply");
        }

        reply.setContents(request.getContents() != null && !request.getContents().trim().isEmpty()
                ? request.getContents() : reply.getContents());
        reply.setArticleId(request.getArticleId() != null ? request.getArticleId() : reply.getArticleId());

        Reply savedReply = replyRepository.save(reply);
        log.info("Reply updated successfully");
        return ReplyResponseDTO.fromEntity(savedReply);
    }

    public void deleteReply(Integer id, User user) {
        log.info("Deleting reply with id: {} by user: {}", id, user.getId());

        Reply reply = replyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reply not found with id: " + id));

        // Check if user is the creator or admin
        if (!reply.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("Only the reply creator or admin can delete the reply");
        }

        replyRepository.deleteById(id);
        log.info("Reply deleted successfully");
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReplyResponseDTO> searchReplies(String searchBy, String search, Integer page,
                                                         Integer pageSize, String sortBy, String sortDirection) {
        log.info("Searching replies with searchBy: {}, search: {}, page: {}, pageSize: {}, sortBy: {}, sortDirection: {}",
                searchBy, search, page, pageSize, sortBy, sortDirection);

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
            case "created":
                entityFieldName = "createdAt";
                break;
            case "article":
                entityFieldName = "articleId";
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
                        log.warn("Invalid ID format for search: {}", search);
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
                        log.warn("Invalid creator ID format for search: {}", search);
                        replyPage = Page.empty(pageable);
                    }
                    break;
                case "article":
                    try {
                        Integer articleId = Integer.parseInt(search.trim());
                        replyPage = replyRepository.findByArticleId(articleId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid article ID format for search: {}", search);
                        replyPage = Page.empty(pageable);
                    }
                    break;
                default:
                    log.warn("Unknown searchBy parameter: {}. Using 'all' as default.", searchBy);
                    replyPage = replyRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
            }
        }

        List<ReplyResponseDTO> result = replyPage.getContent().stream()
                .map(ReplyResponseDTO::fromEntity)
                .toList();

        log.info("Found {} replies out of {} total", result.size(), replyPage.getTotalElements());
        return new PagedResponse<>(result, pageNum, pageSizeNum, replyPage.getTotalElements(), replyPage.getTotalPages());
    }
}
