package com.example.base.service;

import com.example.base.dto.category.CategoryRequest;
import com.example.base.dto.category.CategoryResponse;
import com.example.base.dto.common.PageResponse;

import org.springframework.data.domain.Pageable;

public interface CategoryService {

	PageResponse<CategoryResponse> list(Pageable pageable);

	CategoryResponse getById(Long id);

	CategoryResponse create(CategoryRequest request);

	CategoryResponse update(Long id, CategoryRequest request);

	void delete(Long id);

}
