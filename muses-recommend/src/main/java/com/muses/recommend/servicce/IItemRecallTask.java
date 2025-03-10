package com.muses.recommend.servicce;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @ClassName IRecallTask
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/9 14:12
 */
public interface IItemRecallTask extends Callable<List<Long>> {
//    List<Long> recallByUserId(long userId);
}
