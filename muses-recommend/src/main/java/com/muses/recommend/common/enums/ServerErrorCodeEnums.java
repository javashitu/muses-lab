package com.muses.recommend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName ServerErrorCodeEnums
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2024/9/26 17:18
 */
@Getter
@AllArgsConstructor
public enum ServerErrorCodeEnums {
    /**
     * 非0值表示出现异常
     * 规则xx-yyy-zzzz
     * xx 系统 lab 20
     * yyy 模块 iterm 100, data analyze 200, recommend 300
     * zzzz 错误码
     */
    SUCCESS("0","SUCCESS"),
    PARAM_WRONG("201000010", "param wrong"),
    RECALL_VIDEO_FAILURE("203000010", "recall video failure"),
    SERVER_ERROR("201000001", "server error"),

    ;


    private final String code;

    private final String message;
}
