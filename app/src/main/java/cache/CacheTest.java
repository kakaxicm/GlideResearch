package cache;

import cache.recycle.LruBitmapPool;
import cache.recycle.Resource;

/**
 * Created by chenming on 2018/5/10
 */
public class CacheTest implements Resource.ResourceListener, MemoryCache.OnResourceRemoveListener {
    private LruMemoryCache lruMemoryCache;
    private ActiveResource activeResource;
    private LruBitmapPool lruBitmapPool;

    /**
     * 测试缓存读取用例
     * @param key
     * @return
     */
    public Resource test(Key key) {
        lruMemoryCache = new LruMemoryCache(10);
        //Lru移除资源监听
        lruMemoryCache.setOnResourceRemoveListener(this);
        //引用计数为0释放时候监听
        activeResource = new ActiveResource(this);

        lruBitmapPool = new LruBitmapPool(10);

        /**
         * step1:从活动资源中查找
         */
        Resource resource =activeResource.get(key);
        if(resource != null){
            //引用计数+1
            resource.acquire();
            return resource;
        }

        /**
         * step2:内存缓存中取，如果有则移除转移到活动缓存,活动内存和缓存内存互斥
         */
        if(resource == null){
            resource = lruMemoryCache.get(key);
            if(resource != null){
                /**
                 * 内存缓存移除原因:
                 * 1.lru算法决定，图片可能被释放，如果同时存在内存缓存和活动内存中,
                 * 则活动内存读取的图片可能在LRU缓存中已经被回收，所以活动内存有可能失效
                 */
                //主动移除，不走LRU的移除回调
                lruMemoryCache.remove2(key);
                //引用计数+1
                resource.acquire();
                //加入活动缓存
                activeResource.activate(key, resource);
                return resource;
            }
        }

        return null;
    }

    /**
     * 这个资源没有被引用,则从活动缓存中移除，加入LRU缓存
     * @param key
     * @param resource
     */
    @Override
    public void onResourceReleased(Key key, Resource resource) {
        activeResource.deactive(key);
        lruMemoryCache.put(key, resource);
    }

    /**
     * 从内存缓存被动移除时候回调
     * @param resource
     */
    @Override
    public void onResourceRemove(Resource resource) {
        //图片LRU被移除后，放到图片复用池,LUR的移除有两种渠道:
        // 1.LRU被动移除
        // 2.加入活动内存时移除,第二种不需要放入复用池,已经在内存缓存中处理好
        //如果没有复用池，则可以直接释放resource了
        lruBitmapPool.put(resource.getBitmap());

    }
}
