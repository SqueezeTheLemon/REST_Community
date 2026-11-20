package com.back.global.globalExceptionHandler;

import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RsData<Void>> handle(NoSuchElementException ex) {
        return new ResponseEntity<>(
                new RsData<>(
                        "404-1",
                        "해당 데이터가 존재하지 않습니다."
                ),
                NOT_FOUND
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handle(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .filter(error -> error instanceof FieldError)
                .map(error -> (FieldError) error)
                .map(error -> error.getField() + "-" + error.getCode() + "-" + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("\n"));

        return new ResponseEntity<>(
                new RsData<>(
                        "400-1",
                        msg
                ),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RsData<Void>> handle(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(
                new RsData<>(
                        "400-1",
                        "요청 본문이 올바르지 않습니다."
                ),
                BAD_REQUEST
        );
    }

    // actorId가 없거나, 존재하지 않는 회원일 때
    public static class UnauthenticatedException extends RuntimeException {
        public UnauthenticatedException() {
            super("로그인 후 이용해주세요.");
        }
    }

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<RsData<Void>> handle(UnauthenticatedException ex) {
        return new ResponseEntity<>(
                new RsData<>(
                        "401-1",
                        ex.getMessage()
                ),
                UNAUTHORIZED
        );
    }

    // 글/댓글 수정·삭제에서 작성자가 아닐 때
    // 주의: java.nio.file.AccessDeniedException과 이름이 같으니 import 조심
    public static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException() {
            super("권한이 없습니다.");
        }
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RsData<Void>> handle(AccessDeniedException ex) {
        return new ResponseEntity<>(
                new RsData<>(
                        "403-1",
                        ex.getMessage()
                ),
                FORBIDDEN
        );
    }

    // 회원가입 시 username 중복
    public static class MemberDuplicateUsernameException extends RuntimeException {
        private final String username;

        public MemberDuplicateUsernameException(String username) {
            super("%s(은)는 이미 사용중인 username 입니다.".formatted(username));
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
    }

    // 핸들러 파라미터는 "잡은 예외 객체"를 받는다 (String X)
    @ExceptionHandler(MemberDuplicateUsernameException.class)
    public ResponseEntity<RsData<Void>> handle(MemberDuplicateUsernameException ex) {
        return new ResponseEntity<>(
                new RsData<>(
                        "409-1",
                        ex.getMessage()
                ),
                CONFLICT
        );
    }
}