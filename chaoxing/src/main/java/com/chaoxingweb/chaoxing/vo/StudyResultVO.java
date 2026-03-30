package com.chaoxingweb.chaoxing.vo;

import com.chaoxingweb.chaoxing.enums.StudyResult;

/**
 * 学习结果 VO
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
public class StudyResultVO {

    private StudyResult result;
    private String message;
    private String jobId;
    private String jobName;
    private long timestamp;

    public StudyResultVO() {
    }

    public StudyResultVO(StudyResult result, String message) {
        this.result = result;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public StudyResultVO(StudyResult result, String message, String jobId, String jobName) {
        this.result = result;
        this.message = message;
        this.jobId = jobId;
        this.jobName = jobName;
        this.timestamp = System.currentTimeMillis();
    }

    public StudyResult getResult() {
        return result;
    }

    public void setResult(StudyResult result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
