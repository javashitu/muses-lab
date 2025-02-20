package com.muses.recommend.command.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName LabelQueryResponse
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2024/9/26 17:21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabelQueryResponse {

    private List<String> uidList;

}
