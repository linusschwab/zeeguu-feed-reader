package ch.unibe.scg.zeeguufeedreader.Core;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntryCallbacks;
import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntryPagerAdapter;
import ch.unibe.scg.zeeguufeedreader.FeedEntryList.FeedEntryListCallbacks;
import ch.unibe.scg.zeeguufeedreader.FeedEntryList.FeedEntryListFragment;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Category;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.FeedOverviewCallbacks;
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
        FeedOverviewCallbacks, FeedEntryCallbacks, FeedEntryListCallbacks,
        MyWordsFragment.ZeeguuFragmentMyWordsCallbacks,
        ZeeguuWebViewInterface.ZeeguuWebViewInterfaceCallbacks,
        ZeeguuWebViewFragment.ZeeguuWebViewCallbacks {

    // Drawer
    private Drawer drawer;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    // Layout
    private SlidingUpPanelLayout panelLayout;
    private FrameLayout contentFrame;

    private ViewPager viewPager;
    private FeedEntryPagerAdapter pagerAdapter;
    private boolean isPanelLoaded = false;

    private int currentActionBarColor;

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

        super.onCreate(savedInstanceState);

        // Layout
        setContentView(R.layout.activity_main);
        panelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        contentFrame = (FrameLayout) findViewById(R.id.content);

        viewPager = (ViewPager) findViewById(R.id.panel);
        pagerAdapter = new FeedEntryPagerAdapter(fragmentManager, this);
        viewPager.setAdapter(pagerAdapter);

        currentActionBarColor = ContextCompat.getColor(this, R.color.action_bar_gray);

        setUpToolbar();
        setUpNavigationDrawer();

        // Display Feed Overview
        if (savedInstanceState == null)
            drawer.setSelection(101, true);
    }

    private void setUpNavigationDrawer() {
        ActionBar actionBar = getSupportActionBar();

        // Header
        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.zeeguu_red)
                .addProfiles(new ProfileDrawerItem().withName("Test User").withEmail("test@test.de"))
                .withSelectionListEnabled(false)
                .build();

        // Drawer
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .build();

        // Menu items
        int selectedColor = ContextCompat.getColor(this, R.color.zeeguu_red);
        PrimaryDrawerItem feedOverview = new PrimaryDrawerItem()
                .withIcon(FontAwesome.Icon.faw_home)
                .withName(R.string.title_feed_overview)
                .withSelectedTextColor(selectedColor)
                .withSelectedIconColor(selectedColor)
                .withIdentifier(101);
        PrimaryDrawerItem myWords = new PrimaryDrawerItem()
                .withIcon(FontAwesome.Icon.faw_book)
                .withName(R.string.title_my_words)
                .withSelectedTextColor(selectedColor)
                .withSelectedIconColor(selectedColor)
                .withIdentifier(102);

        SwitchDrawerItem unreadSwitch = new SwitchDrawerItem()
                .withName(R.string.menu_unread_switch)
                .withSelectable(false)
                .withIdentifier(201);
        PrimaryDrawerItem settings = new PrimaryDrawerItem()
                .withName(R.string.title_settings)
                .withSelectable(false)
                .withIdentifier(202);

        drawer.addItems(
                feedOverview,
                myWords,
                new DividerDrawerItem(),
                unreadSwitch,
                settings
        );

        drawer.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem iDrawerItem) {
                return selectDrawerItem(view, position, iDrawerItem);
            }
        });

        unreadSwitch.withOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(IDrawerItem iDrawerItem, CompoundButton compoundButton, boolean activated) {
                getFeedlyAccount().toggleUnreadSwitch(activated);

                // Update view
                feedOverviewFragment.notifyDataSetChanged();
                if (feedEntryListFragment.isVisible())
                    feedEntryListFragment.onUnreadSwitch();
            }
        });

        // Drawer toggle
        if (toolbar != null && actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

            drawerLayout = drawer.getDrawerLayout();

            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                    R.string.navigation_drawer_open,  R.string.navigation_drawer_close);
            drawer.setActionBarDrawerToggle(drawerToggle);

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
    }

    private boolean selectDrawerItem(View view, int position, IDrawerItem iDrawerItem) {

        // Update the main content by replacing fragments
        switch (iDrawerItem.getIdentifier()) {
            case 101:
                switchFragment(feedOverviewFragment, "feedOverview", getString(R.string.title_feed_overview));
                break;
            case 102:
                switchFragment(myWordsFragment, "myWords", getString(R.string.title_my_words));
                break;
            case 201:
                break;
            case 202:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        if (!(iDrawerItem instanceof SwitchDrawerItem))
            drawer.closeDrawer();

        return true;
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawer.setStatusBarColor(Tools.darken(color, 0.25));
            }
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
            currentActionBarColor = color;
        }
    }

    @Override
    public void resetActionBar() {
        // Use default colors
        setActionBar(false, ContextCompat.getColor(this, R.color.action_bar_gray));
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

        if (actionMode != null) {
            actionMode.finish();
            return;
        }

        if (panelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            if (!getCurrentFeedEntryFragment().goBack())
                return;

            panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
            return;
        }

        if (feedlyAuthenticationFragment.goBack() && fragmentManager.getBackStackEntryCount() > 0) {
            popBackStack();
        }
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        // TODO: Set status bar color (and back to transparent on finish)
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

        if (displayTranslationActionMode() && translationActionMode != null)
            translationActionMode.onDestroyActionMode(mode);

        actionMode = null;
        translationActionMode = null;

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

    /**
     *  Connectionmanager and Account is not available before the activity is created!
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();

        SwitchDrawerItem item = (SwitchDrawerItem) drawer.getDrawerItem(201);
        item.withChecked(getFeedlyAccount().showUnreadOnly());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    // Feed entry fragment navigation
    @Override
    public void displayFeedEntryList(Category category, Feed feed) {
        if (category != null) {
            if (getFeedlyAccount().showUnreadOnly())
                feedEntryListFragment.setEntries(category.getUnreadEntries());
            else
                feedEntryListFragment.setEntries(category.getEntries());
            switchFragmentBackstack(feedEntryListFragment, "feedEntryList", category.getName());
        }
        else if (feed != null) {
            feedEntryListFragment.setFeed(feed);
            feed.setColor(Tools.getDominantColor(feed.getFavicon()));
            switchFragmentBackstack(feedEntryListFragment, "feedEntryList", feed.getName());
        }

        if (!isPanelLoaded && feedEntryListFragment.hasEntries()) {
            if (feed != null)
                pagerAdapter.setFeed(feed);
            else if (category != null) {
                if (getFeedlyAccount().showUnreadOnly())
                    pagerAdapter.setEntries(category.getUnreadEntries());
                else
                    pagerAdapter.setEntries(category.getEntries());
            }
            pagerAdapter.notifyDataSetChanged();

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    panelLayout.setDragView(getCurrentFeedEntryFragment().getPanelHeader());
                    //panelLayout.setScrollableView(getCurrentFeedEntryFragment().getWebView());

                    feedEntryListFragment.updateEntry(getCurrentFeedEntryFragment().getEntry(), position);

                    // TODO: Improve for first scroll (maybe move to FeedEntry with callback at onCreateView())
                    if (panelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                        getCurrentFeedEntryFragment().onPanelExpandend(currentActionBarColor);
                        if (position != 0)
                            getFeedEntryFragment(position-1).setHeaderColor(currentActionBarColor);
                        if (position != feedEntryListFragment.getCount()-1)
                            getFeedEntryFragment(position+1).setHeaderColor(currentActionBarColor);
                    }
                    else if (panelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        getCurrentFeedEntryFragment().onPanelCollapsed();
                    }
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

    private FeedEntryFragment getFeedEntryFragment(int position) {
        return (FeedEntryFragment) pagerAdapter.instantiateItem(viewPager, position);
    }

    private void setUpSlidingPanel() {
        panelLayout.setPanelHeight((int) Tools.dpToPx(this, 68));
        panelLayout.setShadowHeight((int) Tools.dpToPx(this, 4));

        panelLayout.setDragView(getCurrentFeedEntryFragment().getPanelHeader());
        //panelLayout.setScrollableView(getCurrentFeedEntryFragment().getWebView());

        panelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {
                // Close action mode
                if (actionMode != null)
                    actionMode.finish();
            }

            @Override
            public void onPanelCollapsed(View view) {
                getCurrentFeedEntryFragment().onPanelCollapsed();
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            @Override
            public void onPanelExpanded(View view) {
                getCurrentFeedEntryFragment().onPanelExpandend(currentActionBarColor);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

                // Check if first page is read
                if (getCurrentFeedEntryFragment().getPosition() == 0) {
                    feedEntryListFragment.updateEntry(getCurrentFeedEntryFragment().getEntry(), 0);
                }
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
        viewPager.setCurrentItem(position);
        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    @Override
    public void updateFeedEntries(ArrayList<FeedEntry> entries, Feed feed) {
        if (feed != null && !feed.equals(pagerAdapter.getFeed())) {
            pagerAdapter.setFeed(feed);
            pagerAdapter.notifyDataSetChanged();
        }
        else if (entries != null && entries.size() != 0) {
            pagerAdapter.setEntries(entries);
            pagerAdapter.notifyDataSetChanged();
        }
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

    public FeedEntry getPagerEntry(int position) {
        return getFeedEntryFragment(position).getEntry();
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