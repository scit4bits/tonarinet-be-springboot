package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.ArticleDTO;
import org.scit4bits.tonarinetserver.dto.BoardDTO;
import org.scit4bits.tonarinetserver.dto.BoardWriteRequestDTO;
import org.scit4bits.tonarinetserver.dto.FileAttachmentRequestDTO;
import org.scit4bits.tonarinetserver.entity.*;
import org.scit4bits.tonarinetserver.entity.FileAttachment.FileType;
import org.scit4bits.tonarinetserver.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 게시판 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRoleService userRoleService;
    private final FileAttachmentService fileAttachmentService;
    private final TagRepository tagRepository;
    private final UserCountryService userCountryService;
    private final NotificationService notificationService;

    /**
     * 사용자가 접근할 수 있는 모든 게시판 목록을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @return 접근 가능한 BoardDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<BoardDTO> getAccessibleBoards(User user) {
        List<UserRole> roles = userRoleService.getUserRoleByUser(user);
        User dbUser = userRepository.findById(user.getId()).get();
        List<BoardDTO> boards = new ArrayList<>();

        for(UserRole role : roles){
            if(role.getIsGranted() && role.getId().getOrgId() != null){
                List<Board> orgBoards = boardRepository.findByOrgId(role.getId().getOrgId());
                for (Board board : orgBoards) {
                    boards.add(BoardDTO.fromEntity(board));
                }
            }
        }

        List<Country> countries = dbUser.getCountries();

        // 사용자가 속한 국가의 게시판 추가
        for (Country country : countries) {
            List<Board> countryBoards = boardRepository.findByCountryCode(country.getCountryCode());
            for (Board board : countryBoards) {
                boards.add(BoardDTO.fromEntity(board));
            }
        }

        return boards;
    }

    /**
     * 특정 게시판의 게시글 목록을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param boardId 게시판 ID
     * @return ArticleDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ArticleDTO> getArticlesByBoardId(User user, Integer boardId) {
        Board board = boardRepository.findById(boardId).orElse(null);
        if (board == null) {
            return null;
        }

        // 게시판 접근 권한 확인
        if (board.getCountryCode() != null) {
            if (!userCountryService.checkUserCountryAccess(user.getId(), board.getCountryCode())) {
                log.debug("사용자 {}가 국가 게시판 {}에 접근할 수 없습니다.", user.getId(), board.getCountryCode());
                return null;
            }
        } else if (board.getOrgId() != null) {
            Organization organization = organizationRepository.findById(board.getOrgId()).orElse(null);
            if (organization == null || !userRoleService.checkUsersRoleInOrg(user, organization, null)) {
                log.debug("사용자 {}가 조직 게시판 {}에 접근할 수 없습니다.", user.getId(), organization != null ? organization.getName() : "unknown");
                return null;
            }
        }

        // 상담 게시글을 제외한 게시글 목록 조회
        List<Article> articles = articleRepository.findByBoardIdAndCategoryNotOrderByCreatedAtDesc(boardId, "counsel");
        return articles.stream()
                .map(ArticleDTO::fromEntity)
                .toList();
    }

    /**
     * 새로운 게시글을 생성합니다.
     * @param user 작성자 정보
     * @param boardId 게시판 ID
     * @param request 게시글 작성 요청 정보
     * @param files 첨부 파일 리스트
     * @return 생성된 ArticleDTO
     */
    public ArticleDTO createArticle(User user, Integer boardId, BoardWriteRequestDTO request, List<MultipartFile> files) {
        Board board = boardRepository.findById(boardId).get();

        // 게시판 접근 권한 확인
        if (board.getCountryCode() != null) {
            if (!userCountryService.checkUserCountryAccess(user.getId(), board.getCountryCode())) {
                log.debug("사용자 {}가 국가 게시판 {}에 접근할 수 없습니다.", user.getId(), board.getCountryCode());
                throw new AccessDeniedException("이 게시판에 게시글을 작성할 권한이 없습니다.");
            }
        } else if (board.getOrgId() != null) {
            Organization organization = organizationRepository.findById(board.getOrgId()).get();
            if (!userRoleService.checkUsersRoleInOrg(user, organization, null)) {
                log.debug("사용자 {}가 조직 게시판 {}에 접근할 수 없습니다.", user.getId(), organization.getName());
                throw new AccessDeniedException("이 게시판에 게시글을 작성할 권한이 없습니다.");
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

        // 공지사항인 경우 알림 발송
        if (savedArticle.getCategory().equals("notice")) {
            log.debug("공지사항 게시글에 대한 알림 발송: {}", savedArticle.getTitle());
            if (board.getId() == 0) {
                // 전체 공지
                log.debug("전체 공지, 모든 사용자에게 알림");
                List<User> allUsers = userRepository.findAll();
                for (User u : allUsers) {
                    notificationService.addNotification(u.getId(), "{\"messageType\": \"newNotice\", \"title\": \"" + article.getTitle() + "\"}", "/board/view/" + savedArticle.getId());
                }
            } else if (board.getOrgId() != null) {
                // 조직 공지
                log.debug("조직 공지, 조직 {}의 모든 사용자에게 알림", board.getOrgId());
                Organization organization = organizationRepository.findById(board.getOrgId()).get();
                List<User> orgUsers = organization.getUsers();
                for (User u : orgUsers) {
                    notificationService.addNotification(u.getId(), "{\"messageType\": \"newOrgNotice\", \"title\": \"" + article.getTitle() + "\"}", "/board/view/" + savedArticle.getId());
                }
            }
        }

        return ArticleDTO.fromEntity(
                savedArticle
        );
    }

    /**
     * 특정 게시판의 정보를 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param boardId 게시판 ID
     * @return BoardDTO
     */
    public BoardDTO getBoardInformation(User user, Integer boardId) {
        Board board = boardRepository.findById(boardId).get();

        // 게시판 접근 권한 확인
        if (board.getCountryCode() != null) {
            if (!userCountryService.checkUserCountryAccess(user.getId(), board.getCountryCode())) {
                log.debug("사용자 {}가 국가 게시판 {}에 접근할 수 없습니다.", user.getId(), board.getCountryCode());
                throw new AccessDeniedException("이 게시판에 접근할 권한이 없습니다.");
            }
        } else if (board.getOrgId() != null) {
            Organization organization = organizationRepository.findById(board.getOrgId()).get();
            if (!userRoleService.checkUsersRoleInOrg(user, organization, null)) {
                log.debug("사용자 {}가 조직 게시판 {}에 접근할 수 없습니다.", user.getId(), organization.getName());
                throw new AccessDeniedException("이 게시판에 접근할 권한이 없습니다.");
            }
        }

        return BoardDTO.fromEntity(board);
    }

}
