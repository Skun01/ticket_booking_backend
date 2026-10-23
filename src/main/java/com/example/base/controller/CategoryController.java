package com.example.base.controller;

import com.example.base.dto.category.CategoryResponse;
import com.example.base.dto.common.PageResponse;
import com.example.base.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@SecurityRequirements
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@Operation(summary = "Danh sách danh mục công khai")
	@GetMapping
	public PageResponse<CategoryResponse> list(
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "100") @Min(1) @Max(100) int size) {
		return categoryService.list(PageRequest.of(page, size, Sort.by("name").ascending()));
	}

}
