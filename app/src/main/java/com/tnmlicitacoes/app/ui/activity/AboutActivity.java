package com.tnmlicitacoes.app.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.utils.Utils;

/**
 *  AboutActivity
 *  Activity that shows info about the app
 */

public class AboutActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setupToolbar(getString(R.string.title_activity_about));

        ImageView websiteIv = (ImageView) findViewById(R.id.websiteIv);
        ImageView emailIv = (ImageView) findViewById(R.id.emailIv);
        ImageView facebookIv = (ImageView) findViewById(R.id.facebookIv);

        if(websiteIv != null) {
            websiteIv.setOnClickListener(this);
        }
        if(emailIv != null) {
            emailIv.setOnClickListener(this);
        }
        if(facebookIv != null) {
            facebookIv.setOnClickListener(this);
        }

    }

    @Override
    protected  void setupToolbar(String title) {
        super.setupToolbar(title);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {

        final int viewId = v.getId();
        if(viewId == R.id.websiteIv) {
            String uri = "http://tnmlicitacoes.com";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        } else if (viewId == R.id.emailIv) {
            Intent i = Utils.sendContactEmail("", "");
            try {
                startActivity(Intent.createChooser(i, getString(R.string.send_email_contact)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, getString(R.string.no_email_clients_installed), Toast.LENGTH_SHORT).show();
            }
        } else if (viewId == R.id.facebookIv) {
            String uri = "https://facebook.com/tnmlicitacoes";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        }
    }
}
