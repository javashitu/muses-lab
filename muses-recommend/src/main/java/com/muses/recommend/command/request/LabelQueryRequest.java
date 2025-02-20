package com.muses.recommend.command.request;

import com.muses.recommend.command.request.param.LabelQueryInfo;
import lombok.Data;

/**
 * @ClassName LabelQueryRequest
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2024/9/26 17:14
 */
@Data
public class LabelQueryRequest {
    private LabelQueryInfo labelQueryInfo;
}
