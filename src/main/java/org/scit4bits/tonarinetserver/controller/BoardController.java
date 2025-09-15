package org.scit4bits.tonarinetserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.ArticleDTO;
import org.scit4bits.tonarinetserver.dto.BoardDTO;
import org.scit4bits.tonarinetserver.dto.BoardWriteRequestDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.ArticleService;
import org.scit4bits.tonarinetserver.service.BoardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 게시판 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardController {
    private final BoardService boardService;
    private final ArticleService articleService;

    /**
     * 사용자가 접근할 수 있는 모든 게시판 목록을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @return BoardDTO 리스트
     */
    @GetMapping({"", "/"})
    public ResponseEntity<List<BoardDTO>> getBoards(@AuthenticationPrincipal User user) {
        // 로그인하지 않은 사용자는 접근할 수 없습니다.
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<BoardDTO> boardDTOs = boardService.getAccessibleBoards(user);

        return ResponseEntity.ok().body(boardDTOs);
    }

    /**
     * 특정 게시판의 정보를 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param boardId 조회할 게시판 ID
     * @return BoardDTO 형태의 게시판 정보
     */
    @GetMapping("/{boardId}/info")
    public ResponseEntity<BoardDTO> getBoard(@AuthenticationPrincipal User user, @PathVariable("boardId") Integer boardId) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        BoardDTO boardDTO = boardService.getBoardInformation(user, boardId);
        // 접근 권한이 없는 경우 403 Forbidden을 반환합니다.
        if (boardDTO == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(boardDTO);
    }

    /**
     * 특정 게시판에 새로운 게시글을 작성합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param boardId 게시글을 작성할 게시판 ID
     * @param request 게시글 작성 요청 정보
     * @param files 첨부 파일 리스트
     * @return 생성된 ArticleDTO 정보
     */
    @PostMapping("/{boardId}/write")
    public ResponseEntity<ArticleDTO> postBoardWrite(@AuthenticationPrincipal User user,
                                                     @PathVariable("boardId") Integer boardId,
                                                     @RequestPart("request") BoardWriteRequestDTO request,
                                                     @RequestPart(name = "files", required = false) List<MultipartFile> files
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.debug("User {} is writing to board {}", user.getUsername(), boardId);
        log.debug("Request to write board article: {}", request);

        // boardService를 통해 게시글을 생성합니다.
        ArticleDTO articleDTO = boardService.createArticle(user, boardId, request, files);

        return ResponseEntity.ok(articleDTO);
    }

    /**
     * 특정 게시판의 게시글을 검색합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param boardId 게시글을 검색할 게시판 ID
     * @param searchBy 검색 기준 (all, title, content, author)
     * @param search 검색어
     * @param category 카테고리
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 ArticleDTO 리스트
     */
    @GetMapping("/{boardId}/articles")
    public ResponseEntity<PagedResponse<ArticleDTO>> getArticlesWithSearch(
            @AuthenticationPrincipal User user,
            @PathVariable("boardId") Integer boardId,
            @RequestParam(name = "searchBy", defaultValue = "all") String searchBy,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // articleService를 통해 게시글을 검색합니다.
        PagedResponse<ArticleDTO> articles = articleService.searchArticles(user, boardId, searchBy, search, category, page, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(articles);
    }

    /**
     * 특정 게시판의 인기 게시글을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param boardId 게시판 ID
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @return 페이징 처리된 ArticleDTO 리스트
     */
    @GetMapping("/{boardId}/hotarticles")
    public ResponseEntity<PagedResponse<ArticleDTO>> getHotArticles(
            @AuthenticationPrincipal User user,
            @PathVariable("boardId") Integer boardId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // articleService를 통해 인기 게시글을 조회합니다.
        PagedResponse<ArticleDTO> hotArticles = articleService.getHotArticles(user, boardId, page, pageSize);
        return ResponseEntity.ok(hotArticles);
    }

}
