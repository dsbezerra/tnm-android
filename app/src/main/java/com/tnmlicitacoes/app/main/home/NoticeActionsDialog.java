package com.tnmlicitacoes.app.main.home;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.Toast;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.interfaces.OnNoticeActionsDialogListener;

public class NoticeActionsDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    private OnNoticeActionsDialogListener mListener;

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_notice_actions_sheet, null);
        dialog.setContentView(contentView);

        View seeDetails  = contentView.findViewById(R.id.seeDetails);
        View viewOnline  = contentView.findViewById(R.id.viewOnline);
        View sendToEmail = contentView.findViewById(R.id.sendToEmail);
        View download    = contentView.findViewById(R.id.download);
        View close       = contentView.findViewById(R.id.close);

        seeDetails.setOnClickListener(this);
        viewOnline.setOnClickListener(this);
        sendToEmail.setOnClickListener(this);
        download.setOnClickListener(this);
        close.setOnClickListener(this);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior ) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }

    @Override
    public void onClick(View view) {

        dismiss();

        if (mListener == null) {
            return;
        }

        switch (view.getId()) {
            case R.id.seeDetails:
                mListener.onSeeDetailsClicked();
                break;
            case R.id.viewOnline:

                mListener.onViewOnlineClicked();
                break;
            case R.id.sendToEmail:
                mListener.onSendToEmailClicked();
                break;
            case R.id.download:
                mListener.onDownloadClicked();
                break;
            case R.id.close:
                break;

        }
    }

    public void setListener(OnNoticeActionsDialogListener listener) {
        this.mListener = listener;
    }
}
