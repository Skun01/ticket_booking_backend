package com.example.base.dto.category;

import com.example.base.domain.Category;

public record CategoryResponse(Long id, String name, String slug) {

	public static CategoryResponse from(Category category) {
		return new CategoryResponse(category.getId(), category.getName(), category.getSlug());
	}

}
