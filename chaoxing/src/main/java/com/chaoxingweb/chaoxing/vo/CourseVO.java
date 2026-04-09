package com.chaoxingweb.chaoxing.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 课程 VO
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseVO {

    private String courseId;
    private String clazzId;
    private String cpi;
    private String courseName;
    private String teacherName;
    private String schoolName;
    private String description;
    private String coverUrl;
    private String status;
    private int progress;
    private int totalJobs;
    private int completedJobs;
    
    /**
     * 是否已同步
     */
    private Boolean synced;
    
    /**
     * 同步时间
     */
    private Long syncTime;
}
