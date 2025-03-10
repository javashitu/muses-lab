package com.muses.recommend.persistence.ck.lab.repo;

import com.muses.recommend.persistence.ck.lab.entity.VideoItemCF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @ClassName VideoItemCFRepo
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/9 18:52
 */
public interface IVideoItemCFRepo extends JpaRepository<VideoItemCF, Long> {

    @Query(value = "SELECT video_id FROM video_itemcf where user_id = :userId order by score limit :topk", nativeQuery = true)
    List<Long> queryTopkVideoItemCF(@Param("userId") long userId, @Param("topk") int topk);
}
