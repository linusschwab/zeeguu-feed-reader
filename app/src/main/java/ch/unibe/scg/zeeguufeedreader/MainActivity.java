package ch.unibe.scg.zeeguufeedreader;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import ch.unibe.scg.zeeguufeedreader.FeedItemCompatibility.FeedItemCompatibilityFragment;

/**
 *  Activity to display and switch between the fragments
 */
public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private FragmentManager fragmentManager = getFragmentManager();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment navigationDrawerFragment;

    private FeedOverviewFragment feedOverviewFragment;
    private FeedItemFragment feedItemFragment;
    private FeedItemCompatibilityFragment feedItemCompatibilityFragment;
    private SettingsFragment settingsFragment;

    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;

    private ActionMode actionMode = null;
    private String currentFragment;

    private boolean browser = true;

    private ZeeguuConnectionManager connectionManager;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize fragments
        feedOverviewFragment = (FeedOverviewFragment) fragmentManager.findFragmentByTag("feedOverviewFragment");
        if (feedOverviewFragment == null) feedOverviewFragment = new FeedOverviewFragment();

        feedItemFragment = (FeedItemFragment) fragmentManager.findFragmentByTag("feedItemFragment");
        if (feedItemFragment == null) feedItemFragment = new FeedItemFragment();

        feedItemCompatibilityFragment = (FeedItemCompatibilityFragment) fragmentManager.findFragmentByTag("feedItemCompatibilityFragment");
        if (feedItemCompatibilityFragment == null) feedItemCompatibilityFragment = new FeedItemCompatibilityFragment();

        settingsFragment = (SettingsFragment) fragmentManager.findFragmentByTag("settingsFragment");
        if (settingsFragment == null) settingsFragment = new SettingsFragment();

        // Layout
        setContentView(R.layout.activity_main);

        navigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        // Display Feed Overview
        if (savedInstanceState == null) {
            // TODO: Mark "Feed List" as active in the navigation drawer
            onNavigationDrawerItemSelected(0);
            navigationDrawerFragment.closeDrawer();
        }

        connectionManager = new ZeeguuConnectionManager(this, feedItemFragment);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        if (browser) {
            switch (position + 1) {
                case 1:
                    title = "Browser";
                    switchFragment(feedItemFragment, "feedItemFragment");
                    break;
                case 2:
                    title = "Settings";
                    switchFragment(settingsFragment, "settingsFragment");
                    break;
            }
        }
        else {
            switch (position + 1) {
                case 1:
                    title = getString(R.string.title_section1);
                    switchFragment(feedOverviewFragment, "feedOverviewFragment");
                    break;
                case 2:
                    title = getString(R.string.title_section2);
                    if (currentApiVersion >= android.os.Build.VERSION_CODES.KITKAT)
                        switchFragment(feedItemFragment, "feedItemFragment");
                    else
                        switchFragment(feedItemCompatibilityFragment, "feedItemCompatibilityFragment");
                    break;
                case 3:
                    title = getString(R.string.title_section3);
                    switchFragment(settingsFragment, "settingsFragment");
                    break;
                case 4:
                    getWindow().setStatusBarColor(getResources().getColor(R.color.darkred));
                    break;
                case 5:
                    getWindow().setStatusBarColor(getResources().getColor(R.color.black));
                    break;
            }
        }
    }

    private void switchFragment(Fragment fragment, String tag) {
        fragmentManager.beginTransaction()
            .replace(R.id.container, fragment, tag)
            .commit();
        currentFragment = tag;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
        actionBar.setDisplayShowCustomEnabled(false);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putCharSequence("title", title);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        title = savedInstanceState.getCharSequence("title");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.global, menu);
            restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            title = getString(R.string.title_section3);
            switchFragment(settingsFragment, "settingsFragment");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Allow to use the Android back button to navigate back in the WebView
     */
    @Override
    public void onBackPressed() {
        if (feedItemFragment.goBack())
            super.onBackPressed();
    }

    /**
     *  Custom action mode for webview text selection
     */
    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        if (actionMode == null) {
            actionMode = mode;
            Menu menu = mode.getMenu();
            // Remove the default menu items (select all, copy, paste, search)
            menu.clear();

            // Remove menu items individually:
            // menu.removeItem(android.R.id.[id_of_item_to_remove])

            // Inflate menu items
            mode.getMenuInflater().inflate(R.menu.translation, menu);

            // Show translation bar
            feedItemFragment.getTranslationBar().setVisibility(View.VISIBLE);
        }

        super.onSupportActionModeStarted(mode);
    }

    public void onContextualMenuItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bookmark:
                feedItemFragment.extractContextFromPage();
                actionMode.finish(); // Action picked, so close the CAB
                break;
            case R.id.action_unhighlight:
                feedItemFragment.unhighlight();
                actionMode.finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        actionMode = null;

        if (currentFragment.equals("feedItemFragment")) {
            // Hide translation bar
            feedItemFragment.getTranslationBar().setVisibility(View.GONE);
        }

        super.onSupportActionModeFinished(mode);
    }

    public void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public FeedItemFragment getFeedItemFragment() {
        return feedItemFragment;
    }

    public NavigationDrawerFragment getNavigationDrawerFragment() {
        return navigationDrawerFragment;
    }

    public boolean isBrowserEnabled() {
        return browser;
    }

    public ZeeguuConnectionManager getConnectionManager() {
        return connectionManager;
    }
}