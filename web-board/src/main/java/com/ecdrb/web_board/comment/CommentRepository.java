package com.ecdrb.web_board.comment;

import com.ecdrb.web_board.question.Question;
import com.ecdrb.web_board.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByQuestion(Question question);
    Page<Comment> findByAuthor(SiteUser siteUser, Pageable pageable);
}