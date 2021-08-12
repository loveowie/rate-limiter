>基于redis的限流组件

支持两种使用方法：

1. AOP注解拦截限流

```java
@RateLimiter(key = "xxxx", max = 100, timeout = 1, timeUnit = TimeUnit.MINUTES)
```

- **key**   限流key

- **max**  限流最大值

- **timeout** 限流时间

- **timeUnit** 限流时间单位

  即在设置时间内，接口访问超过max次数时，会触发限流规则



2. 自定义方法限流

```java
boolean limited = rateLimiterUtils.shouldLimited("xxxx-1", 30, 1, TimeUnit.MINUTES);
if (limited) {
    return null;
}
```

- 传参与注解一致
- 返回值为true代表被限流





##### 实现lua脚本

```lua
local key = KEYS[1]
local now = tonumber(ARGV[1])
local ttl = tonumber(ARGV[2])
local expired = tonumber(ARGV[3])
local max = tonumber(ARGV[4])

-- 清除过期的数据
redis.call('zremrangebyscore', key, 0, expired)

-- 获取 zset 中的当前元素个数
local current = tonumber(redis.call('zcard', key))
local next = current + 1

-- 如果当前个数大于限流最大值，则触发限流
if next > max then
  return 0;
else
  redis.call("zadd", key, now, now)
  -- 每次访问均重新设置 zset 的过期时间
  redis.call("pexpire", key, ttl)
  return next
end
```

