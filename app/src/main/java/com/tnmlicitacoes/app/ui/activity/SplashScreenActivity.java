package com.tnmlicitacoes.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.tnmlicitacoes.app.verifynumber.VerifyNumberActivity;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "SplashScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, VerifyNumberActivity.class);
        startActivity(intent);
        finish();
    }
}
