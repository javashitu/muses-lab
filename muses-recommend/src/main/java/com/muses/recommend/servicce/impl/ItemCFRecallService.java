package com.muses.recommend.servicce.impl;

import com.muses.recommend.persistence.ck.lab.repo.IVideoItemCFRepo;
import com.muses.recommend.servicce.IItemRecallTask;
import com.muses.recommend.servicce.IItemRecallTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName ItemCFRecallService
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/9 18:57
 */
@Slf4j
@Component
public class ItemCFRecallService implements IItemRecallTaskService {
    @Autowired
    private IVideoItemCFRepo videoItemCFRepo;

    public List<Long> recallByUserId(long userId) {
        log.info("begin query video itemcf for user {}", userId);
        List<Long> videoIds = videoItemCFRepo.queryTopkVideoItemCF(userId, 10);
        log.info("itemcf recall video id is {}", videoIds);
        return videoIds;
    }

    public String getRecallType() {
        return "itemcf";
    }

    @Override
    public IItemRecallTask genRecallTask(long userId) {
        log.info("begin generate itemcf recall task");
        return () -> recallByUserId(userId);
    }
}
