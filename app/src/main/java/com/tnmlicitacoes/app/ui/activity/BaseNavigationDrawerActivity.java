package com.tnmlicitacoes.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.utils.SettingsUtils;
import com.tnmlicitacoes.app.utils.Utils;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

/**
 * BaseNavigationDrawerActivity
 * Base activity class for all other views with a navigation drawer
 */

public abstract class BaseNavigationDrawerActivity extends BaseActivity {

    private static final String TAG = "BaseNavigationDrawerActivity";

    private static final int NAVDRAWER_CLOSE_DELAY = 400;

    protected static final int NAVDRAWER_ITEM_NONE = -1;

    protected static final int NAVDRAWER_ITEM_ACCOUNT_HEADER = 0;

    protected static final int NAVDRAWER_ITEM_HOME = 1;

    protected static final int NAVDRAWER_ITEM_MY_BIDDINGS = 2;

    protected static final int NAVDRAWER_ITEM_DIVIDER = 3;

    protected static final int NAVDRAWER_ITEM_ACCOUNT = 4;

    protected static final int NAVDRAWER_ITEM_SETTINGS = 5;

    protected static final int NAVDRAWER_ITEM_CONTACT = 6;

    protected static final int NAVDRAWER_ITEM_ABOUT = 7;

    protected Toolbar mToolbar;

    protected Drawer mDrawer;

    protected TextView mSubscriptionText;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        }
    }

    private void executeEmailIntent(Intent i) {
        try {
            startActivity(Intent.createChooser(i, getString(R.string.send_email_contact)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, getString(R.string.no_email_clients_installed), Toast.LENGTH_SHORT).show();
        }
    }

    public void setupDrawer(Bundle savedInstance) {

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if(mToolbar == null) {
            return;
        }

        int selfItem = getSelfDrawerItem();

        View headerView = View
                .inflate(this,
                        R.layout.drawer_header,
                        null);

        mSubscriptionText = (TextView) headerView.findViewById(R.id.subscriptionName);
        TextView phoneText = (TextView) headerView.findViewById(R.id.phoneText);
        RelativeLayout headerLayout = (RelativeLayout) headerView.findViewById(R.id.headerLayout);

        String plan = SettingsUtils.getBillingSubName(this);
        mSubscriptionText.setText(getString(R.string.header_sub_text, plan.equals(SettingsUtils.STRING_DEFAULT) ? "Trial" : plan));

        String phone = SettingsUtils.getUserPhoneFormattedNumber(this);
        phoneText.setText(phone.equals(SettingsUtils.STRING_DEFAULT) ? "" : phone);

        headerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.closeDrawer();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(BaseNavigationDrawerActivity.this, MySubscriptionActivity.class));
                        //AnalyticsUtils.fireEvent(getApplicationContext(), "Meu plano", "Ver detalhes");
                        overridePendingTransition(R.anim.activity_fade_enter, R.anim.activity_fade_exit);
                    }
                }, NAVDRAWER_CLOSE_DELAY);

            }
        });

        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withHeader(headerView)
                .withHasStableIds(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(getString(R.string.drawer_home)).withIcon(R.drawable.ic_home).withIconTintingEnabled(true).withIdentifier(NAVDRAWER_ITEM_HOME),
                        new PrimaryDrawerItem().withName(getString(R.string.drawer_notices)).withIcon(R.drawable.ic_folder).withIconTintingEnabled(true).withIdentifier(NAVDRAWER_ITEM_MY_BIDDINGS),
                        new DividerDrawerItem().withIdentifier(NAVDRAWER_ITEM_DIVIDER).withSelectable(false),
                        new PrimaryDrawerItem().withName(getString(R.string.drawer_account)).withIcon(R.drawable.ic_account).withIconTintingEnabled(true).withIdentifier(NAVDRAWER_ITEM_ACCOUNT),
                        new PrimaryDrawerItem().withName(getString(R.string.drawer_settings)).withIcon(R.drawable.ic_settings).withIconTintingEnabled(true).withIdentifier(NAVDRAWER_ITEM_SETTINGS),
                        new PrimaryDrawerItem().withName(getString(R.string.drawer_contact)).withIcon(R.drawable.ic_mail).withIconTintingEnabled(true).withIdentifier(NAVDRAWER_ITEM_CONTACT),
                        new PrimaryDrawerItem().withName(getString(R.string.drawer_about)).withIcon(R.drawable.ic_error_outline).withIconTintingEnabled(true).withIdentifier(NAVDRAWER_ITEM_ABOUT)
                )
                .withDrawerGravity(Gravity.START)
                .withActionBarDrawerToggleAnimated(true)
                .withSelectedItem(selfItem)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, final IDrawerItem drawerItem) {

                        if (drawerItem != null) {
                            Intent intent = null;

                            if (drawerItem.getIdentifier() == NAVDRAWER_ITEM_HOME) {

                                //if (BaseNavigationDrawerActivity.this instanceof MainActivity) {
                                //    return false;
                                //}
                                intent = new Intent(BaseNavigationDrawerActivity.this, MainActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                            } else if (drawerItem.getIdentifier() == NAVDRAWER_ITEM_MY_BIDDINGS) {

                                //if (BaseNavigationDrawerActivity.this instanceof MyBiddingsActivity) {
                                //    return false;
                                //}
                                intent = new Intent(BaseNavigationDrawerActivity.this, MyBiddingsActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                            } else if(drawerItem.getIdentifier() == NAVDRAWER_ITEM_ACCOUNT) {
                                intent = new Intent(BaseNavigationDrawerActivity.this, MySubscriptionActivity.class);

                            } else if (drawerItem.getIdentifier() == NAVDRAWER_ITEM_SETTINGS) {
                                intent = new Intent(BaseNavigationDrawerActivity.this, SettingsActivity.class);

                            } else if (drawerItem.getIdentifier() == NAVDRAWER_ITEM_CONTACT) {
                                intent = Utils.sendContactEmail("[Contato]", "");

                            } else if (drawerItem.getIdentifier() == NAVDRAWER_ITEM_ABOUT) {
                                intent = new Intent(BaseNavigationDrawerActivity.this, AboutActivity.class);
                            }

                            if (intent != null) {
                                final Intent newIntent = intent;
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (drawerItem.getIdentifier() == NAVDRAWER_ITEM_CONTACT)
                                            executeEmailIntent(newIntent);
                                        else
                                            startActivity(newIntent);
                                        overridePendingTransition(R.anim.activity_fade_enter, R.anim.activity_fade_exit);
                                    }
                                }, NAVDRAWER_CLOSE_DELAY);
                            }
                        }

                        return false;
                    }
                })
                .build();

        if(savedInstance == null) {
            mDrawer.setSelection(NAVDRAWER_ITEM_HOME, false);
        }

        if(BuildConfig.DEBUG)
            LOG_DEBUG(TAG, "finishedSetupDrawer");
    }

    protected void addPrimaryItem(int stringResID, int iconID) {
        mDrawer.addItem(new PrimaryDrawerItem().withName(getString(stringResID)).withIcon(iconID));
    }

    protected void addDividerItem() {
        mDrawer.addItem(new DividerDrawerItem());
    }

    protected int getSelfDrawerItem(){
        return NAVDRAWER_ITEM_NONE;
    }
}
