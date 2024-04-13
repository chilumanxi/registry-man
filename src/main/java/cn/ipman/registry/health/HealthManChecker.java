package cn.ipman.registry.health;

import cn.ipman.registry.health.HealthChecker;
import cn.ipman.registry.model.InstanceMeta;
import cn.ipman.registry.service.RegistryManService;
import cn.ipman.registry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 探活
 *
 * @Author IpMan
 * @Date 2024/4/13 20:42
 */
@Slf4j
public class HealthManChecker implements HealthChecker {

    RegistryService registryService;

    public HealthManChecker(RegistryService registryService) {
        this.registryService = registryService;
    }

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    // 默认20s超时后取消注册, 代表需要探活
    long timeout = 20_000;

    @Override
    public void start() {
        executor.scheduleWithFixedDelay(() -> {
            System.out.println("Health checker running...");
            long now = System.currentTimeMillis();
            RegistryManService.TIMESTAMPS.keySet().forEach(serviceAndInstance -> {
                long timestamp = RegistryManService.TIMESTAMPS.get(serviceAndInstance);
                if (now - timestamp > timeout) {
                    log.info(" === > Health checker: {} is down", serviceAndInstance);
                    int index = serviceAndInstance.indexOf("@");
                    String service = serviceAndInstance.substring(0, index);
                    String url = serviceAndInstance.substring(index + 1);

                    InstanceMeta instanceMeta = InstanceMeta.from(url);
                    registryService.unregister(service, instanceMeta);
                    RegistryManService.TIMESTAMPS.remove(service);
                }
            });
        }, 10, 30, TimeUnit.SECONDS);

    }

    @Override
    public void stop() {
        executor.shutdown();
    }
}