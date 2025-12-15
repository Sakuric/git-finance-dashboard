package com.example.financedashboard.mapper;

import com.example.financedashboard.entity.BacktestResult;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BacktestResultMapper {

    @Insert("INSERT INTO backtest_result (advice_id, prompt_id, total_return, annualized_return, " +
            "max_drawdown, sharpe_ratio, win_rate, volatility, backtest_start_date, backtest_end_date, " +
            "backtest_duration, is_success, failure_reason) " +
            "VALUES (#{adviceId}, #{promptId}, #{totalReturn}, #{annualizedReturn}, #{maxDrawdown}, " +
            "#{sharpeRatio}, #{winRate}, #{volatility}, #{backtestStartDate}, #{backtestEndDate}, " +
            "#{backtestDuration}, #{isSuccess}, #{failureReason})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(BacktestResult backtestResult);

    @Select("SELECT * FROM backtest_result WHERE advice_id = #{adviceId}")
    BacktestResult findByAdviceId(Long adviceId);

    @Select("SELECT * FROM backtest_result WHERE id = #{id}")
    BacktestResult findById(Long id);

    @Select("SELECT * FROM backtest_result ORDER BY created_at DESC LIMIT #{limit}")
    List<BacktestResult> findRecent(int limit);
}
