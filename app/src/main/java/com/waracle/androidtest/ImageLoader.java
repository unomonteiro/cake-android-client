package com.waracle.androidtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Riad on 20/05/2015.
 */
public class ImageLoader {
    private static ImageLoader sInstance =  new ImageLoader();
    private final LruCache<String, Bitmap> mBitmapCache;

    private ImageLoader() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        mBitmapCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size in kilobytes
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static ImageLoader getInstance() {
        return sInstance;
    }

    private static final String TAG = ImageLoader.class.getSimpleName();

    private final ExecutorService mExecutor = Executors.newCachedThreadPool();

    /**
     * Simple function for loading a bitmap image from the web
     *
     * @param url       image url
     * @param imageView view to set image too.
     */
    public void load(String url, final ImageView imageView) {

        // Can you think of a way to improve loading of bitmaps
        // that have already been loaded previously??
        // https://developer.android.com/topic/performance/graphics/cache-bitmap
        Bitmap cachedBitmap = mBitmapCache.get(url);
        if (cachedBitmap == null) {
            mExecutor.submit(new ImageLoaderTask(url, new ImageLoaderCallback() {
                @Override
                public void imageLoaded(Bitmap bitmap) {
                    mBitmapCache.put(url, bitmap);
                    setImageView(imageView, bitmap);
                }

                @Override
                public void imageFailed() {
                    imageView.setImageResource(R.drawable.ic_cake);
                }
            }));
        } else {
            setImageView(imageView, cachedBitmap);
        }
    }

    private static byte[] loadImageData(String url) {
        byte[] imageBytes = null;
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();

            // Can you think of a way to make the entire
            // HTTP more efficient using HTTP headers??
            // more info at https://developer.android.com/training/efficient-downloads/redundant_redundant
            connection.setUseCaches(true);

            int responseCode = connection.getResponseCode();
            switch (responseCode) {
                case HttpURLConnection.HTTP_OK:
                    inputStream = connection.getInputStream();
                    return StreamUtils.extractBytes(inputStream);
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case HttpURLConnection.HTTP_MOVED_PERM:
                    return loadImageData(connection.getHeaderField("Location"));
                default:
                    return null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            StreamUtils.close(inputStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
        return imageBytes;
    }

    private static Bitmap convertToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    private static void setImageView(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    private interface ImageLoaderCallback {
        void imageLoaded(Bitmap bitmap);
        void imageFailed();
    }

    private static class ImageLoaderTask implements Runnable {
        private final String mUrl;
        private final ImageLoaderCallback mCallback;
        Handler handler = new Handler(Looper.getMainLooper());

        ImageLoaderTask(String url, ImageLoaderCallback callback) {
            mUrl = url;
            mCallback = callback;
        }

        @Override
        public void run() {
            byte[] imageBytes = loadImageData(mUrl);
            if (imageBytes == null) {
                handler.post(() -> mCallback.imageFailed());
            } else {
                Bitmap bitmap = convertToBitmap(imageBytes);
                handler.post(() -> mCallback.imageLoaded(bitmap));
            }
        }
    }
}
