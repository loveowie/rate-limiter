package pers.hyx.limiter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author heyouxin
 * @since 2021/7/29/0029 11:47
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RateLimiterUtils {

    private final static String REDIS_LIMIT_KEY_PREFIX = "limiter:";
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<Long> limitRedisScript;

    public boolean shouldLimited(String key, long max, long timeout, TimeUnit timeUnit) {
        key = REDIS_LIMIT_KEY_PREFIX + key;
        long ttl = timeUnit.toMillis(timeout);
        long now = Instant.now().toEpochMilli();
        long expired = now - ttl;
        String finalKey = key;
        Long executeTimes = redisTemplate.execute((RedisCallback<Long>) connection -> {
            Object nativeConnection = connection.getNativeConnection();
            String script = limitRedisScript.getScriptAsString();
            if (nativeConnection instanceof JedisCluster) {
                return (Long) ((JedisCluster) nativeConnection).eval(script, Collections.singletonList(finalKey), Arrays.asList(String.valueOf(now), String.valueOf(ttl), String.valueOf(expired), String.valueOf(max)));
            }
            else if (nativeConnection instanceof Jedis) {
                return (Long) ((Jedis) nativeConnection).eval(script, Collections.singletonList(finalKey), Arrays.asList(String.valueOf(now), String.valueOf(ttl), String.valueOf(expired), String.valueOf(max)));
            }
            return 0L;
        });
        if (executeTimes != null) {
            if (executeTimes == 0) {
                log.error("【{}】在单位时间 {} 毫秒内已达到访问上限，当前接口上限 {}", key, ttl, max);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
