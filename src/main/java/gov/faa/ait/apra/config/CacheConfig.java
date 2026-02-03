/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 * 
 * APRA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package gov.faa.ait.apra.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CHART_CYCLES_CACHE = "chartCycles";
    public static final String VFR_CHART_CYCLES_CACHE = "vfrChartCycles";
    public static final String TAC_CYCLES_CACHE = "tacCycles";
    public static final String HELICOPTER_CYCLES_CACHE = "helicopterCycles";
    public static final String WALL_PLANNING_CYCLES_CACHE = "wallPlanningCycles";
    public static final String URL_VERIFICATION_CACHE = "urlVerification";

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeineCacheBuilder());
        cacheManager.setCacheNames(java.util.List.of(
            CHART_CYCLES_CACHE,
            VFR_CHART_CYCLES_CACHE,
            TAC_CYCLES_CACHE,
            HELICOPTER_CYCLES_CACHE,
            WALL_PLANNING_CYCLES_CACHE,
            URL_VERIFICATION_CACHE
        ));
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(24, TimeUnit.HOURS)
            .recordStats();
    }

    @Bean
    public Caffeine<Object, Object> chartCycleCaffeine() {
        return Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .recordStats();
    }

    @Bean
    public Caffeine<Object, Object> urlVerificationCaffeine() {
        return Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats();
    }
}
