package cache.recycle;

import android.graphics.Bitmap;

import cache.Key;

/**
 * Created by chenming on 2018/5/9
 * 作用:活动和内存缓存，用于查看引用计数和回收情况
 */
public class Resource {

    private Bitmap bitmap;
    private Key key;//hashmap的key
    //引用计数,用于释放
    private int acquired;


    private ResourceListener listener;

    public Resource(Bitmap bp) {
        this.bitmap = bp;
    }

    /**
     * 当引用计数为0时候，回调, 给活动资源用,调用时将bp移除加到内存缓存中
     */
    public interface ResourceListener {
        //本Resource完全释放回调
        void onResourceReleased(Key key, Resource resource);
    }

    /**
     * 活动内存key对应的资源设置监听
     *
     * @param listener
     */
    public void setListener(Key key, ResourceListener listener) {
        this.key = key;
        this.listener = listener;
    }

    /**
     * 引用计数减一
     */
    public void release() {
        if (--acquired == 0) {
            if (listener != null) {
                listener.onResourceReleased(key, this);
            }
        }
    }

    /**
     * 引用计数加1
     */
    public void acquire() {
        if (bitmap == null || bitmap.isRecycled()) {
            throw new IllegalStateException("bp 已经回收");
        }
        acquired++;
    }

    /**
     * 引用计数为0释放bp
     */
    public void recycle() {
        if (acquired > 0) {
            return;
        }
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

}
