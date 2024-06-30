//package com.xxl.tool.cache;
//
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//
///**
// * local cache tool
// *
// * // TODO，data + conf + manage + tool；超时、定长、LRU、LFU、主动更新
// *
// * @author xuxueli 2018-01-22 21:37:34
// */
//public class CacheTool {
//
//    // cache data
//    private static ConcurrentMap<String, CacheData> cacheRepository = new ConcurrentHashMap<>();
//
//
//    /**
//     * set cache
//     *
//     * @param key
//     * @param val
//     * @param cacheTime
//     * @return
//     */
//    public static boolean set(String key, Object val, long cacheTime){
//
//        // clean timeout cache, before set new cache (avoid cache too much)
//        cleanTimeoutCache();
//
//        // set new cache
//        if (key==null || key.trim().length()==0) {
//            return false;
//        }
//        if (val == null) {
//            remove(key);
//        }
//        if (cacheTime <= 0) {
//            remove(key);
//        }
//        long timeoutTime = System.currentTimeMillis() + cacheTime;
//        CacheData localCacheData = new CacheData(key, val, timeoutTime);
//        cacheRepository.put(localCacheData.getKey(), localCacheData);
//        return true;
//    }
//
//    /**
//     * remove cache
//     *
//     * @param key
//     * @return
//     */
//    public static boolean remove(String key){
//        if (key==null || key.trim().length()==0) {
//            return false;
//        }
//        cacheRepository.remove(key);
//        return true;
//    }
//
//    /**
//     * get cache
//     *
//     * @param key
//     * @return
//     */
//    public static Object get(String key){
//        if (key==null || key.trim().length()==0) {
//            return null;
//        }
//        CacheData localCacheData = cacheRepository.get(key);
//        if (localCacheData!=null && System.currentTimeMillis()<localCacheData.getTimeoutTime()) {
//            return localCacheData.getVal();
//        } else {
//            remove(key);
//            return null;
//        }
//    }
//
//    /**
//     * clean timeout cache
//     *
//     * @return
//     */
//    public static boolean cleanTimeoutCache(){
//        if (!cacheRepository.keySet().isEmpty()) {
//            for (String key: cacheRepository.keySet()) {
//                CacheData localCacheData = cacheRepository.get(key);
//                if (localCacheData!=null && System.currentTimeMillis()>=localCacheData.getTimeoutTime()) {
//                    cacheRepository.remove(key);
//                }
//            }
//        }
//        return true;
//    }
//
//}
