package com.tnmlicitacoes.app.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.adapter.MyBiddingsAdapter;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.tasks.SendEmailTask;
import com.tnmlicitacoes.app.ui.activity.WebviewActivity;
import com.tnmlicitacoes.app.ui.base.BaseFragment;
import com.tnmlicitacoes.app.utils.Utils;

import java.util.HashMap;

import okhttp3.HttpUrl;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class BiddingsTabFragment extends BaseFragment implements OnClickListenerRecyclerView {

    private static final String TAG = "BiddingsTabFragment";

    private TextView mNoItemsView;

    private RecyclerView mRecyclerView;

    private ProgressBar mProgressBar;

    private MyBiddingsAdapter mMyBiddingsAdapter;

    private boolean mIsLoading = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_biddings, container, false);
        initViews(view);
        initAdapters();
        return view;
    }

    private void initViews(View v) {
        mRecyclerView = (RecyclerView) v.findViewById(R.id.myBiddingsRecView);
        mProgressBar = (ProgressBar) v.findViewById(R.id.myBiddingsProgressBar);
        mNoItemsView = (TextView) v.findViewById(R.id.noItemsView);

        mProgressBar.setVisibility(mIsLoading ? View.VISIBLE : View.GONE);
        mRecyclerView.setVisibility(mIsLoading ? View.GONE : View.VISIBLE);
    }

    private void initAdapters() {
        mMyBiddingsAdapter = new MyBiddingsAdapter(getContext());
        mMyBiddingsAdapter.setOnClickListener(this);
    }

    private void setupList() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mMyBiddingsAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mIsLoading = true;
        setupList();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void OnClickListener(View v, int position) {
    }

    private void showOnline(String pageTitle, String noticeLink) {
        if(HttpUrl.parse(noticeLink) != null) {
            Intent webviewIntent = new Intent(getContext(), WebviewActivity.class);
            webviewIntent.putExtra(WebviewActivity.PAGE_TITLE, pageTitle);
            webviewIntent.putExtra(WebviewActivity.PAGE_LINK, noticeLink);
            webviewIntent.putExtra(WebviewActivity.IS_PDF_FILE, noticeLink.endsWith(".pdf"));
            startActivity(webviewIntent);
            overridePendingTransition();
        } else {
            Toast.makeText(getContext(), getString(R.string.web_view_unavailable), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmailTask(String l, String f, String e, String a) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Utils.FILE_LINK_PARAM, l);
        map.put(Utils.FILE_NAME_PARAM, f);
        map.put(Utils.USER_EMAIL_PARAM, e);
        map.put(Utils.AGENCY_NAME_PARAM, a);

        SendEmailTask sendTask = new SendEmailTask(getActivity(), map);
        sendTask.execute();
    }

    private void overridePendingTransition() {
        getActivity().overridePendingTransition(R.anim.activity_fade_enter, R.anim.activity_fade_exit);
    }
}
