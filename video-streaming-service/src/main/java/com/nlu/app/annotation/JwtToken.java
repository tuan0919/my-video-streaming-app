package com.nlu.app.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER) // Đánh dấu tham số của phương thức
@Retention(RetentionPolicy.RUNTIME) // Giữ lại annotation tại thời điểm chạy
public @interface JwtToken {
}
