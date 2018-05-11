package cache.recycle;

import android.graphics.Bitmap;

/**
 * Created by chenming on 2018/5/10
 */
public interface BitmapPool {
    void put(Bitmap bitmap);

    /**
     * 获得一个可复用的bitmap
     * @param width
     * @param height
     * @param config
     * @return
     */
    Bitmap get(int width, int height, Bitmap.Config config);
}
