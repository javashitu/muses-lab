package com.muses.recommend.servicce;

import java.util.Collection;

/**
 * @ClassName IRecallService
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/9 14:04
 */
public interface IRecallService {
    Collection<Long> recallVideo(long userId);
}
