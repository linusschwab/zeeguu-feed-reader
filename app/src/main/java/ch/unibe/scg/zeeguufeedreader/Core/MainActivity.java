package ch.unibe.scg.zeeguufeedreader.Core;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntryPagerAdapter;
import ch.unibe.scg.zeeguufeedreader.FeedEntryList.FeedEntryListFragment;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Category;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyAuthenticationFragment;
import ch.unibe.scg.zeeguufeedreader.Preferences.SettingsActivity;
import ch.unibe.zeeguulibrary.WebView.ZeeguuTranslationActionMode;
import ch.unibe.zeeguulibrary.WebView.ZeeguuWebViewFragment;
import ch.unibe.scg.zeeguufeedreader.FeedEntry.Compatibility.FeedEntryCompatibilityFragment;
import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntryFragment;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.FeedOverviewFragment;
import ch.unibe.scg.zeeguufeedreader.R;
import ch.unibe.zeeguulibrary.WebView.ZeeguuWebViewInterface;
import ch.unibe.zeeguulibrary.MyWords.MyWordsFragment;

/**
 *  Activity to display and switch between the fragments
 */
public class MainActivity extends BaseActivity implements
        FeedOverviewFragment.FeedOverviewCallbacks,
        FeedEntryListFragment.FeedEntryListCallbacks,
        FeedlyAuthenticationFragment.FeedlyAuthenticationCallbacks,
        MyWordsFragment.ZeeguuFragmentMyWordsCallbacks,
        ZeeguuWebViewInterface.ZeeguuWebViewInterfaceCallbacks,
        ZeeguuWebViewFragment.ZeeguuWebViewCallbacks {

    // Navigation
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private RelativeLayout statusbarBackground;
    private NavigationView navigationView;
    private SlidingUpPanelLayout panelLayout;
    private FrameLayout contentFrame;

    private ViewPager viewPager;
    private FeedEntryPagerAdapter pagerAdapter;
    private boolean isPanelLoaded = false;

    // Fragments
    private FeedOverviewFragment feedOverviewFragment;
    private FeedEntryListFragment feedEntryListFragment;
    private FeedEntryCompatibilityFragment feedEntryCompatibilityFragment;
    private FeedlyAuthenticationFragment feedlyAuthenticationFragment;
    private MyWordsFragment myWordsFragment;

    // Action Mode
    private ActionMode actionMode;
    private ZeeguuTranslationActionMode translationActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize fragments
        feedOverviewFragment = (FeedOverviewFragment) fragmentManager.findFragmentByTag("feedOverview");
        if (feedOverviewFragment == null) feedOverviewFragment = new FeedOverviewFragment();

        feedEntryListFragment = (FeedEntryListFragment) fragmentManager.findFragmentByTag("feedEntryList");
        if (feedEntryListFragment == null) feedEntryListFragment = new FeedEntryListFragment();

        feedEntryCompatibilityFragment = (FeedEntryCompatibilityFragment) fragmentManager.findFragmentByTag("feedItemCompatibility");
        if (feedEntryCompatibilityFragment == null) feedEntryCompatibilityFragment = new FeedEntryCompatibilityFragment();

        feedlyAuthenticationFragment = (FeedlyAuthenticationFragment) fragmentManager.findFragmentByTag("feedlyAuthenticationFragment");
        if (feedlyAuthenticationFragment == null) feedlyAuthenticationFragment = new FeedlyAuthenticationFragment();

        myWordsFragment = (MyWordsFragment) fragmentManager.findFragmentByTag("myWords");
        if (myWordsFragment == null) myWordsFragment = new MyWordsFragment();

        // Layout
        setContentView(R.layout.activity_main);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        statusbarBackground = (RelativeLayout) findViewById(R.id.statusbar_background);
        panelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        contentFrame = (FrameLayout) findViewById(R.id.content);

        viewPager = (ViewPager) findViewById(R.id.panel);
        pagerAdapter = new FeedEntryPagerAdapter(fragmentManager);
        viewPager.setAdapter(pagerAdapter);

        setUpToolbar();
        setUpNavigationDrawer(navigationView);

        // Display Feed Overview
        if (savedInstanceState == null)
            switchFragment(feedOverviewFragment, "feedOverview", getString(R.string.title_feed_overview));
    }

    private void setUpNavigationDrawer(NavigationView navigationView) {
        ActionBar actionBar = getSupportActionBar();

        if (toolbar != null && actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                    R.string.navigation_drawer_open,  R.string.navigation_drawer_close);
            drawerLayout.setDrawerListener(drawerToggle);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (drawerToggle.isDrawerIndicatorEnabled())
                        drawerLayout.openDrawer(GravityCompat.START);
                    else
                        onBackPressed();
                }
            });
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                selectDrawerItem(menuItem);
                return true;
            }
        });

        // TODO: Navigation drawer login
        MenuItem navigation_feedly = navigationView.getMenu().findItem(R.id.navigation_feedly);
        navigation_feedly.setVisible(false);
        MenuItem navigation_zeeguu = navigationView.getMenu().findItem(R.id.navigation_zeeguu);
        navigation_zeeguu.setVisible(false);

    }

    private void selectDrawerItem(MenuItem menuItem) {
        CharSequence title = menuItem.getTitle();

        // Update the main content by replacing fragments
        switch (menuItem.getItemId()) {
            case R.id.navigation_feed_overview:
                switchFragment(feedOverviewFragment, "feedOverview", title);
                break;
            case R.id.navigation_my_words:
                switchFragment(myWordsFragment, "myWords", title);
                break;
            case R.id.navigation_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        menuItem.setChecked(true);
        drawerLayout.closeDrawers();
    }

    /**
     * Set the content, color and style of the action bar for this fragment
     */
    @Override
    public void setActionBar(boolean displayBackButton, int color) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            return;

        // Display back arrow
        animateBackArrow(displayBackButton);

        // Set color
        if (color != 0) {
            // TODO: Check compatibility with older Android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                statusbarBackground.setBackgroundColor(color);
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
        }
    }

    @Override
    public void resetActionBar() {
        // Use default colors
        setActionBar(false, getResources().getColor(R.color.action_bar_gray));
    }

    private void animateBackArrow(final boolean displayBackButton) {
        ValueAnimator animator;

        // Set animation direction
        if (displayBackButton)
            animator = ValueAnimator.ofFloat(0, 1);
        else {
            animator = ValueAnimator.ofFloat(1, 0);
            drawerToggle.setDrawerIndicatorEnabled(true);
        }

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                drawerToggle.onDrawerSlide(drawerLayout, slideOffset);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                drawerToggle.setDrawerIndicatorEnabled(!displayBackButton);
            }
        });

        animator.setInterpolator(new DecelerateInterpolator());

        // Animation speed
        animator.setDuration(300);
        animator.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //if (!navigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
        getMenuInflater().inflate(R.menu.global, menu);
            //restoreActionBar();
            return true;
        //}

        //return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Drawer Toggle
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // TODO: Remove settings from menu
        if (id == R.id.action_settings) {
            //switchFragment(settingsFragment, "settings", getString(R.string.title_settings));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Allow to use the Android back button to navigate back in the WebView
     */
    @Override
    public void onBackPressed() {

        if (panelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            if (!getCurrentFeedEntryFragment().goBack())
                return;

            panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
            return;
        }

        if (feedlyAuthenticationFragment.goBack() && fragmentManager.getBackStackEntryCount() > 0) {
            popBackStack();
        }
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        actionMode = mode;

        if (displayTranslationActionMode()) {
            translationActionMode = new ZeeguuTranslationActionMode(getCurrentFeedEntryFragment());
            translationActionMode.onPrepareActionMode(mode, mode.getMenu());
            translationActionMode.onCreateActionMode(mode, mode.getMenu());
        }

        super.onSupportActionModeStarted(mode);
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        actionMode = null;

        if (displayTranslationActionMode())
            translationActionMode.onDestroyActionMode(mode);

        super.onSupportActionModeFinished(mode);
    }

    public void onActionItemClicked(MenuItem item) {
        // Handle custom action mode clicks
        if (actionMode != null) {
            if (displayTranslationActionMode())
                translationActionMode.onActionItemClicked(actionMode, item);
        }
    }

    private boolean displayTranslationActionMode() {
        return panelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    // Feed entry fragment navigation
    @Override
    public void displayFeedEntryList(Feed feed) {
        feedEntryListFragment.setFeed(feed);
        switchFragmentBackstack(feedEntryListFragment, "feedEntryList", feed.getName());

        if (!isPanelLoaded) {
            pagerAdapter.setFeed(feed);
            pagerAdapter.notifyDataSetChanged();

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    panelLayout.setDragView(getCurrentFeedEntryFragment().getPanelHeader());
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            // Show panel
            setUpSlidingPanel();

            isPanelLoaded = true;
        }
    }

    /**
     * Workaround to get the FeedEntryFragment currently visible in the viewPager,
     * see: http://stackoverflow.com/a/8886019
     */
    private FeedEntryFragment getCurrentFeedEntryFragment() {
        return (FeedEntryFragment) pagerAdapter.instantiateItem(viewPager, viewPager.getCurrentItem());
    }

    private void setUpSlidingPanel() {
        panelLayout.setPanelHeight((int) DisplayUtility.dpToPx(this, 68));
        panelLayout.setShadowHeight((int) DisplayUtility.dpToPx(this, 4));

        panelLayout.setDragView(getCurrentFeedEntryFragment().getPanelHeader());

        panelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {

            }

            @Override
            public void onPanelCollapsed(View view) {
                getCurrentFeedEntryFragment().getPanelHeader().setBackgroundColor(getResources().getColor(R.color.white));
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            @Override
            public void onPanelExpanded(View view) {
                getCurrentFeedEntryFragment().getPanelHeader().setBackgroundColor(getResources().getColor(R.color.transparent_white));
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override
            public void onPanelAnchored(View view) {

            }

            @Override
            public void onPanelHidden(View view) {

            }
        });
    }

    @Override
    public void displayFeedEntry(Feed feed, int position) {
        if (!feed.equals(pagerAdapter.getFeed())) {
            pagerAdapter.setFeed(feed);
            pagerAdapter.notifyDataSetChanged();
        }

        viewPager.setCurrentItem(position);

        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    @Override
    public void setSubscriptions(ArrayList<Category> categories, boolean update) {
        // Update unread count
        for (Category category : categories)
            category.getUnreadCount();

        if (update)
            feedOverviewFragment.updateSubscriptions(categories);
        else
            feedOverviewFragment.setCategories(categories);
    }

    // Feedly authentication
    @Override
    public void displayFeedlyAuthentication(String url) {
        feedlyAuthenticationFragment.setUrl(url);

        CharSequence title = getString(R.string.title_feedly_authentication);
        switchFragmentBackstack(feedlyAuthenticationFragment, "feedlyAuthentication", title);
    }

    @Override
    public void feedlyAuthenticationResponse(String response, boolean successful) {
        if (successful)
            getFeedlyConnectionManager().authenticationSuccessful(response);
        else
            getFeedlyConnectionManager().authenticationFailed(response);

        // Switch to next fragment
        CharSequence title = getString(R.string.title_feed_overview);
        switchFragment(feedOverviewFragment, "feedOverview", title);

        getFeedlyConnectionManager().getCategories();
    }

    // Messages
    @Override
    public void displayMessage(String message) {
        Snackbar.make(contentFrame, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void displayErrorMessage(String error, boolean isToast) {
        if (isToast)
            Snackbar.make(contentFrame, error, Snackbar.LENGTH_SHORT).show();
        else {
            getCurrentFeedEntryFragment().setTranslation(error);
        }
    }

    // Zeeguu
    @Override
    public ZeeguuWebViewFragment getWebViewFragment() {
        return getCurrentFeedEntryFragment();
    }

    @Override
    public void setTranslation(String translation) {
        getCurrentFeedEntryFragment().setTranslation(translation);
    }

    @Override
    public void highlight(String word) {
        if (sharedPref.getBoolean("pref_zeeguu_highlight_words", true)) {
            getCurrentFeedEntryFragment().highlight(word);
        }
    }

    @Override
    public void notifyDataChanged(boolean myWordsChanged) {
        myWordsFragment.notifyDataSetChanged(myWordsChanged);
    }
}