package org.scit4bits.tonarinetserver.controller;

import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;



@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardController {
    private final BoardService boardService;
    private final ArticleService articleService;

    @GetMapping({"", "/"})
    public ResponseEntity<List<BoardDTO>> getBoards(@AuthenticationPrincipal User user) {
        if(user == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<BoardDTO> boardDTOs = boardService.getAccessibleBoards(user);

        return ResponseEntity.ok().body(boardDTOs);
    }

    @GetMapping("/{boardId}/info")
    public ResponseEntity<BoardDTO> getBoard(@AuthenticationPrincipal User user, @PathVariable("boardId") Integer boardId) {
        if(user == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        BoardDTO boardDTO = boardService.getBoardInformation(user, boardId);
        if (boardDTO == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(boardDTO);
    }

    @PostMapping("/{boardId}/write")
    public ResponseEntity<ArticleDTO> postBoardWrite(@AuthenticationPrincipal User user,
     @PathVariable("boardId") Integer boardId, 
     @RequestPart("request") BoardWriteRequestDTO request,
     @RequestPart(name= "files", required = false) List<MultipartFile> files
     ) {
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.debug("User {} is writing to board {}", user.getUsername(), boardId);
        log.debug("Request to write board article: {}", request);
        // log.debug("Uploaded files: {}", files.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.toList()));

        ArticleDTO articleDTO = boardService.createArticle(user, boardId, request, files);

        return ResponseEntity.ok(articleDTO);
    }

    @GetMapping("/{boardId}/articles")
    public ResponseEntity<PagedResponse<ArticleDTO>> getArticlesWithSearch(
        @AuthenticationPrincipal User user,
        @PathVariable("boardId") Integer boardId,
        @RequestParam(name="searchBy", defaultValue = "all") String searchBy,
        @RequestParam(name="search", defaultValue = "") String search,
        @RequestParam(name="category", required = false) String category,
        @RequestParam(name="page", defaultValue = "0") Integer page,
        @RequestParam(name="pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(name="sortBy", defaultValue = "id") String sortBy,
        @RequestParam(name="sortDirection", defaultValue = "asc") String sortDirection
    ){
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        PagedResponse<ArticleDTO> articles = articleService.searchArticles(user,boardId, searchBy, search, category, page, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/{boardId}/hotarticles")
    public ResponseEntity<PagedResponse<ArticleDTO>> getHotArticles(
            @AuthenticationPrincipal User user,
            @PathVariable("boardId") Integer boardId,
            @RequestParam(name="page", defaultValue = "0") Integer page,
            @RequestParam(name="pageSize", defaultValue = "10") Integer pageSize
    ) {
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PagedResponse<ArticleDTO> hotArticles = articleService.getHotArticles(user, boardId, page, pageSize);
        return ResponseEntity.ok(hotArticles);
    }
    
}
