package com.chaoxingweb.chaoxing.vo;

/**
 * 课程 VO
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
public class CourseVO {

    private String courseId;
    private String courseName;
    private String teacherName;
    private String schoolName;
    private String courseStatus;
    private int progress;
    private int totalJobs;
    private int completedJobs;

    public CourseVO() {
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(int totalJobs) {
        this.totalJobs = totalJobs;
    }

    public int getCompletedJobs() {
        return completedJobs;
    }

    public void setCompletedJobs(int completedJobs) {
        this.completedJobs = completedJobs;
    }
}
