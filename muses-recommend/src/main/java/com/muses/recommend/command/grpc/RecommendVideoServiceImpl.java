package com.muses.recommend.command.grpc;

import com.google.common.collect.Lists;
import com.muses.recommend.service.IItemRecallTask;
import com.muses.recommend.service.IItemRecallTaskService;
import com.muses.recommend.service.IRecallService;
import com.muses.recommend.service.grpc.RecommendServiceGrpc;
import com.muses.recommend.service.grpc.RecommendVideoReq;
import com.muses.recommend.service.grpc.RecommendVideoRsp;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName RecommendVideoServiceImpl
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/11 16:12
 */
@Slf4j
@Component
@GrpcService
public class RecommendVideoServiceImpl extends RecommendServiceGrpc.RecommendServiceImplBase {

    @Autowired
    private IRecallService iRecallService;


    @Override
    public void recommendVideo4User(RecommendVideoReq req, StreamObserver<RecommendVideoRsp> responseObserver) {
        log.info("recommend video 4 user {}", req.getUserId());
        List<Long> result = iRecallService.recallVideo(req.getUserId());
        log.info("recommend video finished, video is {}", result);
        RecommendVideoRsp rsp = RecommendVideoRsp.newBuilder().addAllVideoIdList(result).build();
        responseObserver.onNext(rsp);
        responseObserver.onCompleted();
    }

}
