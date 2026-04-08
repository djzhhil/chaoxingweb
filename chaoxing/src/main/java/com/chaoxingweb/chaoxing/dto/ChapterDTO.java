package com.chaoxingweb.chaoxing.dto;

/**
 * 章节 DTO
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
public class ChapterDTO {

    private String id;
    private String title;
    private String courseId;
    private String clazzId;
    private String cpi;
    private String parentId;
    private int level;
    private String status;
    private int jobCount;
    private boolean hasFinished;
    private boolean needUnlock;

    public ChapterDTO() {
    }

    public ChapterDTO(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCpi() {
        return cpi;
    }

    public void setCpi(String cpi) {
        this.cpi = cpi;
    }

    public int getJobCount() {
        return jobCount;
    }

    public void setJobCount(int jobCount) {
        this.jobCount = jobCount;
    }

    public boolean isHasFinished() {
        return hasFinished;
    }

    public void setHasFinished(boolean hasFinished) {
        this.hasFinished = hasFinished;
    }

    public boolean isNeedUnlock() {
        return needUnlock;
    }

    public void setNeedUnlock(boolean needUnlock) {
        this.needUnlock = needUnlock;
    }
}
