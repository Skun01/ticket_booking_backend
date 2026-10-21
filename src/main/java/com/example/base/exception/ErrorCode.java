package com.example.base.exception;

import org.springframework.http.HttpStatus;

// Mỗi feature khai báo một enum implement interface này (VD: UserErrorCode),
// name() của enum chính là mã lỗi trả về cho client trong field "code"
public interface ErrorCode {

	String name();

	HttpStatus getStatus();

	String getMessage();

}
