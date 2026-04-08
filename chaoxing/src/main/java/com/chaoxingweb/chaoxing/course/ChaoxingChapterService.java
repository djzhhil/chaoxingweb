package com.chaoxingweb.chaoxing.course;

import com.chaoxingweb.chaoxing.dto.ChapterDTO;

import java.util.List;
import java.util.Map;

/**
 * 超星章节服务接口
 *
 * @author 小克 🐕💎
 * @since 2026-04-08
 */
public interface ChaoxingChapterService {

    /**
     * 获取课程章节列表
     *
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param cpi CPI
     * @return 章节列表
     */
    List<ChapterDTO> getChapterList(String courseId, String clazzId, String cpi);

    /**
     * 获取课程章节详情（包含锁定状态等信息）
     *
     * @param courseId 课程ID
     * @param clazzId 班级ID
     * @param cpi CPI
     * @return 包含 hasLocked 和 points 的 Map
     */
    Map<String, Object> getChapterDetail(String courseId, String clazzId, String cpi);
}
