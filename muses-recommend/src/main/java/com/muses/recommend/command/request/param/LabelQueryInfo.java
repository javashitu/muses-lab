package com.muses.recommend.command.request.param;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @ClassName LabelQueryInfo
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2024/9/26 17:15
 */
@Data
@Slf4j
public class LabelQueryInfo {

    //只支持2层的嵌套查询，再多层的嵌套设计起来太麻烦了
    private String labelName;

    private List<String> labelValue;

    private List<LabelQueryInfo> andQuery;

    private List<LabelQueryInfo> orQuery;


    public static void main(String[] args) throws JsonProcessingException {
        LabelQueryInfo labelQueryInfo = new LabelQueryInfo();
        labelQueryInfo.setLabelName("resolution");
        labelQueryInfo.setLabelValue(Lists.newArrayList("360", "480"));
        String sqlStr;

        LabelQueryInfo labelQueryInfo2 = new LabelQueryInfo();
        labelQueryInfo2.setLabelName("frame_rate");
        labelQueryInfo2.setLabelValue(Lists.newArrayList("25", "50"));

        labelQueryInfo.setOrQuery(Lists.newArrayList(labelQueryInfo2));

        if (CollectionUtils.isEmpty(labelQueryInfo.getOrQuery())) {
            sqlStr = labelQueryInfo.formatAnd();
        } else if (CollectionUtils.isEmpty(labelQueryInfo.getAndQuery())) {
            sqlStr = labelQueryInfo.formatOr();
        } else {
            throw new UnsupportedOperationException("unsupported query, or query and and query only has one");
        }
        log.info(sqlStr);
    }

    public String formatSql() {
        String sqlStr;
        if (CollectionUtils.isEmpty(getOrQuery())) {
            sqlStr = formatAnd();
        } else if (CollectionUtils.isEmpty(getAndQuery())) {
            sqlStr = formatOr();
        } else {
            throw new UnsupportedOperationException("unsupported query, or query and and query only has one");
        }
        return sqlStr;
    }

    private String formatSubOr(List<String> queryValue) {
        StringBuilder queryCondition = new StringBuilder();
        String conditionFormat = "'%s'";
        for (String value : queryValue) {
            queryCondition.append(conditionFormat.formatted(value)).append(",");
        }
        return queryCondition.substring(0, queryCondition.length() - 1);
    }

    /**
     * orQuery is empty
     * 这里的sql拼接有问题，如果第二个query里查询出来的结果是空，在合并时会直接被忽略
     * 比如下面的sql理想因该是没有任何返回的，但是这里返回的是按照第一个查询条件 & 出来的结果，这种情况可以通过查询时限制查询的条件忽略
     * select bitmapCardinality(groupBitmapAndState(video_ids)) from video_tag_bitmap where (label_name = 'resolution' and label_value in ('360','480')) or (label_name = 'frame_rate' and label_value in ('25','50'))
     */
    private String formatAnd() {
        String conditionFormat = "(label_name = '%s' and label_value in (%s))";
        String sqlHead = "select arrayJoin(bitmapToArray(groupBitmapAndState(video_ids))) from video_tag_bitmap where ";

        StringBuilder queryBuilder = new StringBuilder(sqlHead + conditionFormat.formatted(getLabelName(), formatSubOr(getLabelValue())));
        if (CollectionUtils.isEmpty(getAndQuery())) {
            return queryBuilder.toString();
        }
        for (LabelQueryInfo labelQueryInfo : getAndQuery()) {
            queryBuilder.append(" or ").append(conditionFormat.formatted(labelQueryInfo.getLabelName(), formatSubOr(labelQueryInfo.getLabelValue())));
        }
        return queryBuilder.toString();
    }

    //andQuery is empty
    private String formatOr() {
        String conditionFormat = "(label_name = '%s' and label_value in (%s))";
        String sqlHead = "select arrayJoin(bitmapToArray(groupBitmapOrState(video_ids))) from video_tag_bitmap where ";

        StringBuilder queryBuilder = new StringBuilder(sqlHead + conditionFormat.formatted(getLabelName(), formatSubOr(getLabelValue())));
        if (CollectionUtils.isEmpty(getOrQuery())) {
            return queryBuilder.toString();
        }
        for (LabelQueryInfo labelQueryInfo : getOrQuery()) {
            queryBuilder.append(" or ").append(conditionFormat.formatted(labelQueryInfo.getLabelName(), formatSubOr(labelQueryInfo.getLabelValue())));
        }
        return queryBuilder.toString();
    }

}
