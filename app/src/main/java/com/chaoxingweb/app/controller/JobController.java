package com.chaoxingweb.app.controller;

import com.chaoxingweb.chaoxing.course.ChaoxingJobService;
import com.chaoxingweb.chaoxing.dto.JobDTO;
import com.chaoxingweb.chaoxing.dto.StudyResultDTO;
import com.chaoxingweb.chaoxing.service.StudyProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务控制器
 *
 * @author 小克 🐕💎
 * @since 2026-04-08
 */
@RestController
@RequestMapping("/api/job")
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    private final ChaoxingJobService jobService;
    private final StudyProgressService progressService;

    @Autowired
    public JobController(ChaoxingJobService jobService, StudyProgressService progressService) {
        this.jobService = jobService;
        this.progressService = progressService;
    }

    /**
     * 获取章节任务列表
     *
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param knowledgeId 知识点ID（章节ID）
     * @param cpi CPI
     * @return 任务列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getJobList(
            @RequestParam String courseId,
            @RequestParam String clazzId,
            @RequestParam String knowledgeId,
            @RequestParam String cpi) {
        try {
            logger.info("收到获取任务列表请求: courseId={}, clazzId={}, knowledgeId={}", 
                    courseId, clazzId, knowledgeId);
            
            Map<String, Object> result = jobService.getJobList(courseId, clazzId, knowledgeId, cpi);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("获取任务列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 学习单个任务
     *
     * @param job 任务DTO
     * @return 学习结果
     */
    @PostMapping("/study")
    public ResponseEntity<StudyResultDTO> studyJob(@RequestBody JobDTO job) {
        try {
            logger.info("收到学习任务请求: jobId={}, type={}", job.getJobId(), job.getJobType());
            logger.info("任务详情 - objectId: {}, courseId: {}, clazzId: {}, otherInfo: {}", 
                    job.getObjectId(), job.getCourseId(), job.getClazzId(), job.getOtherinfo());
            
            StudyResultDTO result = jobService.studyJob(job);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("学习任务失败", e);
            StudyResultDTO error = new StudyResultDTO(
                    com.chaoxingweb.chaoxing.enums.StudyResult.ERROR, 
                    "学习任务失败: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 学习章节所有任务
     *
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param knowledgeId 知识点ID（章节ID）
     * @param cpi CPI
     * @return 学习结果列表
     */
    @PostMapping("/study/chapter")
    public ResponseEntity<List<StudyResultDTO>> studyChapterJobs(
            @RequestParam String courseId,
            @RequestParam String clazzId,
            @RequestParam String knowledgeId,
            @RequestParam String cpi) {
        try {
            logger.info("收到学习章节所有任务请求: courseId={}, clazzId={}, knowledgeId={}", 
                    courseId, clazzId, knowledgeId);
            
            List<StudyResultDTO> results = jobService.studyChapterJobs(courseId, clazzId, knowledgeId, cpi);
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            logger.error("学习章节任务失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 生成视频进度加密签名
     *
     * @param clazzId 班级ID
     * @param jobId 任务ID
     * @param objectId 对象ID
     * @param playingTime 播放时间（秒）
     * @param duration 总时长（秒）
     * @param uid 用户ID
     * @return 加密签名
     */
    @GetMapping("/enc")
    public ResponseEntity<Map<String, String>> generateEnc(
            @RequestParam String clazzId,
            @RequestParam String jobId,
            @RequestParam String objectId,
            @RequestParam int playingTime,
            @RequestParam int duration,
            @RequestParam String uid) {
        try {
            logger.trace("收到生成enc签名请求");
            
            String enc = jobService.generateEnc(clazzId, jobId, objectId, playingTime, duration, uid);
            
            Map<String, String> result = new HashMap<>();
            result.put("enc", enc);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("生成enc签名失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * SSE订阅学习进度
     *
     * @param jobId 任务ID
     * @return SSE连接
     */
    @GetMapping(value = "/progress/{jobId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeProgress(@PathVariable String jobId) {
        logger.info("客户端订阅学习进度: jobId={}", jobId);
        return progressService.createConnection(jobId);
    }
}
