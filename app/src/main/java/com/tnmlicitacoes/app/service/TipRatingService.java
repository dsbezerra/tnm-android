package com.tnmlicitacoes.app.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.tnmlicitacoes.app.R;

public class TipRatingService extends IntentService {

    private static final String TAG = "TipRatingService";
    
    public TipRatingService() {
        super(TAG);
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle b = intent.getExtras();
        if(b != null) {

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(b.getInt("NOTIFICATION_ID"));

            String tipContent = b.getString("TIP_CONTENT");
            boolean liked = b.getBoolean("LIKE");
            if(liked) {
                //AnalyticsUtils.fireEvent(getApplicationContext(), getString(R.string.tip), getString(R.string.like_text), tipContent);
            } else {
                //AnalyticsUtils.fireEvent(getApplicationContext(), getString(R.string.tip), getString(R.string.dislike_text), tipContent);
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), getString(R.string.thanks_feedback), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
