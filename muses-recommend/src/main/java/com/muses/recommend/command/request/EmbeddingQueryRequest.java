package com.muses.recommend.command.request;

import lombok.Data;

import java.util.List;

/**
 * @ClassName EmbeddingQueryRequest
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/8 16:37
 */
@Data
public class EmbeddingQueryRequest {
    private List<Long> videoIdList;
}
