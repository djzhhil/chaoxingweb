package com.chaoxingweb.chaoxing.facade;

import com.chaoxingweb.chaoxing.dto.ChaoxingLoginDTO;
import com.chaoxingweb.chaoxing.vo.ChaoxingLoginResult;

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
}
