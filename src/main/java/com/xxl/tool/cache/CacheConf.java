package com.xxl.tool.cache;

/**
 * simpie local cache tool
 *
 * @author xuxueli 2018-01-22 21:37:34
 */
public class CacheConf {

    private String key;
    private long effectTime;
    private long expireTime;

    public CacheConf() {
    }
    public CacheConf(String key, long effectTime, long expireTime) {
        this.key = key;
        this.effectTime = effectTime;
        this.expireTime = expireTime;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getEffectTime() {
        return effectTime;
    }

    public void setEffectTime(long effectTime) {
        this.effectTime = effectTime;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    // util
    /**
     * 是否有效(未过期)
     *
     * @return
     */
    public boolean isValid(){
        return expireTime > System.currentTimeMillis();
    }

}