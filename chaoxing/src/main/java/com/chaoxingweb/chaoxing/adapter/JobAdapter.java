package com.chaoxingweb.chaoxing.adapter;

import com.chaoxingweb.chaoxing.dto.JobDTO;
import com.chaoxingweb.chaoxing.enums.JobType;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 任务适配器
 *
 * 职责：
 * - 将超星 API 响应转换为内部模型
 * - 解析 HTML 响应中的任务卡片信息
 * - 不包含业务逻辑
 *
 * @author 小克 🐕💎
 * @since 2026-04-08
 */
@Slf4j
@Component
public class JobAdapter {

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
            Document doc = Jsoup.parse(htmlText);
            
            // 检查是否未开放
            Element notOpenElement = doc.selectFirst("input[id^='ans-job']");
            if (notOpenElement != null && notOpenElement.attr("value").contains("notOpen")) {
                jobInfo.put("notOpen", true);
                result.put("jobList", jobList);
                result.put("jobInfo", jobInfo);
                return result;
            }
            
            jobInfo.put("notOpen", false);
            
            // 查找所有任务卡片
            Elements jobElements = doc.select("div.ans-job-icon");
            
            for (Element jobElement : jobElements) {
                JobDTO job = parseJobElement(jobElement);
                if (job != null) {
                    jobList.add(job);
                }
            }
            
            // 如果没有找到任务，尝试另一种选择器
            if (jobList.isEmpty()) {
                Elements iframeElements = doc.select("iframe");
                for (Element iframe : iframeElements) {
                    JobDTO job = parseIframeJob(iframe);
                    if (job != null) {
                        jobList.add(job);
                    }
                }
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
     * 解析单个任务元素
     *
     * @param jobElement 任务元素
     * @return JobDTO
     */
    private JobDTO parseJobElement(Element jobElement) {
        try {
            Element parent = jobElement.parent();
            if (parent == null) {
                return null;
            }
            
            // 提取任务ID
            String jobId = parent.attr("data");
            if (jobId == null || jobId.isEmpty()) {
                jobId = parent.attr("id");
            }
            
            // 提取任务类型
            JobType jobType = determineJobType(parent);
            
            // 提取其他信息
            Element attachmentElement = parent.selectFirst("input[id^='ans-job-attachment']");
            String attachment = attachmentElement != null ? attachmentElement.val() : "";
            
            // 创建JobDTO
            JobDTO job = new JobDTO();
            job.setJobId(jobId);
            job.setJobType(jobType);
            job.setStatus("pending");
            job.setProgress(0);
            
            // 解析附件信息
            if (!attachment.isEmpty()) {
                parseAttachmentInfo(job, attachment);
            }
            
            return job;
            
        } catch (Exception e) {
            log.warn("解析任务元素失败", e);
            return null;
        }
    }
    
    /**
     * 从iframe解析任务
     *
     * @param iframe iframe元素
     * @return JobDTO
     */
    private JobDTO parseIframeJob(Element iframe) {
        try {
            String src = iframe.attr("src");
            if (src == null || src.isEmpty()) {
                return null;
            }
            
            // 从URL中提取参数
            Map<String, String> params = parseUrlParams(src);
            
            String jobId = params.getOrDefault("jobid", "");
            String objectId = params.getOrDefault("objectid", "");
            String otherInfo = params.getOrDefault("otherinfo", "");
            
            if (jobId.isEmpty()) {
                return null;
            }
            
            // 判断任务类型
            JobType jobType = determineJobTypeFromUrl(src);
            
            // 创建JobDTO
            JobDTO job = new JobDTO();
            job.setJobId(jobId);
            job.setJobType(jobType);
            job.setStatus("pending");
            job.setProgress(0);
            
            // 设置额外信息
            if (!objectId.isEmpty()) {
                // 可以将objectId存储在其他字段中
            }
            if (!otherInfo.isEmpty()) {
                // 可以解析otherInfo获取更多信息
            }
            
            return job;
            
        } catch (Exception e) {
            log.warn("解析iframe任务失败", e);
            return null;
        }
    }
    
    /**
     * 确定任务类型
     *
     * @param element 元素
     * @return JobType
     */
    private JobType determineJobType(Element element) {
        // 根据class或属性判断任务类型
        String className = element.className();
        String id = element.id();
        
        if (className.contains("video") || id.contains("video")) {
            return JobType.VIDEO;
        } else if (className.contains("work") || id.contains("work")) {
            return JobType.WORK;
        } else if (className.contains("read") || id.contains("read")) {
            return JobType.READ;
        } else if (className.contains("doc") || id.contains("doc")) {
            return JobType.DOCUMENT;
        }
        
        return JobType.UNKNOWN;
    }
    
    /**
     * 从URL判断任务类型
     *
     * @param url URL
     * @return JobType
     */
    private JobType determineJobTypeFromUrl(String url) {
        if (url.contains("video") || url.contains("multimedia")) {
            return JobType.VIDEO;
        } else if (url.contains("work") || url.contains("homework")) {
            return JobType.WORK;
        } else if (url.contains("read")) {
            return JobType.READ;
        } else if (url.contains("doc") || url.contains("document")) {
            return JobType.DOCUMENT;
        }
        
        return JobType.UNKNOWN;
    }
    
    /**
     * 解析附件信息
     *
     * @param job JobDTO
     * @param attachment 附件JSON字符串
     */
    private void parseAttachmentInfo(JobDTO job, String attachment) {
        try {
            // 这里需要解析JSON格式的附件信息
            // 简化处理，实际项目中可能需要使用JSON库
            log.trace("解析附件信息: {}", attachment);
            
            // 提取jtoken等信息
            Pattern pattern = Pattern.compile("\"jtoken\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(attachment);
            if (matcher.find()) {
                job.setJtoken(matcher.group(1));
            }
            
        } catch (Exception e) {
            log.warn("解析附件信息失败", e);
        }
    }
    
    /**
     * 解析URL参数
     *
     * @param url URL
     * @return 参数Map
     */
    private Map<String, String> parseUrlParams(String url) {
        Map<String, String> params = new HashMap<>();
        
        try {
            int queryStart = url.indexOf('?');
            if (queryStart == -1) {
                return params;
            }
            
            String query = url.substring(queryStart + 1);
            String[] pairs = query.split("&");
            
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
            
        } catch (Exception e) {
            log.warn("解析URL参数失败", e);
        }
        
        return params;
    }
}
