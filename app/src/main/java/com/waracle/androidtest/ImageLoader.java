package com.waracle.androidtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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

    private ImageLoader() { }

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
        if (TextUtils.isEmpty(url)) {
            throw new InvalidParameterException("URL is empty!");
        }

        // Can you think of a way to improve loading of bitmaps
        // that have already been loaded previously??
        // todo LruCache

        mExecutor.submit(new ImageLoaderTask(url, bitmap -> setImageView(imageView, bitmap)));
    }

    private static byte[] loadImageData(String url) {
        byte[] imageBytes = null;
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();

            // Can you think of a way to make the entire
            // HTTP more efficient using HTTP headers??

            int responseCode = connection.getResponseCode();
            switch (responseCode) {
                case HttpURLConnection.HTTP_OK:
                    inputStream = connection.getInputStream();
                    return StreamUtils.extractBytes(inputStream);
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
                return;
            }
            final Bitmap bitmap = convertToBitmap(imageBytes);
            handler.post(() -> mCallback.imageLoaded(bitmap));
        }
    }
}
