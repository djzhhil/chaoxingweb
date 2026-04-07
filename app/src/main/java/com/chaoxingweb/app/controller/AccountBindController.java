package com.chaoxingweb.app.controller;

import com.chaoxingweb.common.result.Result;
import com.chaoxingweb.course.service.AccountBindingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/account")
public class AccountBindController {
    private final AccountBindingService accountBindingService;

    public AccountBindController(AccountBindingService accountBindingService) {
        this.accountBindingService = accountBindingService;
    }

     /**
      * 绑定超星账号
      */
     @PostMapping("/bind-chaoxing")
     public Result<Void> bindChaoxingAccount(Long userId, String username, String password, String cookie, boolean useCookie) {
         accountBindingService.bindChaoxingAccount(userId, username, password, cookie, useCookie);
         return Result.success();
     }

     /**
      * 解绑超星账号
      */
     @PostMapping("/unbind-chaoxing")
     public Result<Void> unbindChaoxingAccount(Long userId) {
         accountBindingService.unbindChaoxingAccount(userId);
         return Result.success();
     }
}
