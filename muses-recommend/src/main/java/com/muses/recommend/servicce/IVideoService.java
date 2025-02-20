package com.muses.recommend.servicce;


import com.muses.recommend.command.request.LabelQueryRequest;

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
}
