package com.chaoxingweb.chaoxing.service;

import com.chaoxingweb.chaoxing.client.ChaoxingApiClient;
import com.chaoxingweb.chaoxing.dto.JobDTO;
import com.chaoxingweb.chaoxing.dto.StudyResultDTO;
import com.chaoxingweb.chaoxing.enums.StudyResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 直播学习服务
 * 
 * 职责：
 * - 获取直播状态信息（总时长等）
 * - 提交直播观看时长
 * - 处理直播学习任务
 *
 * @author 小克 🐕💎
 * @since 2026-04-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiveService {

    private final ChaoxingApiClient apiClient;

    /**
     * 学习直播任务
     *
     * @param job 直播任务
     * @return 学习结果
     */
    public StudyResultDTO studyLive(JobDTO job) {
        log.info("📺 开始学习直播任务: {}, liveId={}", 
                job.getJobName(), job.getLiveId());

        try {
            // 1. 验证必要参数
            if (!validateLiveParams(job)) {
                return new StudyResultDTO(StudyResult.ERROR, 
                        "缺少必要的直播参数", job.getJobId());
            }

            // 2. 获取直播状态信息
            String courseId = extractCourseId(job);
            Object liveStatus = getLiveStatus(job, courseId);
            
            if (liveStatus != null) {
                log.info("直播状态信息: {}", liveStatus);
            }

            // 3. 提交直播观看时长
            boolean success = submitLiveDuration(job, courseId);

            if (success) {
                log.info("✅ 直播任务学习成功: {}", job.getJobName());
                return new StudyResultDTO(StudyResult.SUCCESS, 
                        "直播观看完成", job.getJobId());
            } else {
                log.warn("⚠️  直播时长提交失败: {}", job.getJobName());
                return new StudyResultDTO(StudyResult.ERROR, 
                        "直播时长提交失败", job.getJobId());
            }

        } catch (Exception e) {
            log.error("❌ 学习直播任务异常: {}", job.getJobName(), e);
            return new StudyResultDTO(StudyResult.ERROR, 
                    "学习异常: " + e.getMessage(), job.getJobId());
        }
    }

    /**
     * 验证直播必要参数
     *
     * @param job 直播任务
     * @return 是否有效
     */
    private boolean validateLiveParams(JobDTO job) {
        String streamName = job.getStreamName();
        String vdoid = job.getVdoid();
        String mid = job.getMid();

        if (streamName == null || streamName.isEmpty()) {
            log.warn("直播任务缺少 streamName");
            return false;
        }

        if (vdoid == null || vdoid.isEmpty()) {
            log.warn("直播任务缺少 vdoid");
            return false;
        }

        if (mid == null || mid.isEmpty()) {
            log.warn("直播任务缺少 mid");
            return false;
        }

        return true;
    }

    /**
     * 提取课程ID
     *
     * @param job 直播任务
     * @return 课程ID
     */
    private String extractCourseId(JobDTO job) {
        // 优先从job中获取
        if (job.getCourseId() != null && !job.getCourseId().isEmpty()) {
            return job.getCourseId();
        }

        // 尝试从otherinfo中提取
        String otherinfo = job.getOtherinfo();
        if (otherinfo != null && !otherinfo.isEmpty()) {
            String[] params = otherinfo.split("&");
            for (String param : params) {
                if (param.startsWith("courseId=")) {
                    return param.substring(9);
                }
            }
        }

        log.warn("无法提取课程ID");
        return "";
    }

    /**
     * 获取直播状态信息
     *
     * @param job 直播任务
     * @param courseId 课程ID
     * @return 直播状态信息
     */
    private Object getLiveStatus(JobDTO job, String courseId) {
        try {
            String liveId = job.getLiveId();
            String mid = job.getMid();
            String clazzId = job.getClazzId();
            String knowledgeId = job.getKnowledgeId();

            if (liveId == null || liveId.isEmpty()) {
                log.debug("直播任务没有liveId，跳过状态查询");
                return null;
            }

            // 构建状态查询URL
            String url = String.format(
                    "https://mooc1.chaoxing.com/ananas/live/liveinfo?liveid=%s&userid=%s&clazzid=%s&knowledgeid=%s&courseid=%s&jobid=%s&ut=s",
                    liveId, mid, clazzId, knowledgeId, courseId, job.getJobId()
            );

            // 调用API获取状态
            return apiClient.getLiveStatus(url);

        } catch (Exception e) {
            log.warn("获取直播状态失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 提交直播观看时长
     *
     * @param job 直播任务
     * @param courseId 课程ID
     * @return 是否成功
     */
    private boolean submitLiveDuration(JobDTO job, String courseId) {
        try {
            String streamName = job.getStreamName();
            String vdoid = job.getVdoid();
            String mid = job.getMid();

            // 构建时长提交URL
            long timestamp = System.currentTimeMillis();
            String url = String.format(
                    "https://zhibo.chaoxing.com/saveTimePc?streamName=%s&vdoid=%s&userId=%s&isStart=0&t=%d&courseId=%s",
                    streamName, vdoid, mid, timestamp, courseId
            );

            log.debug("提交直播时长: streamName={}, vdoid={}, userId={}", 
                    streamName, vdoid, mid);

            // 调用API提交时长
            return apiClient.submitLiveDuration(url);

        } catch (Exception e) {
            log.error("提交直播时长异常: {}", e.getMessage(), e);
            return false;
        }
    }
}
