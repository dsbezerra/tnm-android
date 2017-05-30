package com.tnmlicitacoes.app;

import android.app.Application;
import android.content.Context;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.cache.normalized.CacheKey;
import com.apollographql.apollo.cache.normalized.CacheKeyResolver;
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy;
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper;
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory;
import com.crashlytics.android.Crashlytics;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.Utils;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import okhttp3.Authenticator;
import okhttp3.CertificatePinner;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class TNMApplication extends Application {

    private static final String TAG = "TNMApplication";

    private static final String BASE_URL = "https://tnm-graph.herokuapp.com/graphql";
    private static final String SQL_CACHE_NAME = "tnmdb";
    private ApolloClient mApolloClient;

    public static boolean IsRefreshingToken;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        // We need to initialize apollo client with access token, but in case the access token is
        // expired, we need to use the refresh token so it can successfully refresh the access token
        // for us.
        String authToken = SettingsUtils.getCurrentAccessToken(this);
        if (shouldRefreshToken(this)) {
            authToken = SettingsUtils.getRefreshToken(this);
        }
        initApolloClient(authToken);
    }

    /**
     * Gets the ApolloClient instance
     * @return the apollo client already initialized
     */
    public ApolloClient getApolloClient() {
        return mApolloClient;
    }

    /**
     * Initializes an ApolloClient instance with the given auth token
     * @param authToken the supplier authentication token
     *                  it can be either the refreshToken or accessToken
     *                  refreshToken is used when we need to refresh the accessToken
     *                  accessToken is used when we need to fetch data from the api
     */
    public void initApolloClient(final String authToken) {

        CertificatePinner pinner = new CertificatePinner.Builder()
                .add("tnm-graph.herokuapp.com", "sha256/Vuy2zjFSPqF5Hz18k88DpUViKGbABaF3vZx5Raghplc=")
                .add("tnm-graph.herokuapp.com", "sha256/k2v657xBsOVe1PQRwOsHsw3bsGT2VzIqz5K+59sNQws=")
                .add("tnm-graph.herokuapp.com", "sha256/k2v657xBsOVe1PQRwOsHsw3bsGT2VzIqz5K+59sNQws=")
                .build();

        Interceptor interceptor = null;
        if (authToken != null) {
            interceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    String bearer = "Bearer " + authToken;
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder().method(original.method(), original.body());
                    builder.header("Authorization", bearer);
                    return chain.proceed(builder.build());
                }
            };
        }

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .certificatePinner(pinner)
                .build();

        ApolloSqlHelper apolloSqlHelper = new ApolloSqlHelper(this, SQL_CACHE_NAME);
        NormalizedCacheFactory normalizedCacheFactory = new LruNormalizedCacheFactory(EvictionPolicy.NO_EVICTION,
                new SqlNormalizedCacheFactory(apolloSqlHelper));

        CacheKeyResolver<Map<String, Object>> cacheKeyResolver = new CacheKeyResolver<Map<String, Object>>() {
            @Nonnull
            @Override
            public CacheKey resolve(@Nonnull Map<String, Object> objectSource) {
                String id = (String) objectSource.get("id");
                if (id == null || id.isEmpty()) {
                    return CacheKey.NO_KEY;
                }
                return CacheKey.from(id);
            }
        };

        mApolloClient = ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .normalizedCache(normalizedCacheFactory, cacheKeyResolver)
                .build();
    }

    /**
     * Checks if we should refresh or not the access token
     * @param context Context of the application
     * @return true if we should and false if not
     */
    public static boolean shouldRefreshToken(Context context) {
        long lastRefreshTimestamp = SettingsUtils.getLastAccessTokenRefreshTimestamp(context);
        long currentTimestamp = new Date().getTime();
        return currentTimestamp - lastRefreshTimestamp >= Utils.HOUR_IN_MILLIS;
    }
}
