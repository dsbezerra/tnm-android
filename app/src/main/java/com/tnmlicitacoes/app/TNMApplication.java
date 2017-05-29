package com.tnmlicitacoes.app;

import android.app.Application;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.cache.normalized.CacheKey;
import com.apollographql.apollo.cache.normalized.CacheKeyResolver;
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy;
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper;
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory;
import com.crashlytics.android.Crashlytics;

import java.util.Map;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;

public class TNMApplication extends Application {

    private static final String BASE_URL = "https://tnm-graph.herokuapp.com/graphql";
    private static final String SQL_CACHE_NAME = "tnmdb";
    private ApolloClient mApolloClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .certificatePinner(new CertificatePinner.Builder()
                        .add("tnm-graph.herokuapp.com", "sha256/Vuy2zjFSPqF5Hz18k88DpUViKGbABaF3vZx5Raghplc=")
                        .add("tnm-graph.herokuapp.com", "sha256/k2v657xBsOVe1PQRwOsHsw3bsGT2VzIqz5K+59sNQws=")
                        .add("tnm-graph.herokuapp.com", "sha256/k2v657xBsOVe1PQRwOsHsw3bsGT2VzIqz5K+59sNQws=")
                        .build())
                .build();

        ApolloSqlHelper apolloSqlHelper = new ApolloSqlHelper(this, SQL_CACHE_NAME);
        NormalizedCacheFactory normalizedCacheFactory = new LruNormalizedCacheFactory(EvictionPolicy.NO_EVICTION,
                new SqlNormalizedCacheFactory(apolloSqlHelper));

        CacheKeyResolver<Map<String, Object>> cacheKeyResolver = new CacheKeyResolver<Map<String, Object>>() {
            @Override
            public CacheKey resolve(Map<String, Object> objectSource) {
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

    public ApolloClient getApolloClient() {
        return mApolloClient;
    }
}
