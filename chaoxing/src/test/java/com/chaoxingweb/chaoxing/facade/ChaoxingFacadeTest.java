package com.chaoxingweb.chaoxing.facade;

import com.chaoxingweb.chaoxing.dto.ChaoxingLoginDTO;
import com.chaoxingweb.chaoxing.vo.ChaoxingLoginResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 超星 Facade 测试
 */
@SpringBootTest
class ChaoxingFacadeTest {

    @Autowired
    private ChaoxingFacade chaoxingFacade;

    @Test
    void testLogin() {
        // 准备测试数据
        ChaoxingLoginDTO dto = new ChaoxingLoginDTO();
        dto.setUsername("test");
        dto.setPassword("123456");

        // 调用登录
        ChaoxingLoginResult result = chaoxingFacade.login(dto);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getChaoxingUserId());
        assertNotNull(result.getChaoxingUsername());
        assertNotNull(result.getCookie());
    }
}
