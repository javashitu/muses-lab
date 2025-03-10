package com.muses.recommend.persistence.ck.warehouse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName VideoProgramStatistics
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2024/9/26 17:29
 */
@Data
@Table
@Entity(name = "video_program_statistics")
@NoArgsConstructor
public class VideoProgramStatistics {
    @Id
    @Column(name = "video_id")
    private String videoId;

    @Column(name = "video_name")
    private String videoName;

    @Column(name = "pub_time")
    private long pubTime;

    @Column(name = "pub_date")
    private String pubDate;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "play")
    private int play;

    @Column(name = "likes")
    private int likes;

}
