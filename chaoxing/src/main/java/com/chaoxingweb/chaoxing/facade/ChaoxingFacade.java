package com.chaoxingweb.chaoxing.facade;

import com.chaoxingweb.chaoxing.dto.ChaoxingLoginDTO;
import com.chaoxingweb.chaoxing.vo.ChaoxingLoginResult;

/**
 * 超星模块 Facade
 *
 * 提供给 auth 模块使用的接口，用于调用 chaoxing 模块内部的业务逻辑
 */
public interface ChaoxingFacade {

    /**
     * 超星登录
     *
     * @param dto 登录信息
     * @return 登录结果
     */
    ChaoxingLoginResult login(ChaoxingLoginDTO dto);
}
