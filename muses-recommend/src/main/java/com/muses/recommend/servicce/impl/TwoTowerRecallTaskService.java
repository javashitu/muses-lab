package com.muses.recommend.servicce.impl;

import com.google.common.collect.Lists;
import com.muses.recommend.persistence.milvus.entity.VideoEmbedding;
import com.muses.recommend.persistence.milvus.repo.IVideoEmbeddingRepo;
import com.muses.recommend.servicce.IItemRecallTask;
import com.muses.recommend.servicce.IItemRecallTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @ClassName TwoTowerRecallService
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/9 14:14
 */
@Slf4j
@Component
public class TwoTowerRecallTaskService implements IItemRecallTaskService {

    @Autowired
    private IVideoEmbeddingRepo videoEmbeddingRepo;

    public List<Long> recallByUserId(long userId) {
        log.info("begin query user embedding vector for user {}", userId);
        //TODO 这里应该替换成从user tower查出来的向量
        List<VideoEmbedding> videoEmbeddingList = videoEmbeddingRepo.queryByVideoId(Lists.newArrayList(764800));
        if (CollectionUtils.isEmpty(videoEmbeddingList)) {
            log.info("no nay embedding vector for user {}", userId);
            return Collections.emptyList();
        }
        VideoEmbedding videoEmbedding = videoEmbeddingList.get(0);
        log.info("do search the similar vector for vector {}", videoEmbedding.getVideoEmbeddingVector());
        List<Long> recallVideoEmbedding = videoEmbeddingRepo.annSearch(videoEmbedding.getVideoEmbeddingVector(), 10);
        if (CollectionUtils.isEmpty(recallVideoEmbedding)) {
            return Collections.emptyList();
        }
        return recallVideoEmbedding;
    }

    public String getRecallType(){
        return "twoTower";
    }

    public IItemRecallTask genRecallTask(long userId) {
        log.info("generate tow tower task");
        return () -> recallByUserId(userId);
    }
}
