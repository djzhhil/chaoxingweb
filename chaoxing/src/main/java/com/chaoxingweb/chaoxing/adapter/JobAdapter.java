package com.chaoxingweb.chaoxing.adapter;

import com.chaoxingweb.chaoxing.dto.JobDTO;
import com.chaoxingweb.chaoxing.enums.JobType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 任务适配器
 *
 * 职责：
 * - 解析超星学习通任务卡片页面
 * - 从 JavaScript mArg 变量中提取 JSON 数据
 * - 处理多种任务类型：直播、视频、文档、作业、阅读
 * - 将原始数据转换为 JobDTO 对象
 *
 * @author 小克 🐕💎
 * @since 2026-04-08
 */
@Slf4j
@Component
public class JobAdapter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析课程任务卡片列表
     *
     * @param htmlText 任务卡片页面的HTML内容
     * @return 包含任务列表和任务信息的Map
     */
    public Map<String, Object> decodeCourseCard(String htmlText) {
        log.trace("开始解码任务卡片...");

        Map<String, Object> result = new HashMap<>();
        List<JobDTO> jobList = new ArrayList<>();
        Map<String, Object> jobInfo = new HashMap<>();

        try {
            // 检查章节是否未开放
            if (htmlText.contains("章节未开放")) {
                jobInfo.put("notOpen", true);
                result.put("jobList", jobList);
                result.put("jobInfo", jobInfo);
                return result;
            }

            // 提取 mArg 参数
            Pattern pattern = Pattern.compile("mArg=\\{(.*?)\\};");
            Matcher matcher = pattern.matcher(htmlText.replace(" ", ""));
            
            if (!matcher.find()) {
                log.warn("未找到 mArg 数据");
                result.put("jobList", jobList);
                result.put("jobInfo", jobInfo);
                return result;
            }

            // 解析 JSON 数据
            String jsonStr = "{" + matcher.group(1) + "}";
            JsonNode rootNode = objectMapper.readTree(jsonStr);

            if (rootNode == null || rootNode.isEmpty()) {
                log.warn("mArg 数据为空");
                result.put("jobList", jobList);
                result.put("jobInfo", jobInfo);
                return result;
            }

            // 提取任务信息
            jobInfo = extractJobInfo(rootNode);

            // 处理所有附件任务
            JsonNode attachments = rootNode.get("attachments");
            if (attachments != null && attachments.isArray()) {
                jobList = processAttachmentCards(attachments);
            }

            result.put("jobList", jobList);
            result.put("jobInfo", jobInfo);

            log.info("解析到 {} 个任务点", jobList.size());

        } catch (Exception e) {
            log.error("解析任务卡片异常", e);
        }

        return result;
    }

    /**
     * 从卡片数据中提取任务基本信息
     *
     * @param rootNode 根节点
     * @return 任务基本信息字典
     */
    private Map<String, Object> extractJobInfo(JsonNode rootNode) {
        Map<String, Object> jobInfo = new HashMap<>();
        
        JsonNode defaults = rootNode.get("defaults");
        if (defaults == null || defaults.isEmpty()) {
            return jobInfo;
        }

        jobInfo.put("ktoken", getStringValue(defaults, "ktoken", ""));
        jobInfo.put("mtEnc", getStringValue(defaults, "mtEnc", ""));
        jobInfo.put("reportTimeInterval", getIntValue(defaults, "reportTimeInterval", 60));
        jobInfo.put("defenc", getStringValue(defaults, "defenc", ""));
        jobInfo.put("cardid", getStringValue(defaults, "cardid", ""));
        jobInfo.put("cpi", getStringValue(defaults, "cpi", ""));
        jobInfo.put("qnenc", getStringValue(defaults, "qnenc", ""));
        jobInfo.put("knowledgeid", getStringValue(defaults, "knowledgeid", ""));

        return jobInfo;
    }

    /**
     * 处理所有附件任务卡片
     *
     * @param attachments 附件任务卡片数组
     * @return 处理后的任务列表
     */
    private List<JobDTO> processAttachmentCards(JsonNode attachments) {
        List<JobDTO> jobList = new ArrayList<>();

        for (JsonNode card : attachments) {
            // 跳过已通过的任务
            if (card.has("isPassed") && card.get("isPassed").asBoolean()) {
                continue;
            }

            // 处理无 job 字段的特殊任务（如阅读任务）
            if (!card.has("job") || card.get("job").isNull()) {
                JobDTO readJob = processReadTask(card);
                if (readJob != null) {
                    jobList.add(readJob);
                }
                continue;
            }

            // 清理 otherInfo 字段中的无效参数
            if (card.has("otherInfo")) {
                String otherInfo = card.get("otherInfo").asText();
                if (otherInfo != null && !otherInfo.isEmpty()) {
                    // 只保留第一个参数
                    String cleanedInfo = otherInfo.split("&")[0];
                    log.trace("清理 otherInfo: {} -> {}", otherInfo, cleanedInfo);
                }
            }

            // 多维度判断是否为直播任务
            String cardType = getStringValue(card, "type", "").toLowerCase();
            JsonNode property = card.get("property");
            String propType = property != null ? getStringValue(property, "type", "").toLowerCase() : "";
            String resourceType = property != null ? getStringValue(property, "resourceType", "").toLowerCase() : "";

            boolean isLive = cardType.contains("live")
                    || propType.contains("live")
                    || resourceType.contains("live")
                    || cardType.contains("livestream")
                    || (property != null && property.has("liveId"))
                    || (property != null && property.has("streamName"))
                    || (property != null && property.has("vdoid"));

            // 根据任务类型处理
            JobDTO job = null;
            if (isLive) {
                job = processLiveTask(card);
            } else if ("video".equals(cardType)) {
                job = processVideoTask(card);
            } else if ("document".equals(cardType)) {
                job = processDocumentTask(card);
            } else if ("workid".equals(cardType)) {
                job = processWorkTask(card);
            } else {
                log.warn("未知的任务类型: {}", cardType);
                log.debug("任务卡片数据: {}", card.toString());
            }

            if (job != null) {
                jobList.add(job);
            }
        }

        return jobList;
    }

    /**
     * 处理直播类型任务
     *
     * @param card 任务卡片数据
     * @return JobDTO
     */
    private JobDTO processLiveTask(JsonNode card) {
        try {
            JsonNode property = card.get("property");
            if (property == null) {
                log.warn("直播任务缺少 property 字段");
                return null;
            }

            JobDTO job = new JobDTO();
            job.setJobId(getStringValue(card, "jobid", String.valueOf(card.has("id") ? card.get("id").asLong() : "")));
            job.setJobName(getStringValue(property, "title", getStringValue(property, "name", "未知直播")));
            job.setJobType(JobType.LIVE); // 修正：使用LIVE类型
            job.setType("live");
            
            // 设置额外参数
            job.setJtoken(getStringValue(card, "jtoken", ""));
            job.setOtherinfo(getStringValue(card, "otherInfo", ""));
            
            // 设置直播特有字段
            job.setLiveId(getStringValue(property, "liveId", ""));
            job.setStreamName(getStringValue(property, "streamName", ""));
            job.setVdoid(getStringValue(property, "vdoid", ""));
            job.setObjectId(getStringValue(card, "objectId", ""));
            job.setMid(getStringValue(card, "mid", ""));
            
            log.debug("解析直播任务: {}, liveId={}, streamName={}", 
                    job.getJobName(), job.getLiveId(), job.getStreamName());
            
            return job;

        } catch (Exception e) {
            log.error("解析直播任务失败: {}, 任务数据: {}", e.getMessage(), 
                    card.toString().substring(0, Math.min(200, card.toString().length())));
            return null;
        }
    }

    /**
     * 处理阅读类型任务
     *
     * @param card 任务卡片数据
     * @return JobDTO
     */
    private JobDTO processReadTask(JsonNode card) {
        // 判断是否为阅读任务
        String type = getStringValue(card, "type", "");
        JsonNode property = card.get("property");
        boolean isRead = property != null && !getBooleanValue(property, "read", false);

        if (!"read".equals(type) || !isRead) {
            return null;
        }

        try {
            JobDTO job = new JobDTO();
            job.setJobId(getStringValue(card, "jobid", ""));
            job.setJobName(getStringValue(property, "title", ""));
            job.setJobType(JobType.READ);
            job.setType("read");
            job.setJtoken(getStringValue(card, "jtoken", ""));
            
            log.debug("解析阅读任务: {}", job.getJobName());
            
            return job;

        } catch (Exception e) {
            log.error("解析阅读任务失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 处理视频类型任务
     *
     * @param card 任务卡片数据
     * @return JobDTO
     */
    private JobDTO processVideoTask(JsonNode card) {
        try {
            // 添加原始JSON日志，用于诊断objectId字段问题
            log.info("视频任务卡片完整JSON: {}", card.toPrettyString());
            
            JsonNode property = card.get("property");
            if (property == null) {
                log.warn("视频任务缺少 property 字段");
                return null;
            }

            // mid 是必须字段
            if (!card.has("mid") || card.get("mid").isNull()) {
                log.warn("视频任务缺少 mid 字段，跳过");
                return null;
            }

            JobDTO job = new JobDTO();
            job.setJobId(getStringValue(card, "jobid", ""));
            job.setJobName(getStringValue(property, "name", ""));
            job.setJobType(JobType.VIDEO);
            job.setType("video");
            job.setJtoken(getStringValue(card, "jtoken", ""));
            
            // 视频特有参数
            // 对照Python实现和实际JSON结构，应该使用 objectId 字段（驼峰命名）
            String objectId = getStringValue(card, "objectId", "");  // 驼峰命名（顶层）
            if (objectId.isEmpty()) {
                objectId = getStringValue(card, "objectid", "");  // 全小写（兼容）
            }
            String mid = getStringValue(card, "mid", "");
            
            log.info("字段对比 - objectId(驼峰): {}, objectid(小写): {}, mid: {}", 
                    getStringValue(card, "objectId", ""), 
                    getStringValue(card, "objectid", ""), 
                    mid);
            
            // 如果都没有，才使用 mid
            if (objectId.isEmpty()) {
                objectId = mid;
                log.warn("objectId 和 objectid 都为空，使用 mid 作为 objectId");
            } else {
                log.info("使用 objectId: {}", objectId);
            }
            job.setObjectId(objectId);
            
            job.setOtherinfo(getStringValue(card, "otherInfo", ""));
            job.setDtoken(getStringValue(card, "dtoken", ""));
            
            int playTime = getIntValue(card, "playTime", 0);
            job.setPlayingTime(playTime);
            
            String rt = getStringValue(property, "rt", "");
            job.setRt(rt);
            
            String attDuration = getStringValue(card, "attDuration", "");
            job.setAttDuration(attDuration);
            
            String videoFaceCaptureEnc = getStringValue(card, "videoFaceCaptureEnc", "");
            job.setVideoFaceCaptureEnc(videoFaceCaptureEnc);
            
            // 从 otherInfo 中提取 duration
            String otherInfo = getStringValue(card, "otherInfo", "");
            if (otherInfo != null && !otherInfo.isEmpty()) {
                // 尝试解析 duration 参数
                String[] params = otherInfo.split("&");
                for (String param : params) {
                    if (param.startsWith("duration=")) {
                        try {
                            job.setDuration(Double.parseDouble(param.substring(9)));
                        } catch (NumberFormatException e) {
                            log.debug("解析 duration 失败: {}", param);
                        }
                        break;
                    }
                }
            }
            
            log.debug("解析视频任务: {}, 播放时长: {}", job.getJobName(), playTime);
            
            return job;

        } catch (Exception e) {
            log.error("解析视频任务失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 处理文档类型任务
     *
     * @param card 任务卡片数据
     * @return JobDTO
     */
    private JobDTO processDocumentTask(JsonNode card) {
        try {
            JsonNode property = card.get("property");
            
            JobDTO job = new JobDTO();
            job.setJobId(getStringValue(card, "jobid", ""));
            job.setJobName(getStringValue(property, "title", "未知文档"));
            job.setJobType(JobType.DOCUMENT);
            job.setType("document");
            job.setJtoken(getStringValue(card, "jtoken", ""));
            
            // 文档特有参数
            job.setObjectId(getStringValue(card, "objectid", ""));
            job.setDtoken(getStringValue(card, "dtoken", ""));
            
            log.debug("解析文档任务: {}", job.getJobName());
            
            return job;

        } catch (Exception e) {
            log.error("解析文档任务失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 处理作业类型任务
     *
     * @param card 任务卡片数据
     * @return JobDTO
     */
    private JobDTO processWorkTask(JsonNode card) {
        try {
            JobDTO job = new JobDTO();
            job.setJobId(getStringValue(card, "jobid", ""));
            job.setJobName("作业任务");
            job.setJobType(JobType.WORK);
            job.setType("workid");
            job.setJtoken(getStringValue(card, "jtoken", ""));
            
            log.debug("解析作业任务: jobId={}", job.getJobId());
            
            return job;

        } catch (Exception e) {
            log.error("解析作业任务失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取字符串值，提供默认值
     *
     * @param node JSON 节点
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @return 字符串值
     */
    private String getStringValue(JsonNode node, String fieldName, String defaultValue) {
        if (node == null || !node.has(fieldName)) {
            return defaultValue;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isNull() ? defaultValue : fieldNode.asText(defaultValue);
    }

    /**
     * 获取整数值，提供默认值
     *
     * @param node JSON 节点
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @return 整数值
     */
    private int getIntValue(JsonNode node, String fieldName, int defaultValue) {
        if (node == null || !node.has(fieldName)) {
            return defaultValue;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isNull() ? defaultValue : fieldNode.asInt(defaultValue);
    }

    /**
     * 获取布尔值，提供默认值
     *
     * @param node JSON 节点
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @return 布尔值
     */
    private boolean getBooleanValue(JsonNode node, String fieldName, boolean defaultValue) {
        if (node == null || !node.has(fieldName)) {
            return defaultValue;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isNull() ? defaultValue : fieldNode.asBoolean(defaultValue);
    }
}
