package com.muses.recommend.persistence.mysql.repo;

import com.muses.recommend.persistence.mysql.entity.VideoMeta;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @ClassName VideoMetaRepo
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2024/9/29 10:15
 */
public interface VideoMetaRepo extends JpaRepository<VideoMeta, String> {
}
