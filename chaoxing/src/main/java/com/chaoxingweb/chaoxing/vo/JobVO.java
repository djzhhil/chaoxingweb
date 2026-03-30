package com.chaoxingweb.chaoxing.vo;

import com.chaoxingweb.chaoxing.enums.JobType;

/**
 * 任务 VO
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
public class JobVO {

    private String jobId;
    private String jobName;
    private JobType jobType;
    private String status;
    private int progress;
    private boolean completed;

    public JobVO() {
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

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
