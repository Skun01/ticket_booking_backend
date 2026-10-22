package com.example.base.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, unique = true, length = 120)
	private String slug;

	public Category(String name, String slug) {
		this.name = name;
		this.slug = slug;
	}

	public void update(String name, String slug) {
		this.name = name;
		this.slug = slug;
	}

}
