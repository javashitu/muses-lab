package com.muses.recommend.command.response;

import com.muses.recommend.common.enums.ServerErrorCodeEnums;
import lombok.Builder;
import lombok.Data;

/**
 * @ClassName ApiResult
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2024/9/26 17:18
 */
@Data
public class ApiResult {

    private String code;

    private String message;

    private Object data;

    @Builder
    public ApiResult(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResult success(T data) {
        return ApiResult.builder()
                .code(ServerErrorCodeEnums.SUCCESS.getCode())
                .message(ServerErrorCodeEnums.SUCCESS.getMessage())
                .data(data)
                .build();
    }

    public static ApiResult success() {
        return ApiResult.builder()
                .build();
    }
}
