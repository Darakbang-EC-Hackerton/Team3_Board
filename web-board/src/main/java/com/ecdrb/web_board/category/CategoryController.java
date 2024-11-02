package com.ecdrb.web_board.category;

import com.ecdrb.web_board.DataNotFoundException;
import com.ecdrb.web_board.question.Question;
import com.ecdrb.web_board.question.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequestMapping("/category")
@Controller
public class CategoryController {

    private final CategoryService categoryService;
    private final QuestionService questionService;

    @Autowired
    public CategoryController(CategoryService categoryService, QuestionService questionService) {
        this.categoryService = categoryService;
        this.questionService = questionService;
    }

    @GetMapping("/{category}")
    public String contentList(Model model, @PathVariable("category") String categoryName,
                              @RequestParam(value = "page", defaultValue = "0") int page) {
        Optional<Category> categoryOpt = this.categoryService.getCategoryByName(categoryName);
        if (categoryOpt.isEmpty()) {
            throw new DataNotFoundException("category not found");
        }
        Category category = categoryOpt.get();

        List<Category> categoryList = this.categoryService.getAll();
        Page<Question> paging = this.questionService.getCategoryQuestionList(category, page);

        model.addAttribute("category_name", categoryName);
        model.addAttribute("category_list", categoryList);
        model.addAttribute("paging", paging);
        return "category_question_list";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Category> addCategory(@RequestBody CategoryDto categoryDto) {
        // 카테고리 이름이 비어있는지 확인
        if (categoryDto.getName() == null || categoryDto.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }

        // 카테고리 이름의 중복 체크
        if (categoryService.getCategoryByName(categoryDto.getName()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict
        }

        // 카테고리 생성
        Category createdCategory = categoryService.create(categoryDto.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory); // 201 Created
    }
}
