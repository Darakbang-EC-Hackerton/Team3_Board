package com.ecdrb.web_board.question;

import com.ecdrb.web_board.answer.Answer;
import com.ecdrb.web_board.answer.AnswerForm;
import com.ecdrb.web_board.answer.AnswerService;
import com.ecdrb.web_board.category.Category;
import com.ecdrb.web_board.category.CategoryService;
import com.ecdrb.web_board.comment.Comment;
import com.ecdrb.web_board.comment.CommentForm;
import com.ecdrb.web_board.comment.CommentService;
import com.ecdrb.web_board.user.SiteUser;
import com.ecdrb.web_board.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RequestMapping("/question")
@Controller
@RequiredArgsConstructor // @Autowired 대신 사용할 수 있는 롬복 어노테이션
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;
    private final AnswerService answerService;
    private final CommentService commentService;
    private final CategoryService categoryService;

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(value="page", defaultValue="0") int page,
                       @RequestParam(value="kw", defaultValue="") String kw) {
        Page<Question> paging = questionService.getList(page, kw);
        List<Category> categoryList = categoryService.getAll();

        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("category_list", categoryList);
        return "question_list";
    }

    @GetMapping("/detail/{id}")
    public String detail(Model model,
                         @PathVariable("id") Integer id,
                         AnswerForm answerForm,
                         CommentForm commentForm,
                         @RequestParam(value="ans-page", defaultValue="0") int answerPage,
                         @RequestParam(value="ans-ordering", defaultValue="time") String answerOrderMethod) {
        questionService.viewUp(id);
        Question question = questionService.getQuestion(id);
        Page<Answer> answerPaging = answerService.getAnswerList(question, answerPage, answerOrderMethod);
        List<Comment> commentList = commentService.getCommentList(question);
        List<Category> categoryList = categoryService.getAll();

        model.addAttribute("question", question);
        model.addAttribute("ans_paging", answerPaging);
        model.addAttribute("comment_list", commentList);
        model.addAttribute("category_list", categoryList);
        return "question_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm, Model model) {
        List<Category> categoryList = categoryService.getAll();
        model.addAttribute("category_list", categoryList);
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(@Valid QuestionForm questionForm,
                                 BindingResult bindingResult,
                                 Principal principal) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }

        SiteUser siteUser = userService.getUser(principal.getName());
        Category category = categoryService.getCategoryByName(questionForm.getCategory())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."));
        questionService.create(questionForm.getSubject(), questionForm.getContent(), category, siteUser);
        return "redirect:/question/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm,
                                 @PathVariable("id") Integer id,
                                 Principal principal,
                                 Model model) {
        Question question = questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }
        List<Category> categoryList = categoryService.getAll();

        model.addAttribute("category_list", categoryList);
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm,
                                 BindingResult bindingResult,
                                 Principal principal,
                                 @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }

        Question question = questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }
        questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        return String.format("redirect:/question/detail/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
        Question question = questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
        }
        questionService.delete(question);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id) {
        Question question = questionService.getQuestion(id);
        SiteUser siteUser = userService.getUser(principal.getName());
        questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }
}
