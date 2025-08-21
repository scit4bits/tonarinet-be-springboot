package org.scit4bits.tonarinetserver.controller;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.ArticleDTO;
import org.scit4bits.tonarinetserver.dto.BoardDTO;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.BoardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardController {
    private final BoardService boardService;

    @GetMapping({"", "/"})
    public ResponseEntity<List<BoardDTO>> getBoards(@AuthenticationPrincipal User user) {
        if(user == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<BoardDTO> boardDTOs = boardService.getAccessibleBoards(user);

        return ResponseEntity.ok().body(boardDTOs);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<List<ArticleDTO>> getBoard(@AuthenticationPrincipal User user, @PathVariable("boardId") Integer boardId) {
        if(user == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<ArticleDTO> articleDTOs = boardService.getArticlesOfBoard(user, boardId);
        if (articleDTOs == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(articleDTOs);
    }

}
