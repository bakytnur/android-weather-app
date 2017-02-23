package bakha.currentweather.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class RequestHelperSingleton {
    private static final Object REQUEST_TAG = "weather";
    private static RequestHelperSingleton instance;
    private static Context context;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;

    private RequestHelperSingleton(Context context) {
        RequestHelperSingleton.context = context;
        requestQueue = getRequestQueue();

        imageLoader = new ImageLoader(requestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized RequestHelperSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new RequestHelperSingleton(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        // Set the tag on the request.
        req.setTag(REQUEST_TAG);
        getRequestQueue().add(req);
    }

    public <T> void cancelAll() {
        if (requestQueue != null) {
            requestQueue.cancelAll(REQUEST_TAG);
        }
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }
}