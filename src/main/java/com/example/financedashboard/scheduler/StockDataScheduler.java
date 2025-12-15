package com.example.financedashboard.scheduler;

import com.example.financedashboard.service.sina.SinaDataPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockDataScheduler {

    private final SinaDataPersistenceService persistenceService;

    @EventListener(ApplicationReadyEvent.class)
    public void updateStockDataOnStartup() {
        log.info("应用启动完成，开始更新股票价格和成交量...");

        try {
            persistenceService.syncAllStockDetails();
            log.info("启动时股票数据更新完成");

        } catch (Exception e) {
            log.error("启动时更新股票数据失败", e);
        }
    }
}
