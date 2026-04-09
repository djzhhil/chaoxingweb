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
    
    // 视频学习相关字段
    private String objectId;           // 对象ID，用于视频进度上报
    private String mid;                // 媒体ID（视频/直播）
    private String otherinfo;          // 其他信息字符串，包含rt等参数
    private String dtoken;             // 文档/视频token
    private double duration;           // 视频总时长（秒）
    private double playingTime;        // 当前播放时间（秒）
    private String rt;                 // 完成率系数（0.9或1）
    private String videoFaceCaptureEnc; // 视频人脸捕获加密串
    private String attDuration;        // 附加时长
    private String attDurationEnc;     // 附加时长加密串
    
    // 任务卡片元数据
    private String type;               // 任务类型标识（"video", "work", "read"等）
    private boolean isPassed;          // 是否已通过
    private boolean jobNeedFace;       // 是否需要人脸识别
    private boolean jobNeedVideo;      // 是否需要视频验证
    
    // 测验相关字段
    private String ktoken;             // 知识点token（用于测验）
    private String enc;                // 加密参数（用于测验）
    
    // 直播相关字段
    private String liveId;             // 直播ID
    private String streamName;         // 流名称
    private String vdoid;              // 视频对象ID
    
    // 音频相关字段
    private String audioObjectId;      // 音频对象ID

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

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getOtherinfo() {
        return otherinfo;
    }

    public void setOtherinfo(String otherinfo) {
        this.otherinfo = otherinfo;
    }

    public String getDtoken() {
        return dtoken;
    }

    public void setDtoken(String dtoken) {
        this.dtoken = dtoken;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getPlayingTime() {
        return playingTime;
    }

    public void setPlayingTime(double playingTime) {
        this.playingTime = playingTime;
    }

    public String getRt() {
        return rt;
    }

    public void setRt(String rt) {
        this.rt = rt;
    }

    public String getVideoFaceCaptureEnc() {
        return videoFaceCaptureEnc;
    }

    public void setVideoFaceCaptureEnc(String videoFaceCaptureEnc) {
        this.videoFaceCaptureEnc = videoFaceCaptureEnc;
    }

    public String getAttDuration() {
        return attDuration;
    }

    public void setAttDuration(String attDuration) {
        this.attDuration = attDuration;
    }

    public String getAttDurationEnc() {
        return attDurationEnc;
    }

    public void setAttDurationEnc(String attDurationEnc) {
        this.attDurationEnc = attDurationEnc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isPassed() {
        return isPassed;
    }

    public void setPassed(boolean passed) {
        isPassed = passed;
    }

    public boolean isJobNeedFace() {
        return jobNeedFace;
    }

    public void setJobNeedFace(boolean jobNeedFace) {
        this.jobNeedFace = jobNeedFace;
    }

    public boolean isJobNeedVideo() {
        return jobNeedVideo;
    }

    public void setJobNeedVideo(boolean jobNeedVideo) {
        this.jobNeedVideo = jobNeedVideo;
    }

    public String getKtoken() {
        return ktoken;
    }

    public void setKtoken(String ktoken) {
        this.ktoken = ktoken;
    }

    public String getEnc() {
        return enc;
    }

    public void setEnc(String enc) {
        this.enc = enc;
    }

    public String getLiveId() {
        return liveId;
    }

    public void setLiveId(String liveId) {
        this.liveId = liveId;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getVdoid() {
        return vdoid;
    }

    public void setVdoid(String vdoid) {
        this.vdoid = vdoid;
    }

    public String getAudioObjectId() {
        return audioObjectId;
    }

    public void setAudioObjectId(String audioObjectId) {
        this.audioObjectId = audioObjectId;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }
}
