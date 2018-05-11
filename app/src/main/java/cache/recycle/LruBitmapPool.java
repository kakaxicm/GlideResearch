package cache.recycle;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

import java.util.NavigableMap;
import java.util.TreeMap;

import cache.recycle.BitmapPool;

/**
 * Created by chenming on 2018/5/10
 * key为bp大小
 */
public class LruBitmapPool extends LruCache<Integer, Bitmap> implements BitmapPool {
    NavigableMap<Integer, Integer> map = new TreeMap<>();//按key排序的集合，保存大小
    private static final int MAX_OVER_SIZE_FACTOR = 2;
    private boolean isRemoved;

    /**
     * @param maxSize
     */
    public LruBitmapPool(int maxSize) {
        super(maxSize);
    }

    /**
     * 只存 isMutable为true的图片
     */
    @Override
    public void put(Bitmap bitmap) {
        //最后一级缓存，如果图片不复用，则回收
        if (!bitmap.isMutable()) {
            bitmap.recycle();
            return;
        }
        int size = getSize(bitmap);
        //图片太大，回收一边玩去
        if (size >= maxSize()) {
            bitmap.recycle();
            return;
        }

        put(size, bitmap);
        map.put(size, 0);//大小存入排序集合
    }

    /**
     * 复用的读操作
     * @param width
     * @param height
     * @param config
     * @return
     */
    @Override
    public Bitmap get(int width, int height, Bitmap.Config config) {
        //拿到bp占用的内存
        int size = width * height * (config == Bitmap.Config.ARGB_8888 ? 4 : 2);
        //取集合中比size大的复用bp
        //获得大于或者等于size的复用bp
        Integer key = map.ceilingKey(size);
        if(key != null && key <= size *MAX_OVER_SIZE_FACTOR ){
            //主动remove
            isRemoved = true;
            Bitmap bp = remove(key);
            isRemoved = false;
            return bp;
        }
        return null;
    }

    @Override
    protected int sizeOf(Integer key, Bitmap value) {
        return getSize(value);
    }

    @Override
    protected void entryRemoved(boolean evicted, Integer key, Bitmap oldValue, Bitmap newValue) {
        map.remove(key);//移除大小
        if(!isRemoved){
            //被动移除则回收图片
            oldValue.recycle();
        }
    }

    private int getSize(Bitmap bp) {
        //4.4
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.4以后bp内存复用机制，getAllocationByteCount才能得到准确的大小
            return bp.getAllocationByteCount();
        }
        return bp.getByteCount();
    }
}
