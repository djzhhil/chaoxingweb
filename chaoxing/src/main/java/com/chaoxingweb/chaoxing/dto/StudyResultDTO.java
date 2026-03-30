package com.chaoxingweb.chaoxing.dto;

import com.chaoxingweb.chaoxing.enums.StudyResult;

/**
 * 学习结果 DTO
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
public class StudyResultDTO {

    private StudyResult result;
    private String message;
    private String jobId;
    private long timestamp;

    public StudyResultDTO() {
    }

    public StudyResultDTO(StudyResult result, String message) {
        this.result = result;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public StudyResultDTO(StudyResult result, String message, String jobId) {
        this.result = result;
        this.message = message;
        this.jobId = jobId;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
