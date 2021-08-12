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
