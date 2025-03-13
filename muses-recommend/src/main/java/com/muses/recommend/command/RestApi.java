package com.muses.recommend.command;

import com.muses.recommend.command.request.EmbeddingQueryRequest;
import com.muses.recommend.command.request.LabelQueryRequest;
import com.muses.recommend.command.response.ApiResult;
import com.muses.recommend.command.response.LabelQueryResponse;
import com.muses.recommend.persistence.milvus.entity.VideoEmbedding;
import com.muses.recommend.service.IRecallService;
import com.muses.recommend.service.IVideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName RestApi
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2024/9/26 17:10
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/muses/recommend")
public class RestApi {

    @Autowired
    private IVideoService videoService;

    @Autowired
    private IRecallService recallService;


    @GetMapping(path = "/video/statistic/list/{pageNum}")
    public ApiResult listVideo(@PathVariable("pageNum") Integer pageNum) {
        String result = videoService.getVideoProgramStatistics(pageNum);
        return ApiResult.success(result);
    }

    //放到manager服务去更加合适
    @PostMapping("/video/label/query")
    public ApiResult queryByLabel(@RequestBody LabelQueryRequest request) {
        List<String> result = videoService.queryVideoProgramByLab(request);
        LabelQueryResponse labelQueryResponse = new LabelQueryResponse(result);
        return ApiResult.success(labelQueryResponse);
    }

    @PostMapping("/video/embedding/query")
    public ApiResult queryByLabel(@RequestBody EmbeddingQueryRequest request) {
        List<VideoEmbedding> result = videoService.queryVideoEmbeddingByLab(request);
        return ApiResult.success(result);
    }
}
