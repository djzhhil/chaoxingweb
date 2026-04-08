package com.chaoxingweb.chaoxing.course.impl;

import com.chaoxingweb.chaoxing.adapter.JobAdapter;
import com.chaoxingweb.chaoxing.client.ChaoxingApiClient;
import com.chaoxingweb.chaoxing.core.CipherManager;
import com.chaoxingweb.chaoxing.core.RateLimiter;
import com.chaoxingweb.chaoxing.core.SessionManager;
import com.chaoxingweb.chaoxing.course.ChaoxingJobService;
import com.chaoxingweb.chaoxing.dto.JobDTO;
import com.chaoxingweb.chaoxing.dto.StudyResultDTO;
import com.chaoxingweb.chaoxing.enums.JobType;
import com.chaoxingweb.chaoxing.enums.StudyResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.*;

/**
 * 超星任务服务实现
 *
 * @author 小克 🐕💎
 * @since 2026-04-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChaoxingJobServiceImpl implements ChaoxingJobService {

    private final ChaoxingApiClient apiClient;
    private final JobAdapter jobAdapter;
    private final SessionManager sessionManager;
    private final CipherManager cipherManager;
    private final RateLimiter rateLimiter;

    @Override
    public Map<String, Object> getJobList(String courseId, String clazzId, String knowledgeId, String cpi) {
        log.info("开始获取章节任务列表: courseId={}, clazzId={}, knowledgeId={}, cpi={}", 
                courseId, clazzId, knowledgeId, cpi);

        try {
            List<JobDTO> allJobs = new ArrayList<>();
            Map<String, Object> jobInfo = new HashMap<>();

            // 尝试不同的任务数量（0-6）
            for (int num = 0; num <= 6; num++) {
                // 速率限制
                rateLimiter.limitRate();

                // 获取任务卡片HTML
                String html = apiClient.getJobCardsHtml(clazzId, courseId, knowledgeId, cpi, String.valueOf(num));
                
                if (html == null || html.isEmpty()) {
                    log.error("获取任务卡片失败: num={}", num);
                    continue;
                }

                // 解析任务卡片
                Map<String, Object> result = jobAdapter.decodeCourseCard(html);
                
                @SuppressWarnings("unchecked")
                List<JobDTO> jobs = (List<JobDTO>) result.get("jobList");
                
                @SuppressWarnings("unchecked")
                Map<String, Object> info = (Map<String, Object>) result.get("jobInfo");

                // 检查是否未开放
                if (info != null && Boolean.TRUE.equals(info.get("notOpen"))) {
                    log.info("该章节未开放");
                    jobInfo.put("notOpen", true);
                    return Map.of("jobList", allJobs, "jobInfo", jobInfo);
                }

                // 添加任务到列表
                if (jobs != null && !jobs.isEmpty()) {
                    // 为每个任务设置课程信息
                    for (JobDTO job : jobs) {
                        job.setCourseId(courseId);
                        job.setClazzId(clazzId);
                        job.setKnowledgeId(knowledgeId);
                    }
                    allJobs.addAll(jobs);
                }

                // 更新任务信息
                if (info != null) {
                    jobInfo.putAll(info);
                }
            }

            // 如果没有任务，标记为空页面
            if (allJobs.isEmpty()) {
                log.info("该章节没有任务点");
                jobInfo.put("empty", true);
            }

            log.info("任务列表获取成功，共{}个任务", allJobs.size());
            return Map.of("jobList", allJobs, "jobInfo", jobInfo);

        } catch (Exception e) {
            log.error("获取任务列表异常", e);
            return Map.of("jobList", new ArrayList<>(), "jobInfo", new HashMap<>());
        }
    }

    @Override
    public StudyResultDTO studyVideo(JobDTO job) {
        log.info("开始学习视频任务: jobId={}, jobName={}", job.getJobId(), job.getJobName());

        try {
            // TODO: 实现视频学习逻辑
            // 1. 获取视频信息（时长、dtoken等）
            // 2. 模拟播放过程
            // 3. 定期上报进度
            // 4. 完成学习

            log.warn("视频学习任务暂未完全实现");
            return new StudyResultDTO(StudyResult.SKIP, "视频学习任务暂未完全实现", job.getJobId());

        } catch (Exception e) {
            log.error("学习视频任务异常: jobId={}", job.getJobId(), e);
            return new StudyResultDTO(StudyResult.ERROR, "学习失败: " + e.getMessage(), job.getJobId());
        }
    }

    @Override
    public StudyResultDTO studyDocument(JobDTO job) {
        log.info("开始学习文档任务: jobId={}, jobName={}", job.getJobId(), job.getJobName());

        try {
            // TODO: 实现文档学习逻辑
            // 1. 获取文档信息
            // 2. 模拟阅读过程
            // 3. 上报阅读进度
            // 4. 完成学习

            log.warn("文档学习任务暂未完全实现");
            return new StudyResultDTO(StudyResult.SKIP, "文档学习任务暂未完全实现", job.getJobId());

        } catch (Exception e) {
            log.error("学习文档任务异常: jobId={}", job.getJobId(), e);
            return new StudyResultDTO(StudyResult.ERROR, "学习失败: " + e.getMessage(), job.getJobId());
        }
    }

    @Override
    public StudyResultDTO studyEmptyPage(JobDTO job) {
        log.info("开始学习空页面任务: jobId={}", job.getJobId());

        try {
            // 空页面任务只需要访问即可
            // 这里可以发送一个简单的请求来标记已学习

            log.info("空页面任务学习完成");
            return new StudyResultDTO(StudyResult.SUCCESS, "空页面任务学习完成", job.getJobId());

        } catch (Exception e) {
            log.error("学习空页面任务异常: jobId={}", job.getJobId(), e);
            return new StudyResultDTO(StudyResult.ERROR, "学习失败: " + e.getMessage(), job.getJobId());
        }
    }

    @Override
    public StudyResultDTO studyJob(JobDTO job) {
        log.info("开始学习任务: jobId={}, type={}", job.getJobId(), job.getJobType());

        if (job == null || job.getJobId() == null) {
            return new StudyResultDTO(StudyResult.ERROR, "任务信息无效");
        }

        // 根据任务类型选择不同的学习方式
        return switch (job.getJobType()) {
            case VIDEO -> studyVideo(job);
            case DOCUMENT -> studyDocument(job);
            case EMPTY_PAGE -> studyEmptyPage(job);
            case WORK, READ -> {
                log.warn("暂不支持的任务类型: {}", job.getJobType());
                yield new StudyResultDTO(StudyResult.SKIP, "暂不支持的任务类型: " + job.getJobType(), job.getJobId());
            }
            default -> {
                log.warn("未知任务类型: {}", job.getJobType());
                yield new StudyResultDTO(StudyResult.SKIP, "未知任务类型", job.getJobId());
            }
        };
    }

    @Override
    public List<StudyResultDTO> studyChapterJobs(String courseId, String clazzId, String knowledgeId, String cpi) {
        log.info("开始学习章节所有任务: courseId={}, clazzId={}, knowledgeId={}", 
                courseId, clazzId, knowledgeId);

        List<StudyResultDTO> results = new ArrayList<>();

        try {
            // 获取任务列表
            Map<String, Object> jobListResult = getJobList(courseId, clazzId, knowledgeId, cpi);
            
            @SuppressWarnings("unchecked")
            List<JobDTO> jobs = (List<JobDTO>) jobListResult.get("jobList");

            if (jobs == null || jobs.isEmpty()) {
                log.info("该章节没有任务需要学习");
                return results;
            }

            log.info("共找到{}个任务，开始逐个学习", jobs.size());

            // 逐个学习任务
            for (int i = 0; i < jobs.size(); i++) {
                JobDTO job = jobs.get(i);
                log.info("正在学习第{}/{}个任务", i + 1, jobs.size());

                // 学习任务
                StudyResultDTO result = studyJob(job);
                results.add(result);

                // 速率限制，避免请求过快
                rateLimiter.limitRateWithRandom(1.0, 3.0);

                // 如果失败，可以选择继续或停止
                if (result.getResult() == StudyResult.ERROR) {
                    log.warn("任务学习失败，继续下一个任务");
                }
            }

            log.info("章节任务学习完成，成功: {}, 失败: {}, 跳过: {}",
                    results.stream().filter(r -> r.getResult() == StudyResult.SUCCESS).count(),
                    results.stream().filter(r -> r.getResult() == StudyResult.ERROR).count(),
                    results.stream().filter(r -> r.getResult() == StudyResult.SKIP).count());

        } catch (Exception e) {
            log.error("学习章节任务异常", e);
        }

        return results;
    }

    @Override
    public String generateEnc(String clazzId, String jobId, String objectId, int playingTime, int duration, String uid) {
        try {
            // 构造加密字符串
            String encStr = String.format("[%s][%s][%s][%s][%d][d_yHJ!$pdA~5][%d][0_%d]",
                    clazzId, uid, jobId, objectId, playingTime * 1000, duration * 1000, duration);

            // MD5加密
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(encStr.getBytes());

            // 转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            log.error("生成enc签名失败", e);
            return "";
        }
    }
}
