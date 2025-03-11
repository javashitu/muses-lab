package com.muses.recommend.persistence.milvus.repo.impl;

import com.google.common.collect.Lists;
import com.muses.recommend.common.util.JsonFormatter;
import com.muses.recommend.persistence.milvus.entity.VideoEmbedding;
import com.muses.recommend.persistence.milvus.repo.IVideoEmbeddingRepo;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.GetReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
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
    private JsonFormatter jsonFormatter;

    @Autowired
    private MilvusClientV2 milvusClient;

    private String getCollectionName() {
        return COLLECTION_NAME;
    }

    public List<VideoEmbedding> queryByVideoId(List<Object> videoIdList) {
        GetReq getReq = GetReq.builder()
                .collectionName(getCollectionName())
                .ids(videoIdList)
                .outputFields(List.of("video_embedding_vector"))
                .build();

        GetResp getResp = milvusClient.get(getReq);

        List<QueryResp.QueryResult> results = getResp.getGetResults();
        List<VideoEmbedding> videoEmbeddingList = Lists.newArrayList();
        for (QueryResp.QueryResult result : results) {
            log.info(jsonFormatter.object2Json(result.getEntity()));
            VideoEmbedding videoEmbedding = VideoEmbedding.deserialize(result);
            videoEmbeddingList.add(videoEmbedding);
        }
        return videoEmbeddingList;
    }

    @Override
    public List<Long> annSearch(List<Float> vector, int topK) {
        log.info("begin search topK {} vector for vector {}", topK, vector);
        FloatVec queryVector = new FloatVec(vector);
        SearchReq searchReq = SearchReq.builder()
                .collectionName(getCollectionName())
                .data(Collections.singletonList(queryVector))
                .topK(topK)
                .build();

        SearchResp searchResp = milvusClient.search(searchReq);
        log.info("search vector finished ");
        List<Long> result = Lists.newArrayList();
        List<List<SearchResp.SearchResult>> searchResults = searchResp.getSearchResults();
        for (List<SearchResp.SearchResult> results : searchResults) {
            for (SearchResp.SearchResult searchResult : results) {
                log.info("the most similar vector's video_id {} ", searchResult.getId());
                result.add((long) searchResult.getId());
            }
        }
        return result;
    }
}
