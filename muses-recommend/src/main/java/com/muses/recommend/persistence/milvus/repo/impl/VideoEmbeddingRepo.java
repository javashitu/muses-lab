package com.muses.recommend.persistence.milvus.repo.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.muses.recommend.persistence.milvus.entity.VideoEmbedding;
import com.muses.recommend.persistence.milvus.repo.IVideoEmbeddingRepo;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.GetReq;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.QueryResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName VideoEmbeddingRepo
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/8 14:58
 */
@Slf4j
@Component
public class VideoEmbeddingRepo implements IVideoEmbeddingRepo {
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MilvusClientV2 milvusClient;

    public List<VideoEmbedding> queryByVideoId(List<Object> videoIdList) {
        GetReq getReq = GetReq.builder()
                .collectionName(COLLECTION_NAME)
                .ids(videoIdList)
                .outputFields(List.of("video_embedding_vector"))
                .build();

        GetResp getResp = milvusClient.get(getReq);

        List<QueryResp.QueryResult> results = getResp.getGetResults();
        List<VideoEmbedding> videoEmbeddingList = Lists.newArrayList();
        for (QueryResp.QueryResult result : results) {
            try {
                log.info(mapper.writeValueAsString(result.getEntity()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            VideoEmbedding videoEmbedding = VideoEmbedding.deserialize(result);
            videoEmbeddingList.add(videoEmbedding);
        }
        return videoEmbeddingList;
    }
}
