package com.bbs.auth.app.company;

import com.bbs.Result;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class JoinCompany {

    public static class Param {
    }

    @PutMapping("/company/staff")
    public Result<Boolean> join(@RequestBody Param param) {
        return null;
    }
}
