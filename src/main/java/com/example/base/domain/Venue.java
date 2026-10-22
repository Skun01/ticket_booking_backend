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
@Table(name = "venues")
public class Venue extends BaseEntity {

	@Column(nullable = false, length = 150)
	private String name;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false, length = 100)
	private String city;

	@Column(nullable = false)
	private int capacity;

	public Venue(String name, String address, String city, int capacity) {
		this.name = name;
		this.address = address;
		this.city = city;
		this.capacity = capacity;
	}

	public void update(String name, String address, String city, int capacity) {
		this.name = name;
		this.address = address;
		this.city = city;
		this.capacity = capacity;
	}

}
