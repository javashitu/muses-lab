package com.muses.recommend.enums;

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
     * yyy 模块 iterm 100
     * zzzz 错误码
     */
    SUCCESS("0","SUCCESS"),
    PARAM_WRONG("201000010", "param wrong");


    private final String code;

    private final String message;
}
