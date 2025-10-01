package com.xxl.tool.test.cache;

import com.xxl.tool.cache.iface.Cache;
import com.xxl.tool.cache.CacheTool;
import com.xxl.tool.cache.CacheType;
import com.xxl.tool.cache.iface.CacheListener;
import com.xxl.tool.cache.iface.CacheLoader;
import com.xxl.tool.core.StringTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class Test5 {
	private static final Logger logger = LoggerFactory.getLogger(Test5.class);

    // CacheType + capacity
	@Test
	public void test01() {
		Cache<String, String> cache = CacheTool.newFIFOCache(3).build();
        //Cache<String, String> cache = CacheTool.newLFUCache(3).build();
        //Cache<String, String> cache = CacheTool.newLRUCache(3).build();
        //Cache<String, String> cache = CacheTool.newUnlimitedCache().build();

		String key = "key01";

		// init
		String value = cache.get(key);
		logger.info("first get is null, value = " + value);
		Assertions.assertNull(value, "init fail");

		// set
		cache.put(key, "value01");
		value = cache.get(key);
		logger.info("set, value = " + value);
		Assertions.assertEquals("value01", value, "set fail");

        // remove
        cache.remove(key);
        value = cache.get(key);
        logger.info("remove, value = " + value);
        Assertions.assertNull(value, "remove fail");

        // size
        for (int i = 0; i < 10; i++) {
            cache.put("key"+i, String.valueOf(i));
        }
        logger.info("size = " + cache.size());
        logger.info("asMap = " + cache.asMap());
        Assertions.assertEquals(3, cache.size(), "limit size fail");
	}

    // expireAfterAccess
    @Test
    public void test02() throws InterruptedException {
        Cache<String, String> cache = CacheTool.newLRUCache(3)
                .expireAfterAccess(100)
                .build();

        // put
        String key = "key01";
        cache.put(key, "value01");

        // get
        String value = cache.get(key);
        logger.info("set, value = " + value);
        Assertions.assertEquals("value01", value, "set fail");

        // access
        TimeUnit.MILLISECONDS.sleep(70);
        value = cache.get(key);

        // expireAfterAccess
        TimeUnit.MILLISECONDS.sleep(70);
        value = cache.get(key);
        logger.info("get when timeout, value = " + value);
        Assertions.assertNotNull(value, "timeout fail");
    }

    // expireAfterWrite
    @Test
    public void test03() throws InterruptedException {
        Cache<String, String> cache = CacheTool.newLRUCache(3)
                .expireAfterWrite(100)
                .build();

        // put
        String key = "key01";
        cache.put(key, "value01");

        // get
        String value = cache.get(key);
        logger.info("set, value = " + value);
        Assertions.assertEquals("value01", value, "set fail");

        // access
        TimeUnit.MILLISECONDS.sleep(70);
        value = cache.get(key);

        // expireAfterAccess
        TimeUnit.MILLISECONDS.sleep(70);
        value = cache.get(key);
        logger.info("get when timeout, value = " + value);
        Assertions.assertNull(value, "timeout fail");
    }

    // pruneInterval
    @Test
    public void test04() throws InterruptedException {
        Cache<String, String> cache = CacheTool.newLRUCache()
                .expireAfterAccess(5000)
                .pruneInterval(100)
                .build();

        // batch 1
        for (int i = 0; i < 5; i++) {
            TimeUnit.MILLISECONDS.sleep(1000);
            cache.put("key-" + i, "value-" + i);
        }

        // print
        for (int i = 0; i < 10; i++) {
            TimeUnit.MILLISECONDS.sleep(1000);
            cache.get("key-1");
            logger.info("size = " + cache.size());
        }
    }

    // pruneInterval
    @Test
    public void test05() throws InterruptedException {
        Cache<String, String> cache = CacheTool.<String,String>newLRUCache()
                .loader(new CacheLoader<>() {
                    @Override
                    public String load(String key) throws Exception {
                        return "value-" + key;
                    }
                })
                .build();

        String key = "key01";

        // getIfPresent
        String value = cache.getIfPresent(key);
        logger.info("getIfPresent, value = " + value);
        Assertions.assertNull(value, "getIfPresent fail");

        // get

        value = cache.get(key);
        logger.info("get, value = " + value);
        Assertions.assertEquals("value-" + key, value, "set fail");
    }

    @Test
    public void test06() throws InterruptedException {
        Cache<String, String> cache = CacheTool.<String,String>newLRUCache()
                .listener(new CacheListener<>() {
                    @Override
                    public void onRemove(String key, String value) throws Exception {
                        logger.info("onRemove, key = " + key + ", value = " + value);
                    }
                })
                .build();

        // get
        String key = "key01";
        cache.put(key, "value01");
        logger.info("put, value = " + cache.get(key));
        Assertions.assertEquals("value01", cache.get(key), "set fail");

        // remove
        cache.remove(key);
        logger.info("remove, value = " + cache.get(key));

        // get
        Assertions.assertNull(cache.get(key), "remove fail");
    }

    // get + loader
    @Test
    public void test12() throws InterruptedException {

        Cache<String, String> cache = CacheTool.newLRUCache()
                .build();

        for (int i = 0; i < 5; i++) {
            String key = "key-" + i;
            String value = cache.get(key, new CacheLoader<String, String>() {
                @Override
                public String load(String key) throws Exception {
                    String value = "value-" + key;
                    logger.info(StringTool.format("load: key={0}, value={1}", key, value));
                    return value;
                }
            });
            logger.info(StringTool.format("get: key={0}, value={1}", key, value));
            Assertions.assertEquals("value-" + key, value, "get fail");
        }

        for (int i = 0; i < 10; i++) {
            String key = "key-" + i;
            logger.info(StringTool.format("get: key={0}, value={1}", key, cache.get(key)));
        }
    }

    // monitor
    @Test
    public void test13() throws InterruptedException {
        Cache<String, String> cache = CacheTool.newCache()
                .expireAfterAccess(10 * 1000)
                .capacity(70)
                .build();

        for (int i = 0; i < 100; i++) {
            String key = "key-" + i;
            String value = cache.get(key, new CacheLoader<>() {
                @Override
                public String load(String key) throws Exception {
                    return "value-" + key;
                }
            });
            //logger.info(StringTool.format("result: key={0}, value={1}", key, value));
        }

        logger.info(StringTool.format(""" 
                》》monitor:
                hitCount:{0} ,
                missCount:{1} ,
                size:{2} ,
                isEmpty:{3} ,
                isFull:{4} ,
                """, cache.hitCount(), cache.missCount(), cache.size(), cache.isEmpty(), cache.isFull()));

        for (int i = 0; i < 100; i++) {
            String key = "key-" + i;
            cache.get( key);
        }

        logger.info(StringTool.format("""   
                》》monitor:
                hitCount:{0} ,
                missCount:{1} ,
                size:{2} ,
                isEmpty:{3} ,
                isFull:{4} ,
                """, cache.hitCount(), cache.missCount(), cache.size(), cache.isEmpty(), cache.isFull()));


    }


    @Test
    public void test14() throws InterruptedException {
        Cache<String, String> cache = CacheTool.<String, String>newLRUCache()
                .expireAfterAccess(10 * 1000)
                .capacity(1000)
                .cache(CacheType.LFU)
                .loader(new CacheLoader<>() {
                    @Override
                    public String load(String key) throws Exception {
                        TimeUnit.MILLISECONDS.sleep(50);
                        return MessageFormat.format("data={0}, time={1}", key, DateFormat.getDateTimeInstance().format(new Date()));
                    }
                })
                .build();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            String key = "key-" + i;
            System.out.println(MessageFormat.format("result: key={0}, value={1}", key, cache.get(key)));
        }
        System.out.println("cost " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            String key = "key2-" + i;
            System.out.println(MessageFormat.format("result: key={0}, value={1}", key, cache.getIfPresent(key)));
        }
        System.out.println("cost2 " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        ;
        for (int i = 0; i < 100; i++) {
            String key = "key1-" + i;
            System.out.println(MessageFormat.format("result: key={0}, value={1}", key, cache.getIfPresent(key)));
        }
        System.out.println("cost3 " + (System.currentTimeMillis() - start));
    }

}
