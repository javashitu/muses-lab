package com.muses.recommend.servicce.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.muses.recommend.command.request.LabelQueryRequest;
import com.muses.recommend.persistence.ck.entity.VideoProgramStatistics;
import com.muses.recommend.persistence.ck.repo.VideoProgramStatisticsRepo;
import com.muses.recommend.servicce.IVideoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
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
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("entityManagerClickhouse")
    private EntityManager entityManager;


    @Transactional
    public String getVideoProgramStatistics(int pageNum) {
        List<VideoProgramStatistics> videoList = Lists.newArrayList();
        String resultStr;
        Pageable pageable = PageRequest.of(pageNum, 10, Sort.Direction.DESC, "videoId");
        Page<VideoProgramStatistics> page = videoProgramStatisticsRepo.findAll(pageable);
        log.info("query count total element {} , list size {}", page.getTotalElements(), page.getContent().size());
        videoList.addAll(page.getContent());
        try {
            resultStr = objectMapper.writeValueAsString(videoList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return resultStr;
    }

    public List<String> queryVideoProgramByLab(LabelQueryRequest labelQueryRequest){
        String querySql = labelQueryRequest.getLabelQueryInfo().formatSql();
        Query query = entityManager.createNativeQuery(querySql);
        List<Long> idLists = query.getResultList();
        return idLists.stream().map(value->String.valueOf(value)).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        System.out.println(-1 /10);
    }

}
