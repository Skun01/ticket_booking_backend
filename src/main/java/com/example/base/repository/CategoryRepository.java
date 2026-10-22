package com.example.base.repository;

import com.example.base.domain.Category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	boolean existsBySlug(String slug);

	boolean existsBySlugAndIdNot(String slug, Long id);

}
