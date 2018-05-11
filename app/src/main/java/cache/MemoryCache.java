package cache;

import cache.recycle.Resource;

/**
 * Created by chenming on 2018/5/9
 * 内存缓存标准，操作Resource
 */
public interface MemoryCache {
    /**
     * Resource从缓存中移除回调
     */
    interface OnResourceRemoveListener{
        void onResourceRemove(Resource resource);
    }

    Resource put(Key key, Resource resource);

    Resource remove(Key key);

    Resource remove2(Key key);

    void setOnResourceRemoveListener(OnResourceRemoveListener listener);
}
