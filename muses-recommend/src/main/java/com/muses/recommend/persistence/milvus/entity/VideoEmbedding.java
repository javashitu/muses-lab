package com.muses.recommend.persistence.milvus.entity;

import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import java.util.List;

/**
 * @ClassName VideoEmbedding
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/8 15:20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoEmbedding {
    private long videoId;

    private List<Float> videoEmbeddingVector;

    public static VideoEmbedding deserialize(QueryResp.QueryResult result){
        if(MapUtils.isEmpty(result.getEntity())){
            return null;
        }
        long videoId = (Long) (result.getEntity().get("video_id"));
        List<Float> videoEmbeddingVector = (List<Float>)result.getEntity().get("video_embedding_vector");
        return new VideoEmbedding(videoId, videoEmbeddingVector);
    }

    public static VideoEmbedding deserialize(SearchResp.SearchResult result){
        if(MapUtils.isEmpty(result.getEntity())){
            return null;
        }
        long videoId = (Long) (result.getEntity().get("video_id"));
        List<Float> videoEmbeddingVector = (List<Float>)result.getEntity().get("video_embedding_vector");
        return new VideoEmbedding(videoId, videoEmbeddingVector);
    }
}
