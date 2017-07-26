package com.tnmlicitacoes.app.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.evernote.android.state.StateSaver;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public abstract class BaseFragment extends Fragment {

    /* The parent activity */
    protected Activity mParentActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LOG_DEBUG(getLogTag(), "onAttach");
        mParentActivity = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG_DEBUG(getLogTag(), "onCreate");
        StateSaver.restoreInstanceState(this, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LOG_DEBUG(getLogTag(), "onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LOG_DEBUG(getLogTag(), "onActivityCreated");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LOG_DEBUG(getLogTag(), "onSaveInstanceState");
        StateSaver.saveInstanceState(this, outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        LOG_DEBUG(getLogTag(), "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        LOG_DEBUG(getLogTag(), "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        LOG_DEBUG(getLogTag(), "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        LOG_DEBUG(getLogTag(), "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LOG_DEBUG(getLogTag(), "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LOG_DEBUG(getLogTag(), "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LOG_DEBUG(getLogTag(), "onDetach");
        mParentActivity = null;
    }

    public abstract String getLogTag();
}
