package com.chaoxingweb.chaoxing.course;

import com.chaoxingweb.chaoxing.dto.JobDTO;
import com.chaoxingweb.chaoxing.dto.StudyResultDTO;

import java.util.List;
import java.util.Map;

/**
 * 超星任务服务接口
 *
 * @author 小克 🐕💎
 * @since 2026-04-08
 */
public interface ChaoxingJobService {

    /**
     * 获取章节任务列表
     *
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param knowledgeId 知识点ID（章节ID）
     * @param cpi CPI
     * @return 任务列表和任务信息
     */
    Map<String, Object> getJobList(String courseId, String clazzId, String knowledgeId, String cpi);

    /**
     * 学习视频任务
     *
     * @param job 任务DTO
     * @return 学习结果
     */
    StudyResultDTO studyVideo(JobDTO job);

    /**
     * 学习文档任务
     *
     * @param job 任务DTO
     * @return 学习结果
     */
    StudyResultDTO studyDocument(JobDTO job);

    /**
     * 学习阅读任务
     *
     * @param job 任务DTO
     * @return 学习结果
     */
    StudyResultDTO studyRead(JobDTO job);

    /**
     * 学习测验任务（章节测验）
     *
     * @param job 任务DTO
     * @return 学习结果
     */
    StudyResultDTO studyWork(JobDTO job);

    /**
     * 学习空页面任务
     *
     * @param job 任务DTO
     * @return 学习结果
     */
    StudyResultDTO studyEmptyPage(JobDTO job);

    /**
     * 学习单个任务
     *
     * @param job 任务DTO
     * @return 学习结果
     */
    StudyResultDTO studyJob(JobDTO job);

    /**
     * 学习章节所有任务
     *
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param knowledgeId 知识点ID（章节ID）
     * @param cpi CPI
     * @return 学习结果列表
     */
    List<StudyResultDTO> studyChapterJobs(String courseId, String clazzId, String knowledgeId, String cpi);

    /**
     * 生成视频进度加密签名
     *
     * @param clazzId 班级ID
     * @param jobId 任务ID
     * @param objectId 对象ID
     * @param playingTime 播放时间（秒）
     * @param duration 总时长（秒）
     * @param uid 用户ID
     * @return MD5加密签名
     */
    String generateEnc(String clazzId, String jobId, String objectId, int playingTime, int duration, String uid);
}
