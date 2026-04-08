package com.chaoxingweb.chaoxing.converter;

import com.chaoxingweb.chaoxing.dto.ChapterDTO;
import com.chaoxingweb.chaoxing.vo.ChapterVO;
import org.springframework.stereotype.Component;

/**
 * 章节转换器
 *
 * 职责：
 * - DTO 和 VO 之间的转换
 * - 不包含业务逻辑
 *
 * @author 小克 🐕💎
 * @since 2026-04-08
 */
@Component
public class ChapterConverter {

    /**
     * DTO 转 VO
     *
     * @param dto DTO
     * @return VO
     */
    public ChapterVO toVO(ChapterDTO dto) {
        if (dto == null) {
            return null;
        }

        ChapterVO vo = new ChapterVO();
        vo.setId(dto.getId());
        vo.setTitle(dto.getTitle());
        vo.setStatus(dto.getStatus());
        
        // 根据章节状态设置进度和任务数
        if ("completed".equals(dto.getStatus())) {
            vo.setProgress(100);
            vo.setCompletedJobs(dto.getJobCount());
            vo.setTotalJobs(dto.getJobCount());
        } else if ("locked".equals(dto.getStatus())) {
            vo.setProgress(0);
            vo.setCompletedJobs(0);
            vo.setTotalJobs(dto.getJobCount());
        } else {
            vo.setProgress(0);
            vo.setCompletedJobs(0);
            vo.setTotalJobs(dto.getJobCount());
        }
        
        return vo;
    }

    /**
     * VO 转 DTO
     *
     * @param vo VO
     * @return DTO
     */
    public ChapterDTO toDTO(ChapterVO vo) {
        if (vo == null) {
            return null;
        }

        ChapterDTO dto = new ChapterDTO();
        dto.setId(vo.getId());
        dto.setTitle(vo.getTitle());
        dto.setStatus(vo.getStatus());
        return dto;
    }
}
