package cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cache.recycle.Resource;

/**
 * Created by chenming on 2018/5/10
 * 活动图片缓存
 */
public class ActiveResource {
    private final Resource.ResourceListener listener;
    //resource的弱引用集合
    private Map<Key, ResourceWeakReference> activeResources = new HashMap<>();
    private ReferenceQueue<Resource> queue;//用于引用被回收通知
    private Thread cleanReferenceQueueThread;//回收线程监听
    private boolean isShutDown;//开关标记

    /**
     * Resource的释放监听
     *
     * @param listener
     */
    public ActiveResource(Resource.ResourceListener listener) {
        this.listener = listener;
    }

    /**
     * 加入活动资源缓存
     *
     * @param key
     * @param resource
     */
    public void activate(Key key, Resource resource) {
        resource.setListener(key, listener);
        //活动资源添加
        activeResources.put(key, new ResourceWeakReference(key, resource, getReferenceQueue()));
    }

    /**
     * 移除活动缓存,返回的Resource用于加入内存缓存
     *
     * @param key
     */
    public Resource deactive(Key key) {
        //移除
        ResourceWeakReference reference = activeResources.remove(key);
        if (reference != null) {
            return reference.get();
        }
        return null;
    }

    /**
     * 获得对应Resource
     * @param key
     * @return
     */
    public Resource get(Key key){
        //获取
        ResourceWeakReference reference = activeResources.get(key);
        if (reference != null) {
            return reference.get();
        }
        return null;
    }


    private ReferenceQueue<Resource> getReferenceQueue() {
        if (this.queue == null) {
            //初始化引用队列
            this.queue = new ReferenceQueue<>();
            cleanReferenceQueueThread = new Thread() {
                @Override
                public void run() {
                    //监听回收通知
                    while (!isShutDown) {
                        try {
                            //阻塞代码
                            ResourceWeakReference ref = (ResourceWeakReference) queue.remove();
                            //ref被回收，则移除活动资源
                            activeResources.remove(ref.key);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            cleanReferenceQueueThread.start();
        }
        return queue;
    }

    /**
     * 关闭监听线程
     */
    public void shutDown(){
        isShutDown = true;
        if(cleanReferenceQueueThread != null){
            //中断阻塞线程
            cleanReferenceQueueThread.interrupt();
            try {
                cleanReferenceQueueThread.join(TimeUnit.SECONDS.toMillis(5));
                if(cleanReferenceQueueThread.isAlive()){
                    throw new RuntimeException("Failed to join in time");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static final class ResourceWeakReference extends WeakReference<Resource> {

        private final Key key;

        public ResourceWeakReference(Key key, Resource reference, ReferenceQueue<? super Resource> q) {
            super(reference, q);
            this.key = key;
        }
    }
}
