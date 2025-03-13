package com.muses.recommend.service;


import com.muses.recommend.command.request.EmbeddingQueryRequest;
import com.muses.recommend.command.request.LabelQueryRequest;
import com.muses.recommend.persistence.milvus.entity.VideoEmbedding;

import java.util.List;

/**
 * @ClassName IVideoService
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2024/9/26 17:11
 */
public interface IVideoService {
    String getVideoProgramStatistics(int pageNum);

    List<String> queryVideoProgramByLab(LabelQueryRequest labelQueryRequest);

    List<VideoEmbedding> queryVideoEmbeddingByLab(EmbeddingQueryRequest labelQueryRequest);
}
