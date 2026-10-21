package com.example.base.service.impl;

import java.util.Optional;

import com.example.base.domain.User;
import com.example.base.domain.enums.Role;
import com.example.base.exception.AppException;
import com.example.base.exception.UserErrorCode;
import com.example.base.repository.UserRepository;
import com.example.base.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional(readOnly = true)
	public User getById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	@Transactional
	public User create(String email, String rawPassword, String fullName, Role role) {
		if (userRepository.existsByEmail(email)) {
			throw new AppException(UserErrorCode.EMAIL_ALREADY_REGISTERED);
		}
		try {
			return userRepository.saveAndFlush(new User(email, passwordEncoder.encode(rawPassword), fullName, role));
		}
		catch (DataIntegrityViolationException ex) {
			throw new AppException(UserErrorCode.EMAIL_ALREADY_REGISTERED);
		}
	}

}
