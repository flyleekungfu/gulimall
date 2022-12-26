package com.flylee.gulimall.product.exception;

import com.flylee.gulimall.common.exception.BizCodeEnum;
import com.flylee.gulimall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局异常切面
 *
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.flylee.gulimall.product.controller")
public class GulimallExceptionAdvice {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public R handleValidException(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        Map<String, String> map = new HashMap<>(fieldErrors.size());
        fieldErrors.forEach(fieldError -> map.put(fieldError.getField(), fieldError.getDefaultMessage()));
        log.error("数据校验出现问题:{},异常类型{}",exception.getMessage(),exception.getClass());
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data", map);
    }
}
