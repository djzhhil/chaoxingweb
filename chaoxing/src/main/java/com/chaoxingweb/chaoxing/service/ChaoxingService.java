package com.chaoxingweb.chaoxing.service;

import com.chaoxingweb.chaoxing.dto.ChaoxingLoginDTO;
import com.chaoxingweb.chaoxing.vo.ChaoxingLoginResult;

/**
 * 超星服务接口
 */
public interface ChaoxingService {

    /**
     * 超星登录
     *
     * @param dto 登录信息
     * @return 登录结果
     */
    ChaoxingLoginResult login(ChaoxingLoginDTO dto);
}
