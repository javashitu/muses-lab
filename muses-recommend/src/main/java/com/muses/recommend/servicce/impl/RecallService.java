package com.muses.recommend.servicce.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.muses.recommend.common.enums.ServerErrorCodeEnums;
import com.muses.recommend.common.exception.ServerException;
import com.muses.recommend.servicce.IItemRecallTask;
import com.muses.recommend.servicce.IItemRecallTaskService;
import com.muses.recommend.servicce.IRecallService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName RecallService
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/9 14:05
 */
@Slf4j
@Component
public class RecallService implements IRecallService, InitializingBean, ApplicationContextAware {

    private final AtomicInteger threadCount = new AtomicInteger(0);

    private static final String THREAD_NAME_FORMAT = "recall-item-thread-%s";

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(16, 32, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), new ThreadFactory() {
        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(THREAD_NAME_FORMAT.formatted(threadCount.getAndIncrement()));
            return thread;
        }
    });

    private final ConcurrentMap<String, IItemRecallTaskService> RECALL_SERVICE_MAP = new ConcurrentHashMap<>();

    private ApplicationContext appContext;

    public Set<Long> recallVideo(long userId) {
        log.info("recall video for user {}", userId);
        try {
            List<IItemRecallTask> taskList = genTask(userId);
            List<Future<List<Long>>> taskFutureList = Lists.newArrayList();
            taskList.forEach(task -> {
                Future<List<Long>> future = executor.submit(task);
                taskFutureList.add(future);
            });
            Set<Long> recallVideoIdSet = waitTaskFinish(taskFutureList);
            log.info("the recall video id list is {} ", recallVideoIdSet);
            return recallVideoIdSet;
        } catch (Exception e) {
            log.error("recall video for user failure, userId {}", userId, e);
            throw ServerException.builder().serverErrorCodeEnums(ServerErrorCodeEnums.RECALL_VIDEO_FAILURE).build();
        }
    }

    private List<IItemRecallTask> genTask(long userId) {
        List<IItemRecallTask> taskList = Lists.newArrayList();
        RECALL_SERVICE_MAP.forEach((key, value) -> {
            taskList.add(value.genRecallTask(userId));
        });
        return taskList;
    }

    private Set<Long> waitTaskFinish(List<Future<List<Long>>> taskFutureList) throws ExecutionException, InterruptedException {
        int timeOutCount = 5;
        Set<Long> taskResult = Sets.newHashSet();
        while (timeOutCount > 0) {
            for (int i = taskFutureList.size() - 1; i > 0; i--) {
                if (taskFutureList.get(i).isDone()) {
                    taskResult.addAll(taskFutureList.get(i).get());
                    taskFutureList.remove(i);
                }
            }
            if (CollectionUtils.isEmpty(taskFutureList)) {
                log.info("all task has finished, not need wait");
                break;
            }
            Thread.sleep(100);
            timeOutCount--;
        }
        log.info("wait task end, finished task {} ,unfinished task {} ", taskResult.size(), taskFutureList.size());
        return taskResult;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        appContext.getBeansOfType(IItemRecallTaskService.class)
                .values()
                .forEach(handler -> RECALL_SERVICE_MAP.put(handler.getRecallType(), handler));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }
}
