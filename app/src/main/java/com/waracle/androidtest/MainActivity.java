package com.waracle.androidtest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int CAKE_LOADER_ID = 1;
    private static final String CAKE_LIST_KEY = "cake_list_key";

    private RecyclerView mRecyclerView;
    private CakeAdapter mAdapter;
    private ArrayList<Cake> mData;
    private Snackbar mSnackbar;
    private CakeLoader mCakeLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        mRecyclerView = findViewById(R.id.rv_cakes);
        mRecyclerView.setLayoutManager(getDeviceLayoutManager(isTablet));
        mAdapter = new CakeAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);

        mCakeLoader = new CakeLoader();

        if (savedInstanceState == null || mAdapter.getItemCount() == 0) {
            checkInternet();
        } else {
            mData = savedInstanceState.getParcelableArrayList(CAKE_LIST_KEY);
            mAdapter.setItems(mData);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(CAKE_LIST_KEY, mData);
    }

    private LinearLayoutManager getDeviceLayoutManager(boolean isTablet) {
        if (isTablet) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
            int numColumns = (int) (dpWidth / 300);
            return new GridLayoutManager(this, numColumns);
        } else {
            return new LinearLayoutManager(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            checkInternet();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkInternet() {
        if (NetworkUtils.isOnline(this)) {
            getSupportLoaderManager().restartLoader(CAKE_LOADER_ID, null, mCakeLoader);
            if (mSnackbar != null) {
                mSnackbar.dismiss();
                mSnackbar = null;
            }
        } else {
            showOfflineSnack();
        }
    }

    private void showOfflineSnack() {
        mSnackbar = Snackbar.make(findViewById(android.R.id.content),
                R.string.check_internet, Snackbar.LENGTH_INDEFINITE);
        mSnackbar.setAction(R.string.retry, v -> checkInternet());
        mSnackbar.show();
    }

    private void initLoaders() {
        getSupportLoaderManager().destroyLoader(CAKE_LOADER_ID);
        getSupportLoaderManager().initLoader(CAKE_LOADER_ID, null, mCakeLoader);
    }


    class CakeLoader implements LoaderManager.LoaderCallbacks<ArrayList<Cake>> {

        @SuppressLint("StaticFieldLeak")
        @NonNull
        @Override
        public Loader<ArrayList<Cake>> onCreateLoader(int id, @Nullable Bundle args) {
            return new AsyncTaskLoader<ArrayList<Cake>>(MainActivity.this) {

                @Override
                protected void onStartLoading() {
                    if (mData != null && mData.size() > 0) {
                        deliverResult(mData);
                    } else {
                        forceLoad();
                    }
                }

                @Nullable
                @Override
                public ArrayList<Cake> loadInBackground() {
                    // Load data from net.
                    try {
                        URL url = NetworkUtils.getUrl();
                        String response = NetworkUtils.getResponseFromHttpUrl(url);
                        return JsonUtils.parseCakeList(response);
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    return null;
                }

                @Override
                public void deliverResult(@Nullable ArrayList<Cake> data) {
                    mData = data;
                    super.deliverResult(data);
                }
            };
        }

        @Override
        public void onLoadFinished(@NonNull Loader<ArrayList<Cake>> loader, ArrayList<Cake> data) {
            mAdapter.setItems(data);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<ArrayList<Cake>> loader) {
            mData = null;
        }
    }
}
