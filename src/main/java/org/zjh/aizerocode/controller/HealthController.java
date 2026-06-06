package org.zjh.aizerocode.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zjh.aizerocode.common.BaseResponse;
import org.zjh.aizerocode.common.ResultUtils;

/**
 * @author kayson
 * @since 2026/6/5 18:42
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/check")
    public BaseResponse<String> check() {
        return ResultUtils.success("ok");
    }
}
