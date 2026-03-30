package com.chaoxingweb.chaoxing.dto;

import com.chaoxingweb.chaoxing.enums.JobType;

/**
 * 任务 DTO
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
public class JobDTO {

    private String jobId;
    private String jobName;
    private String courseId;
    private String clazzId;
    private String chapterId;
    private String knowledgeId;
    private JobType jobType;
    private String jtoken;
    private String status;
    private int progress;

    public JobDTO() {
    }

    public JobDTO(String jobId, String jobName, JobType jobType) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.jobType = jobType;
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

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getClazzId() {
        return clazzId;
    }

    public void setClazzId(String clazzId) {
        this.clazzId = clazzId;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getKnowledgeId() {
        return knowledgeId;
    }

    public void setKnowledgeId(String knowledgeId) {
        this.knowledgeId = knowledgeId;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public String getJtoken() {
        return jtoken;
    }

    public void setJtoken(String jtoken) {
        this.jtoken = jtoken;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
