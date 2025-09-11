package org.scit4bits.tonarinetserver.service;

import java.util.ArrayList;
import java.util.List;

import org.scit4bits.tonarinetserver.dto.ArticleDTO;
import org.scit4bits.tonarinetserver.dto.BoardDTO;
import org.scit4bits.tonarinetserver.dto.BoardWriteRequestDTO;
import org.scit4bits.tonarinetserver.dto.FileAttachmentRequestDTO;
import org.scit4bits.tonarinetserver.entity.Article;
import org.scit4bits.tonarinetserver.entity.Board;
import org.scit4bits.tonarinetserver.entity.Country;
import org.scit4bits.tonarinetserver.entity.Organization;
import org.scit4bits.tonarinetserver.entity.Tag;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.entity.FileAttachment.FileType;
import org.scit4bits.tonarinetserver.repository.ArticleRepository;
import org.scit4bits.tonarinetserver.repository.BoardRepository;
import org.scit4bits.tonarinetserver.repository.OrganizationRepository;
import org.scit4bits.tonarinetserver.repository.TagRepository;
import org.scit4bits.tonarinetserver.repository.UserRepository;
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

    @Transactional(readOnly = true)
    public List<BoardDTO> getAccessibleBoards(User user){
        User dbUser = userRepository.findById(user.getId()).get();
        List<BoardDTO> boards = new ArrayList<>();
        List<Organization> orgs = dbUser.getOrganizations();

        for(Organization org : orgs){
            List<Board> orgBoards = boardRepository.findByOrgId(org.getId());
            for(Board board : orgBoards){
                boards.add(BoardDTO.fromEntity(board));
            }
        }

        List<Country> countries = dbUser.getCountries();

        for(Country country: countries){
            List<Board> countryBoards = boardRepository.findByCountryCode(country.getCountryCode());
            for(Board board : countryBoards){
                boards.add(BoardDTO.fromEntity(board));
            }
        }

        return boards;
    }

    @Transactional(readOnly = true)
    public List<ArticleDTO> getArticlesByBoardId(User user, Integer boardId) {
        Board board = boardRepository.findById(boardId).orElse(null);
        if (board == null) {
            return null;
        }

        // Check if user has access to the board
        if(board.getCountryCode() != null){
            if(!userCountryService.checkUserCountryAccess(user.getId(), board.getCountryCode())) {
                log.debug("User {} does not have access to country board {}", user.getId(), board.getCountryCode());
                return null;
            }
        }else if(board.getOrgId() != null){
            Organization organization = organizationRepository.findById(board.getOrgId()).orElse(null);
            if(organization == null || !userRoleService.checkUsersRoleInOrg(user, organization, null)) {
                log.debug("User {} does not have access to organization board {}", user.getId(), organization != null ? organization.getName() : "unknown");
                return null;
            }
        }

        // Get articles for this board (excluding counsel articles)
        List<Article> articles = articleRepository.findByBoardIdAndCategoryNotOrderByCreatedAtDesc(boardId, "counsel");
        return articles.stream()
                .map(ArticleDTO::fromEntity)
                .toList();
    }



    public ArticleDTO createArticle(User user, Integer boardId, BoardWriteRequestDTO request, List<MultipartFile> files) {
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

        if(savedArticle.getCategory().equals("notice")){
            log.debug("Sending notifications for notice article: {}", savedArticle.getTitle());
            if(board.getId() == 0){
                // site-wide notice
                log.debug("Site-wide notice, notifying all users");
                List<User> allUsers = userRepository.findAll();
                for(User u : allUsers){
                    notificationService.addNotification(u.getId(), "{\"messageType\": \"newNotice\", \"title\": \"" + article.getTitle() + "\"}", "/board/view/" + savedArticle.getId());
                }
            }
            else if(board.getOrgId() != null){
                log.debug("Organization notice, notifying all users in organization {}", board.getOrgId());
                Organization organization = organizationRepository.findById(board.getOrgId()).get();
                List<User> orgUsers = organization.getUsers();
                for(User u : orgUsers){
                    notificationService.addNotification(u.getId(), "{\"messageType\": \"newOrgNotice\", \"title\": \"" + article.getTitle() + "\"}", "/board/view/" + savedArticle.getId());
                }
            }
        }

        return ArticleDTO.fromEntity(
            savedArticle
        );
    }

    public BoardDTO getBoardInformation(User user, Integer boardId) {
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
        }

        return BoardDTO.fromEntity(board);
    }

}
