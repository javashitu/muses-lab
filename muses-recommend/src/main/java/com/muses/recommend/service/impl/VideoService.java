package com.muses.recommend.service.impl;

import com.google.common.collect.Lists;
import com.muses.recommend.command.request.EmbeddingQueryRequest;
import com.muses.recommend.command.request.LabelQueryRequest;
import com.muses.recommend.common.util.JsonFormatter;
import com.muses.recommend.persistence.ck.warehouse.entity.VideoProgramStatistics;
import com.muses.recommend.persistence.ck.warehouse.repo.VideoProgramStatisticsRepo;
import com.muses.recommend.persistence.milvus.entity.VideoEmbedding;
import com.muses.recommend.persistence.milvus.repo.impl.VideoEmbeddingRepo;
import com.muses.recommend.service.IVideoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName VideoService
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2024/9/26 17:24
 */
@Slf4j
@Component
public class VideoService implements IVideoService {

    @Autowired
    private VideoProgramStatisticsRepo videoProgramStatisticsRepo;

    @Autowired
    private JsonFormatter jsonFormatter;

    @Autowired
    @Qualifier("entityManagerClickhouse1")
    private EntityManager entityManager;

    @Autowired
    private VideoEmbeddingRepo videoEmbeddingRepo;

    @Transactional
    public String getVideoProgramStatistics(int pageNum) {
        List<VideoProgramStatistics> videoList = Lists.newArrayList();
        String resultStr;
        Pageable pageable = PageRequest.of(pageNum, 10, Sort.Direction.DESC, "videoId");
        Page<VideoProgramStatistics> page = videoProgramStatisticsRepo.findAll(pageable);
        log.info("query count total element {} , list size {}", page.getTotalElements(), page.getContent().size());
        videoList.addAll(page.getContent());
        resultStr = jsonFormatter.object2Json(videoList);
        return resultStr;
    }

    public List<String> queryVideoProgramByLab(LabelQueryRequest labelQueryRequest) {
        String querySql = labelQueryRequest.getLabelQueryInfo().formatSql();
        Query query = entityManager.createNativeQuery(querySql);
        List<Long> idLists = query.getResultList();
        return idLists.stream().map(value -> String.valueOf(value)).collect(Collectors.toList());
    }

    public List<VideoEmbedding> queryVideoEmbeddingByLab(EmbeddingQueryRequest embeddingQueryRequest) {
        if (CollectionUtils.isEmpty(embeddingQueryRequest.getVideoIdList())) {
            return videoEmbeddingRepo.queryByVideoId(Lists.newArrayList(764800, 764801));
        }
        return videoEmbeddingRepo.queryByVideoId((List) embeddingQueryRequest.getVideoIdList());
    }

}
