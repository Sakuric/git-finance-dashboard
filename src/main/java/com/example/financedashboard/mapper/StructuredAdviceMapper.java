package com.example.financedashboard.mapper;

import com.example.financedashboard.entity.StructuredAdvice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StructuredAdviceMapper {

    int insert(StructuredAdvice advice);

    int batchInsert(@Param("list") List<StructuredAdvice> list);

    List<StructuredAdvice> findByAdviceId(@Param("adviceId") Long adviceId);

    StructuredAdvice findById(@Param("id") Long id);

    int updateById(StructuredAdvice advice);
}
