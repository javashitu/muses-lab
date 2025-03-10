package com.muses.recommend.persistence.milvus.repo;

import com.muses.recommend.persistence.milvus.entity.VideoEmbedding;

import java.util.List;

/**
 * @ClassName IVideoEmbedding
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/8 14:57
 */
public interface IVideoEmbeddingRepo {
    String COLLECTION_NAME = "video_embedding";

    List<VideoEmbedding> queryByVideoId(List<Object> videoIdList);

    List<Long> annSearch(List<Float> vector, int topK);
}
