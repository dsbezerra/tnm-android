package com.tnmlicitacoes.app.ui.activity;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.ui.base.BaseActivity;

public class WebviewActivity extends BaseActivity {

    private static final String GOOGLE_DOCS_VIEW_DOC_URI = "http://docs.google.com/gview?embedded=true&url=";

    public static final String PAGE_TITLE = "page_title";

    public static final String PAGE_LINK = "page_link";

    public static final String IS_PDF_FILE = "is_pdf_file";

    private WebView mWebView;

    private TextView mPageTitle;

    private TextView mPageLink;

    private ImageButton mImageButton;

    private boolean mIsPdfFile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        initViews();
        initListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        handleIntent();
    }

    private void initViews() {
        mWebView = (WebView) findViewById(R.id.webView);
        mPageTitle = (TextView)findViewById(R.id.pageTitle);
        mPageLink = (TextView)findViewById(R.id.pageLink);
        mImageButton = (ImageButton) findViewById(R.id.closeButton);

        mWebView.setInitialScale(100);

        WebSettings settings = mWebView.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            settings.setDisplayZoomControls(false);
        }
    }

    private void initListeners() {
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {

            private ProgressBar progressBar = (ProgressBar) findViewById(R.id.loadProgressBar);

            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {

            private ProgressBar progressBar = (ProgressBar) findViewById(R.id.loadProgressBar);

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(ProgressBar.VISIBLE);
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(WebviewActivity.this, description, Toast.LENGTH_SHORT).show();
            }

            public void onPageFinished(WebView view, String url) {
                try {
                    progressBar.setVisibility(ProgressBar.GONE);
                    if(!mIsPdfFile) {
                        if(mPageTitle != null && mPageLink != null) {
                            mPageTitle.setText(view.getTitle());
                            mPageLink.setText(url);
                        }
                    }

                    if(view.getTitle().contains("about:blank")) {
                        finish();
                    }

                } catch (NullPointerException e) {
                    e.printStackTrace();
                    //AnalyticsUtils.fireEvent(WebviewActivity.this, "Falha", "NullPointerException", WebviewActivity.class.getSimpleName() +  ": onPageFinished()");
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    public void handleIntent() {

        Bundle b = getIntent().getExtras();
        if(b == null) {
            Toast.makeText(this, getString(R.string.webview_load_error), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String pageTitle = b.getString(PAGE_TITLE);
        String pageLink = b.getString(PAGE_LINK);

        mIsPdfFile = b.getBoolean(IS_PDF_FILE);
        if(mIsPdfFile) {
            pageLink = GOOGLE_DOCS_VIEW_DOC_URI + pageLink;
        }

        mPageTitle.setText(pageTitle);
        mPageLink.setText(mIsPdfFile ? "Visualizando edital" : pageLink);
        mWebView.loadUrl(pageLink);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
    }
}
