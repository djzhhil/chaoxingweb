package com.chaoxingweb.chaoxing.converter;

import com.chaoxingweb.chaoxing.dto.CourseDTO;
import com.chaoxingweb.chaoxing.vo.CourseVO;
import org.springframework.stereotype.Component;

/**
 * 课程转换器
 *
 * 职责：
 * - DTO 和 VO 之间的转换
 * - 不包含业务逻辑
 *
 * @author 小克 🐕💎
 * @since 2026-03-31
 */
@Component
public class CourseConverter {

    /**
     * DTO 转 VO
     *
     * @param dto DTO
     * @return VO
     */
    public CourseVO toVO(CourseDTO dto) {
        if (dto == null) {
            return null;
        }

        CourseVO vo = new CourseVO();
        vo.setCourseId(dto.getCourseId());
        vo.setClazzId(dto.getClazzId());
        vo.setCpi(dto.getCpi());
        vo.setCourseName(dto.getCourseName());
        vo.setTeacherName(dto.getTeacherName());
        vo.setSchoolName(dto.getSchoolName());
        vo.setStatus(dto.getCourseStatus());
        return vo;
    }

    /**
     * VO 转 DTO
     *
     * @param vo VO
     * @return DTO
     */
    public CourseDTO toDTO(CourseVO vo) {
        if (vo == null) {
            return null;
        }

        CourseDTO dto = new CourseDTO();
        dto.setCourseId(vo.getCourseId());
        dto.setClazzId(vo.getClazzId());
        dto.setCpi(vo.getCpi());
        dto.setCourseName(vo.getCourseName());
        dto.setTeacherName(vo.getTeacherName());
        dto.setSchoolName(vo.getSchoolName());
        dto.setCourseStatus(vo.getStatus());
        return dto;
    }
}
