package com.back2261.matchservice.domain.service;

import com.back2261.matchservice.interfaces.response.PredictFeignResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "predict-service", url = "${predict-service.url}")
public interface PredictFeignService {
    @PostMapping(value = "/predict")
    PredictFeignResponse predict(@RequestParam String user_id);
}
