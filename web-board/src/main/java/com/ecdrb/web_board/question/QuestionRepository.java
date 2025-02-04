package com.ecdrb.web_board.question;

import com.ecdrb.web_board.category.Category;
import com.ecdrb.web_board.question.Question;
import com.ecdrb.web_board.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    Question findBySubject(String subject);
    Question findBySubjectAndContent(String subject, String content);
    List<Question> findBySubjectLike(String subject);
    Page<Question> findAll(Pageable pageable);
    Page<Question> findAll(Specification<Question> spec, Pageable pageable);

    Page<Question> findByAuthor(SiteUser siteUser, Pageable pageable);

    Page<Question> findByCategory(Category category, Pageable pageable);
}
