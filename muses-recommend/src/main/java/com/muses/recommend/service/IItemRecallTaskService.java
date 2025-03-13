package com.muses.recommend.service;

/**
 * @ClassName IItemRecallService
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/9 15:21
 */
public interface IItemRecallTaskService {
    String getRecallType();

    IItemRecallTask genRecallTask(long userId);
}
