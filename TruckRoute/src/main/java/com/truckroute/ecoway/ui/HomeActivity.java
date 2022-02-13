package com.truckroute.ecoway.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.truckroute.ecoway.R;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AppBottomSheetDialog.Listener {

    private static final String SELECTED_ITEM_ID = "SELECTED_ITEM_ID";
    private final Handler mDrawerHandler = new Handler();
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private int mSelectedId;
    private Toolbar mToolbar;
    private Fragment navFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(mToolbar);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        assert mNavigationView != null;
        mNavigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, mToolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                super.onDrawerSlide(drawerView, 0);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSelectedId = mNavigationView.getMenu().getItem(prefs.getInt("default_view", 0)).getItemId();
        mSelectedId = savedInstanceState == null ? mSelectedId : savedInstanceState.getInt(SELECTED_ITEM_ID);
        mNavigationView.getMenu().findItem(mSelectedId).setChecked(true);

        if (savedInstanceState == null) {
            mDrawerHandler.removeCallbacksAndMessages(null);
            mDrawerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigate(mSelectedId);
                }
            }, 250);

            boolean openDrawer = prefs.getBoolean("open_drawer", false);

            if (openDrawer)
                mDrawerLayout.openDrawer(GravityCompat.START);
            else
                mDrawerLayout.closeDrawers();
        }
    }

    private void navigate(final int itemId) {
        switch (itemId) {
            case R.id.nav_home:
                setTitle(R.string.menu_home);
                navFragment = new HomeFragment();
                break;
            case R.id.nav_leader_board:
                setTitle(R.string.menu_leader_board);
                navFragment = new LeaderBoardFragment();
                break;
            case R.id.nav_about_us:
                setTitle(R.string.nav_about);
                navFragment = new AboutUsFragment();
                break;
            case R.id.nav_feedback:
                setTitle(R.string.menu_feedback);
                navFragment = WebViewFragment.newInstance(Constants.FEEDBACK_FORM_URL);
                break;
            case R.id.nav_permit:
                setTitle(R.string.menu_option_permit);
                navFragment = WebViewFragment.newInstance(Constants.PERMIT_URL);
                break;
            case R.id.nav_food_bank:
                setTitle(R.string.menu_option_food_bank);
                navFragment = WebViewFragment.newInstance(Constants.FOOD_BANK_URL);
                break;
            case R.id.nav_logout:
                setTitle(R.string.menu_logout);
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
        }
        if (navFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            try {
                transaction.replace(R.id.content_frame, navFragment).commit();
            } catch (IllegalStateException ignored) {
            }
        }
    }

    public int dp(float value) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;

        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        mSelectedId = menuItem.getItemId();
        mDrawerHandler.removeCallbacksAndMessages(null);
        mDrawerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigate(mSelectedId);
            }
        }, 250);
        mDrawerLayout.closeDrawers();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM_ID, mSelectedId);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFilterSelected(String listValue) {
        if (navFragment instanceof HomeFragment){
            ((HomeFragment) navFragment).onFilterSelected(listValue);
        }
    }
}

