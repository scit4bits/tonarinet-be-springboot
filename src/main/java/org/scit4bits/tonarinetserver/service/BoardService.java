package org.scit4bits.tonarinetserver.service;

import java.util.ArrayList;
import java.util.List;

import org.scit4bits.tonarinetserver.dto.ArticleDTO;
import org.scit4bits.tonarinetserver.dto.BoardDTO;
import org.scit4bits.tonarinetserver.entity.Article;
import org.scit4bits.tonarinetserver.entity.Board;
import org.scit4bits.tonarinetserver.entity.Country;
import org.scit4bits.tonarinetserver.entity.Organization;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.entity.UserRole;
import org.scit4bits.tonarinetserver.repository.BoardRepository;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

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

    public List<ArticleDTO> getArticlesOfBoard(User user, Integer boardId) {
        User dbUser = userRepository.findById(user.getId()).get();
        Board board = boardRepository.findById(boardId).get();
        boolean authenticated = false;
        for(UserRole roles: dbUser.getUserRoles()){
            if(roles.getId().getOrgId() == board.getOrgId() || user.getIsAdmin() == true){
                // User has access to the board
                authenticated = true;
                break;
            }
        }
        if(!authenticated){
            return null;
        }
        List<ArticleDTO> articleDTOs = new ArrayList<>();

        for(Article article : board.getArticles()){ // full experiences with JPA
            articleDTOs.add(ArticleDTO.fromEntity(article));
        }
        return articleDTOs;
    }

}
