package com.muses.recommend.persistence.ck.lab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName VideoItemCF
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/9 18:51
 */
@Data
@Table
@Entity(name = "video_item_cf")
@NoArgsConstructor
public class VideoItemCF {
    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "video_id")
    private String videoId;

    @Column(name = "score")
    private double score;

}
