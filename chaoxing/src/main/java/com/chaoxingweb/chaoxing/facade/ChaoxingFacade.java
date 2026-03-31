package com.chaoxingweb.chaoxing.facade;

import com.chaoxingweb.chaoxing.dto.ChaoxingLoginDTO;
import com.chaoxingweb.chaoxing.vo.ChaoxingLoginResult;
import com.chaoxingweb.chaoxing.vo.CourseVO;

import java.util.List;

/**
 * 超星模块 Facade
 *
 * 职责：
 * - 对外提供统一接口
 * - 协调各个模块
 * - 不包含具体业务逻辑
 */
public interface ChaoxingFacade {

    /**
     * 超星登录
     *
     * @param dto 登录信息（DTO - 内部数据传输）
     * @return 登录结果（VO - 外部视图展示）
     */
    ChaoxingLoginResult login(ChaoxingLoginDTO dto);

    /**
     * 获取课程列表
     *
     * @return 课程列表
     */
    List<CourseVO> getCourseList();

    /**
     * 获取课程详情
     *
     * @param courseId 课程 ID
     * @return 课程详情
     */
    CourseVO getCourseDetail(String courseId);
}
