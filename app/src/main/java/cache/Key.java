package cache;

import java.security.MessageDigest;

/**
 * Created by chenming on 2018/5/9
 */
public interface Key {
    void updateDiskCacheKey(MessageDigest md);
}
