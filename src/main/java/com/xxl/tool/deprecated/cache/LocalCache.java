//package com.xxl.tool.cache;
//
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//public class LocalCache<K, V> {
//    private final ConcurrentHashMap<K, CacheEntry<V>> cacheMap = new ConcurrentHashMap<>();
//    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
//
//    public LocalCache(long expirationTime, TimeUnit timeUnit) {
//        executorService.scheduleAtFixedRate(this::removeExpiredEntries, expirationTime, expirationTime, timeUnit);
//    }
//
//    public void put(K key, V value, long duration, TimeUnit timeUnit) {
//        long expirationTime = System.currentTimeMillis() + timeUnit.toMillis(duration);
//        cacheMap.put(key, new CacheEntry<>(value, expirationTime));
//    }
//
//    public V get(K key) {
//        CacheEntry<V> entry = cacheMap.get(key);
//        if (entry != null && entry.isExpired()) {
//            cacheMap.remove(key);
//            return null;
//        }
//        return entry != null ? entry.getValue() : null;
//    }
//
//    public void remove(K key) {
//        cacheMap.remove(key);
//    }
//
//    private void removeExpiredEntries() {
//        long currentTime = System.currentTimeMillis();
//        cacheMap.entrySet().removeIf(entry -> entry.getValue().isExpired(currentTime));
//    }
//
//    private static class CacheEntry<V> {
//        private final V value;
//        private final long expirationTime;
//
//        public CacheEntry(V value, long expirationTime) {
//            this.value = value;
//            this.expirationTime = expirationTime;
//        }
//
//        public V getValue() {
//            return value;
//        }
//
//        public boolean isExpired() {
//            return isExpired(System.currentTimeMillis());
//        }
//
//        public boolean isExpired(long currentTime) {
//            return currentTime > expirationTime;
//        }
//    }
//
//    public void shutdown() {
//        executorService.shutdown();
//    }
//}