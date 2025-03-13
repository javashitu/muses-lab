package com.muses.recommend.service;

import java.util.Collection;
import java.util.List;

/**
 * @ClassName IRecallService
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/9 14:04
 */
public interface IRecallService {
    List<Long> recallVideo(long userId);
}
