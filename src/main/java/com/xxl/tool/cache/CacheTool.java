package com.xxl.tool.cache;

import com.xxl.tool.core.StringTool;

/**
 * local cache tool
 *
 * @author xuxueli 2018-01-22 21:37:34
 */
public class CacheTool {

    /**
     * cache manage
     */
    private static CacheManage cacheManage = null;
    static {
        cacheManage = new CacheManage();
    }


    /**
     * set cache
     *
     * @param key
     * @param val
     * @param liveTime      : 存活时间，单位毫秒
     * @return
     */
    public static boolean set(String key, Object val, long liveTime){

        // check capacity (avoid big object)
        cacheManage.capacityCheck();

        // valid param
        if (StringTool.isBlank(key)) {
            return false;
        }
        if (val == null || liveTime <= 0) {
            remove(key);
        }

        // make config
        long effectTime = System.currentTimeMillis();
        long expireTime = effectTime + liveTime;
        CacheConf cacheConf = new CacheConf(key, effectTime, expireTime);

        // set
        cacheManage.getCacheData().put(key, val);
        cacheManage.getCacheConf().put(key, cacheConf);
        return true;
    }

    /**
     * remove cache
     *
     * @param key
     * @return
     */
    public static boolean remove(String key){
        // valid param
        if (StringTool.isBlank(key)) {
            return false;
        }

        // remove
        cacheManage.getCacheData().remove(key);
        cacheManage.getCacheConf().remove(key);
        return true;
    }

    /**
     * get cache
     *
     * @param key
     * @return
     */
    public static Object get(String key){
        // valid param
        if (StringTool.isBlank(key)) {
            return false;
        }

        // load
        Object cacheVal = cacheManage.getCacheData().get(key);
        CacheConf cacheConf = cacheManage.getCacheConf().get(key);

        // valid exist
        if (cacheVal==null || cacheConf==null) {
            return null;
        }

        // valid timeout
        if (!cacheConf.isValid()) {
            remove(key);
            return null;
        }

        return cacheVal;
    }


}
