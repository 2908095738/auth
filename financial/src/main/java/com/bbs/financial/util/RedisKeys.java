package com.bbs.financial.util;

//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//
//@Component
//@Slf4j
//public class RedisUtil{
//
//    @Resource(name = "protoStuffTemplate")
//    private RedisTemplate<String, String> protoStuffTemplate;
//
//    /**
//     * 设置过期时间，单位秒
//     * @param key 键的名称
//     * @param timeout 过期时间
//     * @return 成功：true，失败：false
//     */
//    public boolean setExpireTime(String key, long timeout) {
//        return protoStuffTemplate.expire(key, timeout, TimeUnit.SECONDS);
//    }
//
//    /**
//     * 通过键删除一个值
//     * @param key 键的名称
//     */
//    public void delete(String key) {
//        protoStuffTemplate.delete(key);
//    }
//
//    /**
//     * 判断key是否存在
//     * @param key 键的名称
//     * @return 存在：true，不存在：false
//     */
//    public boolean hasKey(String key) {
//        return protoStuffTemplate.hasKey(key);
//    }
//
//    /**
//     * 数据存储
//     * @param key 键
//     * @param value 值
//     */
//    public void set(String key, String value) {
//        protoStuffTemplate.boundValueOps(key).set(value);
//    }
//
//    /**
//     * 数据存储的同时设置过期时间
//     * @param key 键
//     * @param value 值
//     * @param expireTime 过期时间
//     */
//    public void set(String key, String value, Long expireTime) {
//        protoStuffTemplate.boundValueOps(key).set(value, expireTime, TimeUnit.SECONDS);
//    }
//
//    /**
//     * 数据取值
//     * @param key 键
//     * @return 查询成功：值，查询失败，null
//     */
//    public Object get(String key) {
//        return protoStuffTemplate.opsForValue().get(key);
//    }
//
//    public List<String> mget(List<String> keys) {
//        return protoStuffTemplate.opsForValue().multiGet(keys);
//    }
//
//    public Integer incr(String key, Integer v){
//        Long increment = protoStuffTemplate.opsForValue().increment(key, v);
//        return increment.intValue();
//    }
//
//    public Integer decr(String key, Integer v){
//        Long increment = protoStuffTemplate.opsForValue().decrement(key, v);
//        return increment.intValue();
//    }
//
//    /**
//     * 如果存在则设置
//     * @param key 键
//     * @return
//     */
//    public Boolean setIfPresent(String key, String value) {
//        return protoStuffTemplate.opsForValue().setIfPresent(key, value);
//    }
//
//    //zset
//    public void zSet(String key, Set<ZSetOperations.TypedTuple<String>> tuples) {
//        protoStuffTemplate.opsForZSet().add(key, tuples);
//    }
//    public Set<String> zGet(String key, Double startScore, Double endScore) {
//        return protoStuffTemplate.opsForZSet().rangeByScore(key, startScore, endScore);
//    }
//
//    public Set<String> zGet(String key, Long startScore, Long endScore) {
//        return zGet(key, startScore.doubleValue(), endScore.doubleValue());
//    }
//
//
//    //hash
//    public void hashSet(String key, Map<String, String> values) {
//        protoStuffTemplate.opsForHash().putAll(key, values);
//    }
//
//
//    public Object hashGet(String key, String hashKey) {
//        return protoStuffTemplate.opsForHash().get(key, hashKey);
//    }
//
//    public Map<Object, Object> hashGet(String key){
//        return protoStuffTemplate.opsForHash().entries(key);
//    }
//
//    public List<Object> mhashGet(String key, Collection<Object> hashKeys) {
//        return protoStuffTemplate.opsForHash().multiGet(key, hashKeys);
//    }
//
//    public void hashSet(String key, Object hashKey, String value) {
//        protoStuffTemplate.opsForHash().put(key, hashKey, value);
//    }
//
//    public void hashPutAll(String key, Map map) {
//        protoStuffTemplate.opsForHash().putAll(key,map);
//    }
//
//    public void delHash(String key, Object hashKey) {
//        protoStuffTemplate.opsForHash().delete(key, hashKey);
//    }
//
//    public void hashIntr(String key, String hashKey, Integer v) {
//        protoStuffTemplate.opsForHash().increment(key,hashKey,v);
//    }
//
//
//
//
//    @Autowired(required = false)
//    public void setRedisTemplate(RedisTemplate redisTemplate) {
//        RedisSerializer stringSerializer = new StringRedisSerializer();//序列化为String
//        //不能反序列化
//        //Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);//序列化为Json
//        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
//        redisTemplate.setKeySerializer(stringSerializer);
//        redisTemplate.setValueSerializer(serializer);
//        redisTemplate.setHashKeySerializer(stringSerializer);
//        redisTemplate.setHashValueSerializer(serializer);
//        this.protoStuffTemplate = redisTemplate;
//    }
//
//
//    @Slf4j
//    @Component
//    public static class Redisson {
//        /**
//         * 暴力解锁
//         */
//        public static void forceUnlock(RLock lock) {
//            lock.forceUnlock();
//            log.warn("Redisson: 暴力解除 lock={}", lock.getName());
//        }
//
//        /**
//         * 加锁执行代码，抢锁失败 or 异常则直接执行
//         *
//         * @param function  代码
//         * @param lock      Redisson Lock
//         * @param waitTime  等待获取锁时间
//         * @param leaseTime 自动解锁时间
//         * @param unit      时间单位
//         * @param <R>       执行结果类型
//         * @return 代码执行结果
//         */
//        public static <R> R lockExec(RLock lock, int waitTime, int leaseTime, TimeUnit unit, Supplier<R> function) {
//            try {
//                if (lock.tryLock(waitTime, leaseTime, unit)) {
//                    log.debug("Redisson: 获取锁 key={}", lock.getName());
//                    try {
//                        TimeInterval timer = DateUtil.timer();
//                        R result = function.get();
//                        long interval = timer.interval();
//                        log.debug("Redisson: 分布式锁业务代码执行完成 key={}; 耗时（毫秒）={}", lock.getName(), interval);
//                        timer.interval();
//                        return result;
//                    } finally {
//                        if (lock.isLocked()) {   //判断是否持有锁，并释放
//                            lock.unlock();
//                            log.debug("Redisson: 释放锁 key={}", lock.getName());
//                        }
//                    }
//                }
//            } catch (InterruptedException e) {
//                log.error("Redisson: 分布式锁，中断异常！！！key={}", lock.getName());
//                e.printStackTrace();
//            }
//            if (lock.getHoldCount() > 0) forceUnlock(lock);  //出现异常后，依旧持有锁，则暴力解锁
//            return function.get();  //再执行业务
//        }
//
//        /**
//         * 加锁执行代码，抢锁失败 or 异常则直接执行
//         * @param function 代码
//         * @param lock Redisson Lock
//         * @param waitTime  等待获取锁时间
//         * @param leaseTime 自动解锁时间
//         * @param unit 时间单位
//         */
//        public static void lockExec(RLock lock, int waitTime, int leaseTime, TimeUnit unit, Runnable function) {
//            try {
//                if(lock.tryLock(waitTime, leaseTime, unit)) {
//                    log.debug("Redisson: 获取锁 key={}", lock.getName());
//                    try {
//                        TimeInterval timer = DateUtil.timer();
//                        function.run();
//                        long interval = timer.interval();
//                        log.debug("Redisson: 执行完成 key={}; 耗时（毫秒）={}", lock.getName(), interval);
//                        timer.interval();
//                        return;
//                    } finally {
//                        if(lock.isLocked()) {
//                            lock.unlock();
//                            log.debug("Redisson: 释放锁 key={}", lock.getName());
//                        }
//                    }
//                }
//            } catch (InterruptedException e) {
//                log.error("Redisson: 分布式锁，中断异常！！！key={}", lock.getName());
//                e.printStackTrace();
//            }
//            if(lock.getHoldCount() > 0) {
//                forceUnlock(lock);
//            }
//            function.run();
//        }
//    }
//}
