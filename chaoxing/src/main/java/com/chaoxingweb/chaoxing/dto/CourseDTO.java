package com.chaoxingweb.chaoxing.dto;

/**
 * 课程 DTO
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
public class CourseDTO {

    private String courseId;
    private String clazzId;
    private String cpi;
    private String courseName;
    private String teacherName;
    private String schoolName;
    private String courseStatus;
    private String description;
    private String coverUrl;
    private String status;

    public CourseDTO() {
    }

    public CourseDTO(String courseId, String clazzId, String cpi, String courseName) {
        this.courseId = courseId;
        this.clazzId = clazzId;
        this.cpi = cpi;
        this.courseName = courseName;
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

    public String getCpi() {
        return cpi;
    }

    public void setCpi(String cpi) {
        this.cpi = cpi;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getCourseStatus() {
        return courseStatus;
    }

    public void setCourseStatus(String courseStatus) {
        this.courseStatus = courseStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
