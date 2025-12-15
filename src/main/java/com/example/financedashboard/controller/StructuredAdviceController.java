package com.example.financedashboard.controller;

import com.example.financedashboard.entity.StructuredAdvice;
import com.example.financedashboard.mapper.StructuredAdviceMapper;
import com.example.financedashboard.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/structured-advice")
@RequiredArgsConstructor
public class StructuredAdviceController {

    private final StructuredAdviceMapper structuredAdviceMapper;

    @GetMapping("/{adviceId}")
    public Result<List<StructuredAdvice>> getStructuredAdvice(@PathVariable Long adviceId) {
        List<StructuredAdvice> advices = structuredAdviceMapper.findByAdviceId(adviceId);
        if (advices.isEmpty()) {
            return Result.error(404, "未找到该建议的结构化数据");
        }
        return Result.success(advices);
    }
}
