package com.auth.app.register;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class VXProgramRegister {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Param {

        // 手机号获取凭证
        private String code;
    }

    @PostMapping("/vx")
    public void register(@RequestBody Param param1) {


    }
}
