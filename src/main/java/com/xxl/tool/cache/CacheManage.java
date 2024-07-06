package com.xxl.tool.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * cache manage
 *
 * @author xuxueli 2018-01-22 21:37:34
 */
public class CacheManage {

    private ConcurrentMap<String, Object> cacheData = new ConcurrentHashMap<>();
    private ConcurrentMap<String, CacheConf> cacheConf = new ConcurrentHashMap<>();
    private long maxLenth = 10000;

    // set、get
    public ConcurrentMap<String, Object> getCacheData() {
        return cacheData;
    }

    public void setCacheData(ConcurrentMap<String, Object> cacheData) {
        this.cacheData = cacheData;
    }

    public ConcurrentMap<String, CacheConf> getCacheConf() {
        return cacheConf;
    }

    public void setCacheConf(ConcurrentMap<String, CacheConf> cacheConf) {
        this.cacheConf = cacheConf;
    }

    public long getMaxLenth() {
        return maxLenth;
    }

    public void setMaxLenth(long maxLenth) {
        this.maxLenth = maxLenth;
    }

    // tool

    /**
     * capacity check
     */
    public void capacityCheck() {

        // capacity 0-50%：pass
        if (cacheConf.size() < maxLenth * 0.5) {
            return;
        }
        // capacity 90-100%：random clear
        if (cacheConf.size() < maxLenth * 0.9) {
            if (new Random().nextBoolean()) {
                return;
            }
        }

        // capacity Reach 100%：must clear
        // clear expired data
        Set<String> keysToRemove = new HashSet<>();
        for (Map.Entry<String, CacheConf> entry: cacheConf.entrySet()) {
            if (!entry.getValue().isValid()) {
                keysToRemove.add(entry.getKey());
            }
        }
        removeKeys(keysToRemove);

        // valid capacity, do remove if exceed capacity
        long clearCnt = cacheConf.size() - maxLenth;
        if (clearCnt > 0) {
            Set<String> keysToCut = new HashSet<>();    // TODO，》》 LRU、LFU
            // collect key
            for (String key: cacheConf.keySet()) {
                keysToCut.add(key);
                if (keysToCut.size() >= clearCnt) {
                    break;
                }
            }
            // remove
            removeKeys(keysToCut);
        }

    }

    private void removeKeys(Set<String> keys){
        if (!keys.isEmpty()) {
            for (String key : keys) {
                cacheData.remove(key);
                cacheConf.remove(key);
            }
        }
    }


}
