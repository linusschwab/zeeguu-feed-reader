package ch.unibe.scg.zeeguufeedreader.Core;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Stack;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.FeedEntryList.FeedEntryListFragment;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyAuthenticationFragment;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyCallbacks;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyConnectionManager;
import ch.unibe.zeeguulibrary.WebView.ZeeguuTranslationActionMode;
import ch.unibe.zeeguulibrary.WebView.ZeeguuWebViewFragment;
import ch.unibe.scg.zeeguufeedreader.FeedEntry.Compatibility.FeedEntryCompatibilityFragment;
import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntryFragment;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.FeedOverviewFragment;
import ch.unibe.scg.zeeguufeedreader.R;
import ch.unibe.scg.zeeguufeedreader.Preferences.SettingsFragment;
import ch.unibe.zeeguulibrary.WebView.ZeeguuWebViewInterface;
import ch.unibe.zeeguulibrary.Core.ZeeguuAccount;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuCreateAccountDialog;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuDialogCallbacks;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuLoginDialog;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuLogoutDialog;
import ch.unibe.zeeguulibrary.MyWords.MyWordsFragment;

/**
 *  Activity to display and switch between the fragments
 */
public class MainActivity extends AppCompatActivity implements
        SettingsFragment.SettingsCallbacks,
        FeedOverviewFragment.FeedOverviewCallbacks,
        FeedEntryListFragment.FeedEntryListCallbacks,
        FeedlyCallbacks,
        FeedlyAuthenticationFragment.FeedlyAuthenticationCallbacks,
        MyWordsFragment.ZeeguuFragmentMyWordsCallbacks,
        ZeeguuWebViewInterface.ZeeguuWebViewInterfaceCallbacks,
        ZeeguuWebViewFragment.ZeeguuWebViewCallbacks,
        ZeeguuConnectionManager.ZeeguuConnectionManagerCallbacks,
        ZeeguuAccount.ZeeguuAccountCallbacks,
        ZeeguuDialogCallbacks {

    // Navigation
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private RelativeLayout statusBarBackground;
    private NavigationView navigationView;
    private SlidingUpPanelLayout panel;
    private FrameLayout contentFrame;

    private FragmentManager fragmentManager = getFragmentManager();

    // Fragments
    private DataFragment dataFragment;
    private FeedOverviewFragment feedOverviewFragment;
    private FeedEntryListFragment feedEntryListFragment;
    private FeedEntryFragment feedEntryFragment;
    private FeedEntryCompatibilityFragment feedEntryCompatibilityFragment;
    private FeedlyAuthenticationFragment feedlyAuthenticationFragment;
    private SettingsFragment settingsFragment;
    private MyWordsFragment myWordsFragment;

    // Dialogs
    private ZeeguuLoginDialog zeeguuLoginDialog;
    private ZeeguuLogoutDialog zeeguuLogoutDialog;
    private ZeeguuCreateAccountDialog zeeguuCreateAccountDialog;

    // Action Mode
    private ActionMode actionMode;
    private ZeeguuTranslationActionMode translationActionMode;

    private SharedPreferences sharedPref;
    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;

    private Stack<CharSequence> backStackTitle = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        restoreDataFragment();

        // Dialogs
        zeeguuLoginDialog = (ZeeguuLoginDialog) fragmentManager.findFragmentByTag("zeeguuLoginDialog");
        if (zeeguuLoginDialog == null) zeeguuLoginDialog = new ZeeguuLoginDialog();

        zeeguuLogoutDialog = (ZeeguuLogoutDialog) fragmentManager.findFragmentByTag("zeeguuLogoutDialog");
        if (zeeguuLogoutDialog == null) zeeguuLogoutDialog = new ZeeguuLogoutDialog();

        zeeguuCreateAccountDialog = (ZeeguuCreateAccountDialog) fragmentManager.findFragmentByTag("zeeguuCreateAccountDialog");
        if (zeeguuCreateAccountDialog == null) zeeguuCreateAccountDialog = new ZeeguuCreateAccountDialog();

        // Initialize UI fragments
        feedOverviewFragment = (FeedOverviewFragment) fragmentManager.findFragmentByTag("feedOverview");
        if (feedOverviewFragment == null) feedOverviewFragment = new FeedOverviewFragment();

        feedEntryListFragment = (FeedEntryListFragment) fragmentManager.findFragmentByTag("feedEntryList");
        if (feedEntryListFragment == null) feedEntryListFragment = new FeedEntryListFragment();

        feedEntryFragment = (FeedEntryFragment) fragmentManager.findFragmentByTag("feedEntry");
        if (feedEntryFragment == null) feedEntryFragment = new FeedEntryFragment();

        feedEntryCompatibilityFragment = (FeedEntryCompatibilityFragment) fragmentManager.findFragmentByTag("feedItemCompatibility");
        if (feedEntryCompatibilityFragment == null) feedEntryCompatibilityFragment = new FeedEntryCompatibilityFragment();

        feedlyAuthenticationFragment = (FeedlyAuthenticationFragment) fragmentManager.findFragmentByTag("feedlyAuthenticationFragment");
        if (feedlyAuthenticationFragment == null) feedlyAuthenticationFragment = new FeedlyAuthenticationFragment();

        myWordsFragment = (MyWordsFragment) fragmentManager.findFragmentByTag("myWords");
        if (myWordsFragment == null) myWordsFragment = new MyWordsFragment();

        settingsFragment = (SettingsFragment) fragmentManager.findFragmentByTag("settings");
        if (settingsFragment == null) settingsFragment = new SettingsFragment();

        // Action Mode
        translationActionMode = new ZeeguuTranslationActionMode(feedEntryFragment);

        // Data fragment
        createDataFragment();

        // Layout
        setContentView(R.layout.activity_main);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        statusBarBackground = (RelativeLayout) findViewById(R.id.status_bar_background);
        panel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        contentFrame = (FrameLayout) findViewById(R.id.container);

        setUpToolbar();
        setUpNavigationDrawer(navigationView);

        // Display Feed Overview
        if (savedInstanceState == null)
            switchFragment(feedOverviewFragment, "feedOverview", getString(R.string.title_feed_overview));
    }

    private void setUpToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
            setSupportActionBar(toolbar);
        }
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
                switchFragment(settingsFragment, "settings", title);
                break;
        }

        menuItem.setChecked(true);
        drawerLayout.closeDrawers();
    }

    private void createDataFragment() {
        if (dataFragment == null) {
            // Add the fragment
            dataFragment = new DataFragment();
            addFragment(dataFragment, "data");
            // Create objects, store in data fragment
            //dataFragment.setZeeguuConnectionManager(this);
        }
    }

    private void restoreDataFragment() {
        dataFragment = (DataFragment) fragmentManager.findFragmentByTag("data");
        if (dataFragment != null) dataFragment.onRestore(this);
    }

    private void addFragment(Fragment fragment, String tag) {
        fragmentManager.beginTransaction()
            .add(fragment, tag)
            .commit();
    }

    private void switchFragment(Fragment fragment, String tag, CharSequence title) {
        emptyBackStack();
        fragmentManager.beginTransaction()
            .replace(R.id.container, fragment, tag)
            .commit();

        setTitle(title);
    }

    private void switchFragmentBackstack(Fragment fragment, String tag, CharSequence title) {
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, tag)
                .addToBackStack(tag)
                .commit();

        backStackTitle.push(getTitle());
        setTitle(title);
    }

    private void emptyBackStack() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = fragmentManager.getBackStackEntryAt(0);
            fragmentManager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            backStackTitle = new Stack<>();
        }
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
        drawerToggle.setDrawerIndicatorEnabled(!displayBackButton);

        // Set color
        if (color != 0) {
            // TODO: Check compatibility with older Android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                statusBarBackground.setBackgroundColor(color);
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
        }
    }

    @Override
    public void resetActionBar() {
        // Use default colors
        setActionBar(false, getResources().getColor(R.color.action_bar_gray));
    }

    @Override
    public void setTitle(CharSequence title) {
        if (toolbar != null)
            toolbar.setTitle(title);
        super.setTitle(title);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putCharSequence("title", getTitle());
        savedInstanceState.putCharSequenceArrayList("backStackTitle", new ArrayList<CharSequence>(backStackTitle));
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        setTitle(savedInstanceState.getCharSequence("title"));

        ArrayList<CharSequence> titleArrayList = savedInstanceState.getCharSequenceArrayList("backStackTitle");
        if (titleArrayList != null) {
            backStackTitle = new Stack<>();
            for (CharSequence title : titleArrayList) {
                backStackTitle.push(title);
            }
        }
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
            switchFragment(settingsFragment, "settings", getString(R.string.title_settings));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Allow to use the Android back button to navigate back in the WebView
     */
    @Override
    public void onBackPressed() {

        if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
            return;
        }

        // TODO: feedEntryFragment.goBack() (Make sure that this only applies to web pages and not feed entries)
        if (feedlyAuthenticationFragment.goBack() && fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            if (!backStackTitle.isEmpty())
                setTitle(backStackTitle.pop());
        }
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        actionMode = mode;

        if (displayTranslationActionMode()) {
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
        return panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED;
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

    public void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    // Feed entry fragment navigation
    @Override
    public void displayFeedEntryList(Feed feed) {
        feedEntryListFragment.setFeed(feed);
        switchFragmentBackstack(feedEntryListFragment, "feedEntryList", feed.getName());

        // Show panel
        panel.setPanelHeight((int) Utility.dpToPx(this, 68));
        panel.setShadowHeight((int) Utility.dpToPx(this, 4));

        // Load fragment
        if (!feedEntryFragment.isAdded() && !feed.getEntries().isEmpty()) {
            FeedEntry entry = feed.getEntries().get(0);
            feedEntryFragment.setEntry(entry);

            TextView panel_title = (TextView) findViewById(R.id.panel_title);
            panel_title.setText(entry.getTitle());

            Thread thread = new Thread(backgroundThread);
            thread.start();
        }
    }

    @Override
    public void displayFeedEntry(FeedEntry entry) {
        feedEntryFragment.setEntry(entry);

        panel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    private Runnable backgroundThread = new Runnable() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

            // TODO: Temporary workaround
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            fragmentManager.beginTransaction()
                    .replace(R.id.panel, feedEntryFragment, "feedEntry")
                    .commit();
        }
    };

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
            dataFragment.getFeedlyConnectionManager().authenticationSuccessful(response);
        else
            dataFragment.getFeedlyConnectionManager().authenticationFailed(response);

        // Switch to next fragment
        CharSequence title = getString(R.string.title_feed_overview);
        switchFragment(feedOverviewFragment, "feedOverview", title);
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
            feedEntryFragment.setTranslation(error);
        }
    }

    // Zeeguu authentication
    @Override
    public void showZeeguuLoginDialog(String message, String email) {
        zeeguuLoginDialog.setMessage(message);
        zeeguuLoginDialog.setEmail(email);
        zeeguuLoginDialog.show(fragmentManager, "zeeguuLoginDialog");
    }

    public void showZeeguuLogoutDialog() {
        zeeguuLogoutDialog.show(fragmentManager, "zeeguuLogoutDialog");
    }

    public void showZeeguuCreateAccountDialog(String message, String username, String email) {
        zeeguuCreateAccountDialog.setMessage(message);
        zeeguuCreateAccountDialog.setUsername(username);
        zeeguuCreateAccountDialog.setEmail(email);
        zeeguuCreateAccountDialog.show(fragmentManager, "zeeguuCreateAccountDialog");
    }

    // Zeeguu
    @Override
    public ZeeguuWebViewFragment getWebViewFragment() {
        return feedEntryFragment;
    }

    @Override
    public ZeeguuConnectionManager getConnectionManager() {
        return dataFragment.getZeeguuConnectionManager();
    }

    @Override
    public void setTranslation(String translation) {
        feedEntryFragment.setTranslation(translation);
    }

    @Override
    public void highlight(String word) {
        if (sharedPref.getBoolean("pref_zeeguu_highlight_words", true)) {
            feedEntryFragment.highlight(word);
        }
    }

    @Override
    public void openUrlInBrowser(String URL) {

    }

    @Override
    public void notifyDataChanged(boolean myWordsChanged) {
        myWordsFragment.notifyDataSetChanged(myWordsChanged);
    }

    @Override
    public void notifyLanguageChanged(boolean isLanguageFrom) {

    }

    @Override
    public void bookmarkWord(String bookmarkID) {

    }

    /*
     * Shared preferences helper methods
     */
    @Override
    public void saveString(int prefKey, String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getResources().getString(prefKey), value);
        editor.apply();
    }

    @Override
    public String loadString(int prefKey) {
        return sharedPref.getString(getResources().getString(prefKey), "");
    }

    @Override
    public void saveLong(int prefKey, Long value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(getResources().getString(prefKey), value);
        editor.apply();
    }

    @Override
    public Long loadLong(int prefKey) {
        return sharedPref.getLong(getResources().getString(prefKey), 0);
    }
}