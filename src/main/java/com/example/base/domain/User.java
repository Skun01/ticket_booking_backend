package com.example.base.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String fullName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	public User(String email, String password, String fullName, Role role) {
		this.email = email;
		this.password = password;
		this.fullName = fullName;
		this.role = role;
	}

}
