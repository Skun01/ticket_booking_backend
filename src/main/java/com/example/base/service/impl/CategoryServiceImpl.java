package com.example.base.service.impl;

import com.example.base.domain.Category;
import com.example.base.dto.category.CategoryRequest;
import com.example.base.dto.category.CategoryResponse;
import com.example.base.dto.common.PageResponse;
import com.example.base.exception.AppException;
import com.example.base.exception.CatalogErrorCode;
import com.example.base.repository.CategoryRepository;
import com.example.base.repository.EventRepository;
import com.example.base.service.CategoryService;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;
	private final EventRepository eventRepository;

	@Override
	public PageResponse<CategoryResponse> list(Pageable pageable) {
		return PageResponse.from(categoryRepository.findAll(pageable).map(CategoryResponse::from));
	}

	@Override
	public CategoryResponse getById(Long id) {
		return CategoryResponse.from(findCategory(id));
	}

	@Override
	@Transactional
	public CategoryResponse create(CategoryRequest request) {
		if (categoryRepository.existsBySlug(request.slug())) {
			throw new AppException(CatalogErrorCode.CATEGORY_SLUG_EXISTS);
		}
		Category category = new Category(request.name().trim(), request.slug());
		try {
			categoryRepository.saveAndFlush(category);
		}
		catch (DataIntegrityViolationException ex) {
			throw new AppException(CatalogErrorCode.CATEGORY_SLUG_EXISTS);
		}

		return CategoryResponse.from(category);
	}

	@Override
	@Transactional
	public CategoryResponse update(Long id, CategoryRequest request) {
		Category category = findCategory(id);
		if (categoryRepository.existsBySlugAndIdNot(request.slug(), id)) {
			throw new AppException(CatalogErrorCode.CATEGORY_SLUG_EXISTS);
		}
		category.update(request.name().trim(), request.slug());
		try {
			categoryRepository.flush();
		}
		catch (DataIntegrityViolationException ex) {
			throw new AppException(CatalogErrorCode.CATEGORY_SLUG_EXISTS);
		}

		return CategoryResponse.from(category);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		Category category = findCategory(id);
		if (eventRepository.existsByCategoryId(id)) {
			throw new AppException(CatalogErrorCode.CATEGORY_IN_USE);
		}
		try {
			categoryRepository.delete(category);
			categoryRepository.flush();
		}
		catch (DataIntegrityViolationException ex) {
			throw new AppException(CatalogErrorCode.CATEGORY_IN_USE);
		}
	}

	private Category findCategory(Long id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new AppException(CatalogErrorCode.CATEGORY_NOT_FOUND));
	}

}
