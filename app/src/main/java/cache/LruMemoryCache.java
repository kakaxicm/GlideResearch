package cache;

import android.os.Build;
import android.util.LruCache;

import cache.recycle.Resource;

/**
 * Created by chenming on 2018/5/9
 */
public class LruMemoryCache extends LruCache<Key, Resource> implements MemoryCache {
    private OnResourceRemoveListener listener;
    private boolean isRemoved;

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public LruMemoryCache(int maxSize) {
        super(maxSize);
    }



    @Override
    protected int sizeOf(Key key, Resource value) {
        //4.4
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            //4.4以后bp内存复用机制，getAllocationByteCount才能得到准确的大小
            return value.getBitmap().getAllocationByteCount();
        }
        return value.getBitmap().getByteCount();
    }

    /**
     * 被动移除时，暴露接口，图片放到BP复用池
     * @param evicted
     * @param key
     * @param oldValue
     * @param newValue
     */
    @Override
    protected void entryRemoved(boolean evicted, Key key, Resource oldValue, Resource newValue) {
        //oldValue 给复用池使用
        if(listener != null && oldValue != null && !isRemoved){
            listener.onResourceRemove(oldValue);
        }
    }

    /**
     * 主动移除
     * @param key
     * @return
     */
    @Override
    public Resource remove2(Key key) {
        //主动移除不会调用onResourceRemove回调
        isRemoved = true;
        Resource resource = remove(key);
        isRemoved = false;
        return resource;
    }

    /**
     * 暴露给复用池？（TODO）
     * @param listener
     */
    @Override
    public void setOnResourceRemoveListener(OnResourceRemoveListener listener) {
        this.listener = listener;
    }
}
