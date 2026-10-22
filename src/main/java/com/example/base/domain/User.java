package com.example.base.domain;

import com.example.base.domain.enums.Role;
import com.example.base.domain.enums.UserStatus;
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

	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserStatus status;

	public User(String email, String password, String fullName, Role role) {
		this.email = email;
		this.password = password;
		this.fullName = fullName;
		this.role = role;
		this.status = UserStatus.ACTIVE;
	}

	public void updateProfile(String fullName, String phone) {
		this.fullName = fullName;
		this.phone = phone;
	}

	public void lock() {
		this.status = UserStatus.LOCKED;
	}

	public void activate() {
		this.status = UserStatus.ACTIVE;
	}

}
