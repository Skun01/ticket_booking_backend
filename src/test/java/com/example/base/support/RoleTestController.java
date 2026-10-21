package com.example.base.support;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoint chỉ tồn tại khi chạy test (được component scan vì nằm trong package gốc), dùng để kiểm tra phân quyền
@RestController
class RoleTestController {

	@GetMapping("/api/test/admin-only")
	@PreAuthorize("hasRole('ADMIN')")
	String adminOnly() {
		return "ok";
	}

}
