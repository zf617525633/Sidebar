
package co.yoyu.sidebar.cache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

public class LocalImageCache {

    private static final int HARD_CACHE_CAPACITY = 50;

    private  static LocalImageCache sLocalImageCache;;
   
    private String cacheFilePath;
    
    private static Context mContext;

    // Hard cache, with a fixed maximum capacity and a life duration
    private final HashMap<String, Bitmap> sHardBitmapCache = new LinkedHashMap<String, Bitmap>(
            HARD_CACHE_CAPACITY / 2, 0.75f, true) {
        private static final long serialVersionUID = -7190622541619388252L;

        @Override
        protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                // Entries push-out of hard reference cache are transferred to
                // soft reference cache
                sWeakBitmapCache.put(eldest.getKey(), new WeakReference<Bitmap>(eldest.getValue()));
                return true;
            } else {
                return false;
            }
        }
    };

    // Soft cache for bitmap kicked out of hard cache
    private final ConcurrentHashMap<String, WeakReference<Bitmap>> sWeakBitmapCache = new ConcurrentHashMap<String, WeakReference<Bitmap>>(
            HARD_CACHE_CAPACITY / 2);

    private LocalImageCache() {
        cacheFilePath = getImageCacheDictory();
    }

    public static LocalImageCache getInstance(Context context) {
        mContext = context;
        if(sLocalImageCache==null)
           sLocalImageCache = new LocalImageCache();
        return sLocalImageCache;
    }

    public void clear() {
        sHardBitmapCache.clear();
        sWeakBitmapCache.clear();
        deleteCacheFile();
    }

    private void deleteCacheFile() {
        if (cacheFilePath != null) {
            File file = new File(cacheFilePath);
            if (file.exists()) {
                deleteAll(file);
            }
        }
    }

    /**
     * TODO 删掉所有的文件
     * 
     * @param File file 文件
     * @author zhangf
     * @date 2013-5-4
     * @return void
     */
    private void deleteAll(File file) {
        if (file.isFile() || file.list().length == 0) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteAll(files[i]);
                files[i].delete();
            }
            if (file.exists()) // 如果文件本身就是目录 ，就要删除目录
                file.delete();
        }
    }

    /**
     * @param key The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    public Bitmap getBitmapFromCache(String key) {
        // First try the hard reference cache
        synchronized (sHardBitmapCache) {
            final Bitmap bitmap = sHardBitmapCache.get(key);
            if (bitmap != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                sHardBitmapCache.remove(key);
                sHardBitmapCache.put(key, bitmap);
                return bitmap;
            }
        }

        // Then try the soft reference cache
        WeakReference<Bitmap> bitmapReference = sWeakBitmapCache.get(key);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                // Bitmap found in soft cache
                return bitmap;
            } else {
                // Soft reference has been Garbage Collected
                sWeakBitmapCache.remove(key);
            }
        }

        return null;
    }

    /**
     * TODO 根据key获取内存中的bitmap
     * 
     * @author zhangf
     * @date 2013-5-4
     * @return Bitmap
     */
    public Bitmap get(String key) {
        Bitmap bitmap = null;
        if (sHardBitmapCache.containsKey(key)) {
            bitmap = sHardBitmapCache.get(key);
        } else if (sWeakBitmapCache.containsKey(key)) {
            bitmap = sHardBitmapCache.get(key);
        }
        return bitmap;
    }

    /**
     * 判断图片是否存在首先判断内存中是否存在然后判断本地是否存在
     * 
     * @param key
     * @return boolean
     */
    public boolean isBitmapExit(String key) {
        boolean isExit = sHardBitmapCache.containsKey(key);
        if (false == isExit) {
            isExit = sWeakBitmapCache.containsKey(key);
            if (false == isExit) {
                isExit = isLocalHasBmp(key);
            }
        }
        return isExit;
    }

    /**
     * TODO 根据key判断本地有没有资源
     * 
     * @author zhangf
     * @date 2013-5-4
     * @return boolean
     */
    private boolean isLocalHasBmp(String key) {
        boolean isExit = true;
        String name = changeUrlToName(key);
        String filePath = cacheFilePath;
        if (filePath == null) {
            return false;
        }
        File file = new File(filePath, name);

        if (file.exists() && !file.isDirectory()) {
            isExit = cacheBmpToMemory(file, key);
        } else {
            isExit = false;
        }
        return isExit;
    }

    /**
     * TODO 讲本地图片存到内存中
     * 
     * @param File file 本地图片文件
     * @param String key 图片的key
     * @author zhangf
     * @date 2013-5-4
     * @return boolean
     */
    private boolean cacheBmpToMemory(File file, String key) {
        boolean sucessed = true;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            sucessed = false;
        }
        byte[] bs = getBytesFromStream(inputStream);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bs, 0, bs.length);
        if (bitmap == null) {
            return false;
        }
        this.put(key, bitmap, false);
        return sucessed;
    }

    /**
     * TODO 将输入流转化成byte数组
     * 
     * @author zhangf
     * @date 2013-5-4
     * @return byte[]
     */
    private byte[] getBytesFromStream(InputStream inputStream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[8 * 1024];
        int len = 0;
        while (len != -1) {
            try {
                len = inputStream.read(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (len != -1) {
                baos.write(b, 0, len);
            }
        }

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return baos.toByteArray();
    }


    /**
     * TODO 将url转化成名称
     * 
     * @author zhangf
     * @date 2013-5-4
     * @return String
     */
    private String changeUrlToName(String url) {
        String name = url.replaceAll(":", "_");
        name = name.replaceAll("//", "_");
        name = name.replaceAll("/", "_");
        name = name.replaceAll("=", "_");
        name = name.replaceAll(",", "_");
        name = name.replaceAll("&", "_");
        name = name.replace("png", "cache");
        return name;
    }
    
    
    
    public Bitmap getBitmapFromLocalOrMemory(String url) {
        String name = changeUrlToName(url);
        Bitmap bitmap = null;
        /*** 从内存中取 **/
        synchronized (sHardBitmapCache) {
            bitmap = sHardBitmapCache.get(name);
            if (bitmap != null) {
                sHardBitmapCache.remove(name);
                sHardBitmapCache.put(name, bitmap);
                return bitmap;
            }
        }
        WeakReference<Bitmap> bitmapReference = sWeakBitmapCache.get(name);
        if (bitmapReference != null) {
            bitmap = bitmapReference.get();
            if (bitmap != null) {
                return bitmap;
            } else {
                sWeakBitmapCache.remove(name);

            }
        }
        // update by zhangf getview取缓存会new File，统一在入口赋值路径
         String filePath = cacheFilePath ;
        if (filePath != null) {
            bitmap = loadImageFromLocal(filePath, name);
            sHardBitmapCache.put(name, bitmap);
        }
        // end
        return bitmap;

        // return null;
    }
    
    
    private Bitmap loadImageFromLocal(String cacheDirectory, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
            final File file = new File(cacheDirectory, fileName);
            if (file.exists()) {
                InputStream stream = null;
                try {
                    file.setLastModified(System.currentTimeMillis());
                    stream = new FileInputStream(file);
                    return BitmapFactory.decodeStream(stream, null, null);
                } catch (FileNotFoundException e) {
                } finally {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        return null;
    }
    

    /**
     * TODO 将bitmap缓存到文件中
     * 
     * @param String key 键
     * @param Bitmap value 图片
     * @author zhangf
     * @date 2013-5-4
     * @return Bitmap
     */
    public Bitmap put(String key, Bitmap value) {
        String filePath = cacheFilePath;
        if (filePath == null) {
            return null;
        }
        String name = changeUrlToName(key);
        File file = new File(filePath, name);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            sHardBitmapCache.put(key, value);
            return null;
        }
        value.compress(CompressFormat.PNG, 100, outputStream);
        try {
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (null != outputStream) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }
        return sHardBitmapCache.put(key, value);
    }

    /**
     * @param key
     * @param value
     * @param isCacheToLocal 是否缓存到本地
     * @return
     */
    public Bitmap put(String key, Bitmap value, boolean isCacheToLocal) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (isCacheToLocal) {
                return this.put(key, value);
            } else {
                return sHardBitmapCache.put(key, value);
            }
        } else {
            return sHardBitmapCache.put(key, value);
        }
    }
    
    public static String getImageCacheDictory() {
        if (isExternalStorageAvailable()) {
            String sdPath = getExternalDirctory();
            if (sdPath != null) {
                File file = new File(sdPath, "sidebar");
                if (!file.exists())
                    file.mkdirs();
                return file.getAbsolutePath();
            }
        } else {
            String dataPath = mContext.getFilesDir().getAbsolutePath();
            if (dataPath != null) {
                File file = new File(dataPath, "sidebar");
                if (!file.exists())
                    file.mkdirs();
                return file.getAbsolutePath();
            }
        }
        return null;
    }
    
    public static String getExternalDirctory() {
        if (isExternalStorageAvailable()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return null;
    }
    
    public static boolean isExternalStorageAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }
    

}
