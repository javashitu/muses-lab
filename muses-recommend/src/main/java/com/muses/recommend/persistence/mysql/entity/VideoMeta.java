package com.muses.recommend.persistence.mysql.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName VideoMeta
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2024/9/29 10:13
 */
@Data
@Table
@Entity
@NoArgsConstructor
public class VideoMeta {

    @Id
    private String id;

    private String fileName;

    private String tags;

    //格式mp4, avi
    private String format;

    //分辨率
    private String resolution;

    //比特率 kbps
    private int bitRate;

    //帧率
    private String frameRate;


    private String videoCodec;

    private String audioCodec;

    private int size;

    //时长，seconds
    private int duration;

}
