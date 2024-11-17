package com.cp.classpay.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class ApiResponse<T> {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Rangoon")
    private Date timeStamp;
    private int statusCode;
    private HttpStatus httpStatus;
    private String message;
    private T data;

    public ApiResponse(HttpStatus httpStatus, T data) {
        this.timeStamp = new Date();
        this.statusCode = httpStatus.value();
        this.httpStatus = httpStatus;
        this.message = httpStatus.getReasonPhrase().toUpperCase();
        this.data = data;
    }

    public static <T> ResponseEntity<ApiResponse<T>> of(T data, HttpStatus httpStatus) {
        ApiResponse<T> body = new ApiResponse<>(httpStatus, data);
        return new ResponseEntity<>(body, httpStatus);
    }

}
