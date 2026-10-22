package com.example.base.controller;

import java.net.URI;

import com.example.base.annotation.IsAdmin;
import com.example.base.dto.category.CategoryRequest;
import com.example.base.dto.category.CategoryResponse;
import com.example.base.dto.common.PageResponse;
import com.example.base.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
@IsAdmin
@RequiredArgsConstructor
public class AdminCategoryController {

	private final CategoryService categoryService;

	@Operation(summary = "Danh sách danh mục")
	@GetMapping
	public PageResponse<CategoryResponse> list(
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return categoryService.list(PageRequest.of(page, size, Sort.by("id").descending()));
	}

	@Operation(summary = "Chi tiết danh mục")
	@GetMapping("/{id}")
	public CategoryResponse getById(@PathVariable Long id) {
		return categoryService.getById(id);
	}

	@Operation(summary = "Tạo danh mục")
	@PostMapping
	public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
		CategoryResponse response = categoryService.create(request);

		return ResponseEntity.created(URI.create("/api/admin/categories/" + response.id())).body(response);
	}

	@Operation(summary = "Cập nhật danh mục")
	@PutMapping("/{id}")
	public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
		return categoryService.update(id, request);
	}

	@Operation(summary = "Xóa danh mục")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		categoryService.delete(id);

		return ResponseEntity.noContent().build();
	}

}
