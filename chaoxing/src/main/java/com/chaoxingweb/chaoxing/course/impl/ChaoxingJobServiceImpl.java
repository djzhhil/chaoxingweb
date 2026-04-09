package com.chaoxingweb.chaoxing.course.impl;

import com.chaoxingweb.auth.entity.User;
import com.chaoxingweb.auth.repository.UserRepository;
import com.chaoxingweb.auth.service.LoginService;
import com.chaoxingweb.chaoxing.adapter.JobAdapter;
import com.chaoxingweb.chaoxing.adapter.WorkAdapter;
import com.chaoxingweb.chaoxing.client.ChaoxingApiClient;
import com.chaoxingweb.chaoxing.core.CipherManager;
import com.chaoxingweb.chaoxing.core.RateLimiter;
import com.chaoxingweb.chaoxing.core.SessionManager;
import com.chaoxingweb.chaoxing.course.ChaoxingJobService;
import com.chaoxingweb.chaoxing.dto.*;
import com.chaoxingweb.chaoxing.enums.JobType;
import com.chaoxingweb.chaoxing.enums.StudyResult;
import com.chaoxingweb.chaoxing.service.LiveService;
import com.chaoxingweb.chaoxing.service.StudyProgressService;
import com.chaoxingweb.chaoxing.tiku.AnswerMatcher;
import com.chaoxingweb.chaoxing.tiku.TikuService;
import com.chaoxingweb.chaoxing.tiku.WorkAnswerManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final WorkAdapter workAdapter;
    private final SessionManager sessionManager;
    private final CipherManager cipherManager;
    private final RateLimiter rateLimiter;
    private final StudyProgressService progressService; // 注入进度服务
    private final LoginService loginService; // 注入登录服务,用于恢复会话
    private final UserRepository userRepository; // 注入用户仓库,获取超星Cookie
    private final TikuService tikuService; // 注入题库服务
    private final AnswerMatcher answerMatcher; // 注入答案匹配器
    private final LiveService liveService; // 注入直播服务
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Random random = new Random();
    
    // 视频学习配置
    private static final double THRESHOLD = 0.5; // 每次循环的时间间隔（秒）
    private static final int MAX_FORBIDDEN_RETRY = 2; // 最大403重试次数

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
        log.debug("任务详情 - objectId: {}, courseId: {}, clazzId: {}", 
                job.getObjectId(), job.getCourseId(), job.getClazzId());

        try {
            // 检查并恢复会话状态(如果fid缺失)
            ensureSessionValid();
            
            // 检查objectId是否正确（应该是MD5格式，32位十六进制）
            String objectId = job.getObjectId();
            if (objectId != null && objectId.matches("^\\d+$") && objectId.length() > 20) {
                // objectId是纯数字且很长，可能是mid，需要修正
                log.warn("检测到objectId可能是mid（纯数字），尝试从API获取正确的objectId");
                
                // 重新获取任务列表以获取正确的objectId
                Map<String, Object> jobListResult = getJobList(
                        job.getCourseId(), 
                        job.getClazzId(), 
                        extractKnowledgeIdFromOtherInfo(job.getOtherinfo()),
                        extractCpiFromOtherInfo(job.getOtherinfo())
                );
                
                @SuppressWarnings("unchecked")
                List<JobDTO> jobs = (List<JobDTO>) jobListResult.get("jobList");
                
                if (jobs != null && !jobs.isEmpty()) {
                    // 找到匹配的任务
                    for (JobDTO correctJob : jobs) {
                        if (correctJob.getJobId().equals(job.getJobId())) {
                            log.info("找到正确的objectId: {}", correctJob.getObjectId());
                            job.setObjectId(correctJob.getObjectId());
                            break;
                        }
                    }
                }
            }
            
            // 1. 获取视频信息（时长、dtoken等）
            String fid = sessionManager.getFid();
            String uid = sessionManager.getUid();
            
            log.info("会话信息 - fid: {}, uid: {}", fid, uid);
            
            if (fid == null || fid.isEmpty()) {
                log.error("未获取到FID，sessionManager.getFid() 返回: {}", fid);
                return new StudyResultDTO(StudyResult.ERROR, "未获取到FID", job.getJobId());
            }
            
            if (uid == null || uid.isEmpty()) {
                log.warn("未获取到UID，但继续尝试");
            }
            
            if (job.getObjectId() == null || job.getObjectId().isEmpty()) {
                log.error("job.getObjectId() 为空");
                return new StudyResultDTO(StudyResult.ERROR, "任务缺少objectId", job.getJobId());
            }

            log.info("调用 getMediaStatus: objectId={}, fid={}", job.getObjectId(), fid);
            String mediaStatusJson = apiClient.getMediaStatus(job.getObjectId(), fid, true);
            
            if (mediaStatusJson == null) {
                log.error("getMediaStatus 返回 null");
                return new StudyResultDTO(StudyResult.ERROR, "获取视频状态失败", job.getJobId());
            }
            
            log.debug("媒体状态响应: {}", mediaStatusJson);

            JsonNode videoInfo = objectMapper.readTree(mediaStatusJson);
            String status = videoInfo.path("status").asText();
            
            log.info("视频状态: {}", status);
            
            if (!"success".equals(status)) {
                log.error("视频状态异常: {}, 完整响应: {}", status, mediaStatusJson);
                return new StudyResultDTO(StudyResult.ERROR, "视频状态异常: " + status, job.getJobId());
            }

            String dtoken = videoInfo.path("dtoken").asText();
            int duration = videoInfo.path("duration").asInt();
            
            // 已播放时间（毫秒转秒）
            int playTime = job.getPlayingTime() > 0 ? (int)(job.getPlayingTime() / 1000) : 0;
            
            log.info("视频信息 - 总时长: {}s, 已播放: {}s, dtoken: {}", duration, playTime, dtoken);

            // 2. 检查是否已经完成
            if (playTime >= duration) {
                log.info("视频已完成，跳过学习");
                return new StudyResultDTO(StudyResult.SUCCESS, "视频已完成", job.getJobId());
            }

            // 3. 模拟播放过程并上报进度
            StudyResult result = simulateVideoPlayback(job, dtoken, duration, playTime);
            
            if (result == StudyResult.SUCCESS) {
                return new StudyResultDTO(StudyResult.SUCCESS, "视频学习完成", job.getJobId());
            } else if (result == StudyResult.SKIP) {
                return new StudyResultDTO(StudyResult.SKIP, "视频学习被跳过", job.getJobId());
            } else {
                return new StudyResultDTO(StudyResult.ERROR, "视频学习失败", job.getJobId());
            }

        } catch (Exception e) {
            log.error("学习视频任务异常: jobId={}", job.getJobId(), e);
            log.error("异常类型: {}", e.getClass().getName());
            log.error("异常消息: {}", e.getMessage());
            return new StudyResultDTO(StudyResult.ERROR, "学习失败: " + e.getMessage(), job.getJobId());
        }
    }

    /**
     * 模拟视频播放过程
     *
     * @param job 任务信息
     * @param dtoken 文档token
     * @param duration 视频总时长（秒）
     * @param initialPlayTime 初始播放时间（秒）
     * @return 学习结果
     */
    private StudyResult simulateVideoPlayback(JobDTO job, String dtoken, int duration, int initialPlayTime) {
        double playTimeDouble = initialPlayTime; // 使用double类型累积
        int playTime = initialPlayTime; // 用于上报的整数
        int lastLogTime = 0;
        long lastIterTime = System.currentTimeMillis(); // 在循环外初始化
        int waitTime = 30 + random.nextInt(61); // 30-90秒随机
        int forbiddenRetry = 0;
        int lastPrintedPercent = -1; // 记录上次输出的进度百分比

        log.info("开始模拟播放: 总时长={}s, 起始位置={}s", duration, initialPlayTime);

        // 首次上报两次进度（对照Python实现）
        Object[] result1 = reportVideoProgress(job, dtoken, duration, playTime);
        boolean passed = (boolean) result1[0];
        int statusCode = (int) result1[1];
        
        // 第二次上报到结尾
        if (!passed) {
            Object[] result2 = reportVideoProgress(job, dtoken, duration, duration);
            passed = (boolean) result2[0];
            statusCode = (int) result2[1];
        }
        
        if (passed) {
            log.info("✅ 任务瞬间完成: {}", job.getJobName());
            return StudyResult.SUCCESS;
        }

        // 循环模拟播放
        int loopCount = 0;
        while (!passed) {
            loopCount++;
            playTime = (int)playTimeDouble; // 每次循环开始时转换
            
            // 检查是否需要上报进度
            if (playTime - lastLogTime >= waitTime || playTime >= duration) {
                
                Object[] result = reportVideoProgress(job, dtoken, duration, playTime);
                passed = (boolean) result[0];
                statusCode = (int) result[1];
                
                if (!passed) {
                    // 处理403错误
                    if (statusCode == 403) {
                        if (forbiddenRetry >= MAX_FORBIDDEN_RETRY) {
                            log.warn("403重试失败, 跳过当前任务");
                            return StudyResult.SKIP;
                        }
                        
                        forbiddenRetry++;
                        log.warn("出现403报错, 正在尝试刷新会话状态 (第{}次)", forbiddenRetry);
                        
                        try {
                            Thread.sleep((long)(random.nextDouble() * 2000 + 2000)); // 2-4秒
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return StudyResult.ERROR;
                        }
                        
                        // 先更新Cookie，再刷新视频状态（对照Python实现）
                        sessionManager.updateCookie(sessionManager.getCookie());
                        Map<String, Object> refreshedMeta = refreshVideoStatus(job);
                        if (refreshedMeta != null) {
                            dtoken = (String) refreshedMeta.getOrDefault("dtoken", dtoken);
                            duration = (int) refreshedMeta.getOrDefault("duration", duration);
                            playTimeDouble = (int) refreshedMeta.getOrDefault("playTime", playTimeDouble); // 更新double
                            playTime = (int)playTimeDouble; // 同步更新int
                            log.debug("刷新成功 - dtoken: {}, duration: {}, playTime: {}", dtoken, duration, playTime);
                            continue;
                        }
                        
                        log.error("刷新会话状态失败，跳过当前任务");
                        return StudyResult.SKIP;
                    } else if (statusCode != 200) {
                        // 其他错误
                        log.error("视频进度上报失败，状态码: {}", statusCode);
                        return StudyResult.ERROR;
                    }
                }
                
                waitTime = 30 + random.nextInt(61); // 重新设置等待时间
                lastLogTime = playTime;
            }

            // 计算经过的时间（考虑播放速度）
            long currentTime = System.currentTimeMillis();
            double dt = (currentTime - lastIterTime) / 1000.0; // 转换为秒
            lastIterTime = currentTime; // 更新为当前时间，供下次循环使用
            
            // 对照Python实现，需要乘以倍速（默认为1.0），累积到double变量
            double speed = 1.0; // TODO: 可以从配置中读取
            playTimeDouble = Math.min(duration, playTimeDouble + dt * speed);
            playTime = (int)playTimeDouble; // 转换为整数
            
            // 显示进度条（只有当进度变化时才输出）
            int percentComplete = duration > 0 ? (int)((long)playTime * 100 / duration) : 100;
            if (percentComplete != lastPrintedPercent) {
                com.chaoxingweb.chaoxing.util.ProgressBar.showProgress(
                        job.getJobName(), playTime, duration, percentComplete);
                lastPrintedPercent = percentComplete;
                
                // 推送SSE进度
                StudyProgress progress = StudyProgress.builder()
                        .jobId(job.getJobId())
                        .jobName(job.getJobName())
                        .jobType("VIDEO")
                        .currentTime(playTime)
                        .totalTime(duration)
                        .percent(percentComplete)
                        .status("STUDYING")
                        .timestamp(System.currentTimeMillis())
                        .build();
                progressService.pushProgress(job.getJobId(), progress);
            }
            
            // 速率限制
            try {
                Thread.sleep((long)(THRESHOLD * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return StudyResult.ERROR;
            }
        }

        // 显示完成消息
        com.chaoxingweb.chaoxing.util.ProgressBar.showComplete(job.getJobName());
        
        // 推送完成状态
        StudyProgress completeProgress = StudyProgress.builder()
                .jobId(job.getJobId())
                .jobName(job.getJobName())
                .jobType("VIDEO")
                .currentTime(duration)
                .totalTime(duration)
                .percent(100)
                .status("COMPLETED")
                .timestamp(System.currentTimeMillis())
                .build();
        progressService.pushProgress(job.getJobId(), completeProgress);
        
        // 关闭SSE连接
        progressService.closeConnection(job.getJobId());
        
        return StudyResult.SUCCESS;
    }

    /**
     * 上报视频进度
     *
     * @param job 任务信息
     * @param dtoken 文档token
     * @param duration 视频总时长
     * @param playingTime 当前播放时间
     * @return 上报结果数组 [isPassed, statusCode]
     */
    private Object[] reportVideoProgress(JobDTO job, String dtoken, int duration, int playingTime) {
        try {
            // 速率限制
            rateLimiter.limitRateWithRandom(0.5, 2.0);

            String clazzId = job.getClazzId();
            String courseId = job.getCourseId();
            String uid = sessionManager.getUid();
            
            if (clazzId == null || courseId == null || uid == null) {
                log.error("缺少必要参数: clazzId={}, courseId={}, uid={}", clazzId, courseId, uid);
                return new Object[]{false, 0};
            }

            // 生成enc签名
            String enc = generateEnc(clazzId, job.getJobId(), job.getObjectId(), playingTime, duration, uid);

            // 构造请求参数
            Map<String, String> params = new HashMap<>();
            params.put("clazzId", clazzId);
            params.put("playingTime", String.valueOf(playingTime));
            params.put("duration", String.valueOf(duration));
            params.put("clipTime", "0_" + duration);
            params.put("objectId", job.getObjectId());
            params.put("otherInfo", job.getOtherinfo());
            params.put("courseId", courseId);
            params.put("jobid", job.getJobId());
            params.put("userid", uid);
            params.put("isdrag", "3");
            params.put("view", "pc");
            params.put("enc", enc);
            params.put("dtype", "Video");

            // 添加可选参数
            if (job.getVideoFaceCaptureEnc() != null && !job.getVideoFaceCaptureEnc().isEmpty()) {
                params.put("videoFaceCaptureEnc", job.getVideoFaceCaptureEnc());
            }
            if (job.getAttDuration() != null && !job.getAttDuration().isEmpty()) {
                params.put("attDuration", job.getAttDuration());
            }
            if (job.getAttDurationEnc() != null && !job.getAttDurationEnc().isEmpty()) {
                params.put("attDurationEnc", job.getAttDurationEnc());
            }

            // 提取 CPI
            String cpi = extractCpiFromOtherInfo(job.getOtherinfo());
            if (cpi == null) {
                log.error("无法从 otherInfo 中提取 CPI");
                return new Object[]{false, 0};
            }

            // 处理rt参数（对照Python实现）
            String rt = job.getRt();
            if (rt == null || rt.isEmpty()) {
                // 从 otherInfo 中提取 rt
                Pattern pattern = Pattern.compile("-rt_([1d])");
                Matcher matcher = pattern.matcher(job.getOtherinfo());
                if (matcher.find()) {
                    String rtChar = matcher.group(1);
                    rt = "d".equals(rtChar) ? "0.9" : "1";
                    log.trace("从 otherInfo 中获取 rt: {}", rt);
                }
            }

            // 如果有rt值，直接尝试
            if (rt != null && !rt.isEmpty()) {
                params.put("rt", rt);
                params.put("_t", String.valueOf(System.currentTimeMillis()));
                
                String response = apiClient.reportVideoProgress(cpi, dtoken, params);
                
                if (response != null) {
                    JsonNode jsonResponse = objectMapper.readTree(response);
                    boolean isPassed = jsonResponse.path("isPassed").asBoolean(false);
                    log.debug("视频进度上报成功 - isPassed: {}, rt: {}", isPassed, rt);
                    return new Object[]{isPassed, 200};
                } else {
                    log.warn("视频进度上报返回 null");
                    return new Object[]{false, 0};
                }
            } else {
                // 没有rt值，循环尝试 0.9 和 1（对照Python实现）
                log.warn("未能获取 rt 参数，尝试默认值 0.9 和 1");
                for (String rtValue : new String[]{"0.9", "1"}) {
                    params.put("rt", rtValue);
                    params.put("_t", String.valueOf(System.currentTimeMillis()));
                    
                    String response = apiClient.reportVideoProgress(cpi, dtoken, params);
                    
                    if (response != null) {
                        JsonNode jsonResponse = objectMapper.readTree(response);
                        boolean isPassed = jsonResponse.path("isPassed").asBoolean(false);
                        
                        if (isPassed) {
                            log.info("视频进度上报成功 - rt: {}", rtValue);
                            return new Object[]{true, 200};
                        }
                        // 如果返回403，继续尝试下一个rt值
                        log.debug("rt={} 未通过，尝试下一个值", rtValue);
                    } else {
                        log.warn("rt={} 时上报返回 null", rtValue);
                    }
                }
                
                log.warn("所有 rt 值都未通过");
                return new Object[]{false, 403};
            }

        } catch (Exception e) {
            log.error("上报视频进度异常", e);
            return new Object[]{false, 0};
        }
    }

    /**
     * 刷新视频状态
     *
     * @param job 任务信息
     * @return 刷新后的元数据
     */
    private Map<String, Object> refreshVideoStatus(JobDTO job) {
        try {
            rateLimiter.limitRateWithRandom(0.1, 0.2);
            
            String fid = sessionManager.getFid();
            if (fid == null) {
                return null;
            }

            String mediaStatusJson = apiClient.getMediaStatus(job.getObjectId(), fid, true);
            if (mediaStatusJson == null) {
                return null;
            }

            JsonNode videoInfo = objectMapper.readTree(mediaStatusJson);
            if ("success".equals(videoInfo.path("status").asText())) {
                Map<String, Object> result = new HashMap<>();
                result.put("dtoken", videoInfo.path("dtoken").asText());
                result.put("duration", videoInfo.path("duration").asInt());
                result.put("playTime", videoInfo.path("playTime").asInt() / 1000); // 毫秒转秒
                return result;
            }

            return null;

        } catch (Exception e) {
            log.debug("刷新视频状态失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 otherInfo 中提取 CPI
     *
     * @param otherInfo otherInfo 字符串
     * @return CPI
     */
    private String extractCpiFromOtherInfo(String otherInfo) {
        if (otherInfo == null || otherInfo.isEmpty()) {
            return null;
        }
        
        // 尝试多种格式：cpi_475073453 或 cpi=475073453
        Pattern pattern = Pattern.compile("cpi[_=](\\d+)");
        Matcher matcher = pattern.matcher(otherInfo);
        if (matcher.find()) {
            String cpi = matcher.group(1);
            log.debug("从 otherInfo 中提取到 CPI: {}", cpi);
            return cpi;
        }
        
        log.warn("无法从 otherInfo 中提取 CPI: {}", otherInfo);
        return null;
    }

    @Override
    public StudyResultDTO studyDocument(JobDTO job) {
        log.info("开始学习文档任务: jobId={}, jobName={}", job.getJobId(), job.getJobName());

        try {
            // 1. 获取知识点ID
            String knowledgeId = extractKnowledgeIdFromOtherInfo(job.getOtherinfo());
            if (knowledgeId == null) {
                log.warn("无法从 otherInfo 中提取 knowledgeId，尝试使用默认值");
                knowledgeId = job.getKnowledgeId();
            }
            
            if (knowledgeId == null || knowledgeId.isEmpty()) {
                return new StudyResultDTO(StudyResult.ERROR, "缺少 knowledgeId", job.getJobId());
            }

            // 2. 调用文档学习接口
            boolean success = apiClient.completeDocumentStudy(
                    job.getJobId(),
                    knowledgeId,
                    job.getCourseId(),
                    job.getClazzId(),
                    job.getJtoken()
            );

            if (success) {
                log.info("文档任务学习完成: {}", job.getJobName());
                return new StudyResultDTO(StudyResult.SUCCESS, "文档学习完成", job.getJobId());
            } else {
                log.error("文档任务学习失败: {}", job.getJobName());
                return new StudyResultDTO(StudyResult.ERROR, "文档学习失败", job.getJobId());
            }

        } catch (Exception e) {
            log.error("学习文档任务异常: jobId={}", job.getJobId(), e);
            return new StudyResultDTO(StudyResult.ERROR, "学习失败: " + e.getMessage(), job.getJobId());
        }
    }

    /**
     * 从 otherInfo 中提取 knowledgeId（nodeId）
     *
     * @param otherInfo otherInfo 字符串
     * @return knowledgeId
     */
    private String extractKnowledgeIdFromOtherInfo(String otherInfo) {
        if (otherInfo == null || otherInfo.isEmpty()) {
            return null;
        }
        
        Pattern pattern = Pattern.compile("nodeId_(.*?)-");
        Matcher matcher = pattern.matcher(otherInfo);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }

    @Override
    public StudyResultDTO studyRead(JobDTO job) {
        log.info("开始学习阅读任务: jobId={}, jobName={}", job.getJobId(), job.getJobName());

        try {
            // 1. 获取知识点ID
            String knowledgeId = job.getKnowledgeId();
            if (knowledgeId == null || knowledgeId.isEmpty()) {
                // 尝试从 otherInfo 中提取
                knowledgeId = extractKnowledgeIdFromOtherInfo(job.getOtherinfo());
            }
            
            if (knowledgeId == null || knowledgeId.isEmpty()) {
                log.warn("无法获取 knowledgeId，使用默认值");
                knowledgeId = "0";
            }

            // 2. 调用阅读学习接口
            boolean success = apiClient.completeReadStudy(
                    job.getJobId(),
                    knowledgeId,
                    job.getCourseId(),
                    job.getClazzId(),
                    job.getJtoken()
            );

            if (success) {
                log.info("✅ 阅读任务学习完成: {}", job.getJobName());
                return new StudyResultDTO(StudyResult.SUCCESS, "阅读学习完成", job.getJobId());
            } else {
                log.error("❌ 阅读任务学习失败: {}", job.getJobName());
                return new StudyResultDTO(StudyResult.ERROR, "阅读学习失败", job.getJobId());
            }

        } catch (Exception e) {
            log.error("学习阅读任务异常: jobId={}", job.getJobId(), e);
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
    public StudyResultDTO studyWork(JobDTO job) {
        log.info("📝 开始学习测验任务: jobId={}, jobName={}", job.getJobId(), job.getJobName());

        try {
            // 1. 检查并恢复会话状态
            ensureSessionValid();

            // 2. 提取必要参数
            String knowledgeId = job.getKnowledgeId();
            if (knowledgeId == null || knowledgeId.isEmpty()) {
                knowledgeId = extractKnowledgeIdFromOtherInfo(job.getOtherinfo());
            }

            String cpi = extractCpiFromOtherInfo(job.getOtherinfo());
            if (cpi == null || cpi.isEmpty()) {
                cpi = "0";
            }

            // 3. 获取workId（去掉work-前缀）
            String workId = job.getJobId().replace("work-", "");

            log.info("测验参数 - workId: {}, knowledgeId: {}, cpi: {}", workId, knowledgeId, cpi);

            // 4. 获取测验题目HTML
            String html = apiClient.getWorkQuestionsHtml(
                    workId,
                    job.getJobId(),
                    knowledgeId,
                    job.getKtoken() != null ? job.getKtoken() : "",
                    cpi,
                    job.getClazzId(),
                    job.getCourseId(),
                    job.getEnc()
            );

            if (html == null || html.isEmpty()) {
                log.error("❌ 获取测验题目失败");
                return new StudyResultDTO(StudyResult.ERROR, "获取测验题目失败", job.getJobId());
            }

            // 5. 解析题目
            Map<String, Object> parsedData = workAdapter.decodeQuestionsInfo(html);
            
            @SuppressWarnings("unchecked")
            List<QuestionDTO> questions = (List<QuestionDTO>) parsedData.get("questions");
            
            @SuppressWarnings("unchecked")
            Map<String, String> formData = (Map<String, String>) parsedData.get("formData");

            if (questions == null || questions.isEmpty()) {
                log.warn("⚠️  未解析到题目，可能测验已完成或无题目");
                return new StudyResultDTO(StudyResult.SUCCESS, "测验无题目或已完成", job.getJobId());
            }

            log.info("✅ 成功解析{}道题目", questions.size());

            // 6. 创建答题管理器并处理所有题目
            WorkAnswerManager answerManager = new WorkAnswerManager(tikuService, answerMatcher);
            int foundAnswers = answerManager.processAllQuestions(questions);

            // 7. 获取提交策略
            String pyFlag = answerManager.getPyFlag(foundAnswers, questions.size());

            // 8. 构建提交表单
            Map<String, String> submitData = answerManager.buildSubmitFormData(questions, formData, pyFlag);

            // 9. 提交答案
            boolean success = apiClient.submitWorkAnswers(submitData);

            if (success) {
                String action = "1".equals(pyFlag) ? "保存" : "提交";
                log.info("✅ 测验任务{}成功: {}/{}道题找到答案", action, foundAnswers, questions.size());
                return new StudyResultDTO(StudyResult.SUCCESS, 
                        String.format("测验%s成功 (%d/%d)", action, foundAnswers, questions.size()), 
                        job.getJobId());
            } else {
                log.error("❌ 测验答案提交失败");
                return new StudyResultDTO(StudyResult.ERROR, "测验答案提交失败", job.getJobId());
            }

        } catch (Exception e) {
            log.error("学习测验任务异常: jobId={}", job.getJobId(), e);
            return new StudyResultDTO(StudyResult.ERROR, "学习失败: " + e.getMessage(), job.getJobId());
        }
    }

    @Override
    public StudyResultDTO studyLive(JobDTO job) {
        log.info("📺 开始学习直播任务: jobId={}, jobName={}", job.getJobId(), job.getJobName());

        try {
            // 检查并恢复会话状态
            ensureSessionValid();

            // 调用直播服务进行学习
            return liveService.studyLive(job);

        } catch (Exception e) {
            log.error("学习直播任务异常: jobId={}", job.getJobId(), e);
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
            case READ -> studyRead(job);
            case WORK -> studyWork(job);
            case LIVE -> studyLive(job);
            case EMPTY_PAGE -> studyEmptyPage(job);
            default -> {
                log.warn("未知任务类型: {}", job.getJobType());
                yield new StudyResultDTO(StudyResult.SKIP, "未知任务类型", job.getJobId());
            }
        };
    }

    @Override
    public List<StudyResultDTO> studyChapterJobs(String courseId, String clazzId, String knowledgeId, String cpi) {
        log.info("═══════════════════════════════════════");
        log.info("📚 开始学习章节所有任务");
        log.info("课程ID: {}", courseId);
        log.info("班级ID: {}", clazzId);
        log.info("知识点ID: {}", knowledgeId);
        log.info("═══════════════════════════════════════");

        List<StudyResultDTO> results = new ArrayList<>();

        try {
            // 获取任务列表
            Map<String, Object> jobListResult = getJobList(courseId, clazzId, knowledgeId, cpi);
            
            @SuppressWarnings("unchecked")
            List<JobDTO> jobs = (List<JobDTO>) jobListResult.get("jobList");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> jobInfo = (Map<String, Object>) jobListResult.get("jobInfo");

            // 检查章节是否未开放
            if (jobInfo != null && Boolean.TRUE.equals(jobInfo.get("notOpen"))) {
                log.warn("⚠️  该章节未开放，跳过学习");
                return results;
            }

            if (jobs == null || jobs.isEmpty()) {
                log.info("✅ 该章节没有任务需要学习");
                return results;
            }

            log.info("📋 共找到{}个任务，开始逐个学习", jobs.size());
            log.info("═══════════════════════════════════════");

            int successCount = 0;
            int errorCount = 0;
            int skipCount = 0;

            // 逐个学习任务
            for (int i = 0; i < jobs.size(); i++) {
                JobDTO job = jobs.get(i);
                
                log.info("───────────────────────────────────");
                log.info("🔄 [{}/{}] 开始学习任务", i + 1, jobs.size());
                log.info("   任务ID: {}", job.getJobId());
                log.info("   任务名称: {}", job.getJobName() != null ? job.getJobName() : "未知");
                log.info("   任务类型: {}", job.getJobType());
                log.info("───────────────────────────────────");

                StudyResultDTO result = null;
                
                // 任务级别重试（最多3次）
                int maxRetries = 3;
                for (int retry = 0; retry < maxRetries; retry++) {
                    try {
                        result = studyJob(job);
                        
                        // 如果成功或跳过，跳出重试循环
                        if (result.getResult() == StudyResult.SUCCESS || 
                            result.getResult() == StudyResult.SKIP) {
                            break;
                        }
                        
                        // 如果是错误且还有重试次数，等待后重试
                        if (retry < maxRetries - 1) {
                            log.warn("⚠️  任务学习失败，第{}次重试...", retry + 1);
                            rateLimiter.limitRateWithRandom(2.0, 5.0); // 重试前等待更长时间
                        }
                        
                    } catch (Exception e) {
                        log.error("❌ 任务学习异常 (尝试 {}/{}): {}", retry + 1, maxRetries, e.getMessage());
                        if (retry == maxRetries - 1) {
                            result = new StudyResultDTO(StudyResult.ERROR, 
                                    "学习异常: " + e.getMessage(), job.getJobId());
                        }
                    }
                }
                
                if (result == null) {
                    result = new StudyResultDTO(StudyResult.ERROR, "未知错误", job.getJobId());
                }
                
                results.add(result);

                // 统计结果
                switch (result.getResult()) {
                    case SUCCESS -> {
                        successCount++;
                        log.info("✅ [{}/{}] 任务学习成功: {}", i + 1, jobs.size(), result.getMessage());
                    }
                    case ERROR -> {
                        errorCount++;
                        log.error("❌ [{}/{}] 任务学习失败: {}", i + 1, jobs.size(), result.getMessage());
                    }
                    case SKIP -> {
                        skipCount++;
                        log.info("⏭️  [{}/{}] 任务已跳过: {}", i + 1, jobs.size(), result.getMessage());
                    }
                }

                // 速率限制，避免请求过快（最后一个任务不需要等待）
                if (i < jobs.size() - 1) {
                    rateLimiter.limitRateWithRandom(1.0, 3.0);
                }
            }

            // 输出章节学习总结
            log.info("═══════════════════════════════════════");
            log.info("📊 章节任务学习完成");
            log.info("总任务数: {}", jobs.size());
            log.info("✅ 成功: {}", successCount);
            log.info("❌ 失败: {}", errorCount);
            log.info("⏭️  跳过: {}", skipCount);
            log.info("成功率: {:.1f}%", (double) successCount / jobs.size() * 100);
            log.info("═══════════════════════════════════════");

        } catch (Exception e) {
            log.error("❌ 学习章节任务异常", e);
        }

        return results;
    }

    /**
     * 确保会话有效，如果fid缺失则尝试从数据库恢复
     */
    private void ensureSessionValid() {
        String fid = sessionManager.getFid();
        
        if (fid != null && !fid.isEmpty()) {
            log.debug("会话状态正常 - fid: {}", fid);
            return;
        }
        
        log.warn("检测到FID缺失，尝试从数据库恢复会话");
        
        try {
            // 1. 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.error("用户未登录，无法恢复会话");
                throw new RuntimeException("用户未登录");
            }
            
            String username = authentication.getName();
            log.debug("当前登录用户: {}", username);
            
            // 2. 从数据库查询用户
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            
            // 3. 检查是否绑定了超星账号
            if (user.getChaoxingCookie() == null || user.getChaoxingCookie().isEmpty()) {
                log.error("用户未绑定超星账号: {}", username);
                throw new RuntimeException("请先绑定超星账号");
            }
            
            // 4. 使用Cookie重新登录以恢复fid/uid
            log.info("使用保存的Cookie重新登录以恢复会话: userId={}", user.getId());
            LoginService.LoginResult loginResult = loginService.loginWithCookie(user.getChaoxingCookie());
            
            if (!loginResult.isSuccess()) {
                log.error("Cookie登录失败: {}", loginResult.getMessage());
                throw new RuntimeException("Cookie已失效，请重新绑定超星账号");
            }
            
            // 5. 验证fid/uid是否成功设置
            String newFid = sessionManager.getFid();
            String newUid = sessionManager.getUid();
            
            if (newFid == null || newFid.isEmpty()) {
                log.error("会话恢复后FID仍为空");
                throw new RuntimeException("会话恢复失败");
            }
            
            log.info("✅ 会话恢复成功 - fid: {}, uid: {}", newFid, newUid);
            
        } catch (RuntimeException e) {
            log.error("会话恢复失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("会话恢复异常", e);
            throw new RuntimeException("会话恢复异常: " + e.getMessage());
        }
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
