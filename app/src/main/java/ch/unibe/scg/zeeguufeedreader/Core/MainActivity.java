package ch.unibe.scg.zeeguufeedreader.Core;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.FeedEntryList.FeedEntryListFragment;
import ch.unibe.zeeguulibrary.WebView.BrowserFragment;
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
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        SettingsFragment.SettingsCallbacks,
        FeedOverviewFragment.FeedOverviewCallbacks,
        FeedEntryListFragment.FeedEntryListCallbacks,
        BrowserFragment.BrowserCallbacks,
        MyWordsFragment.ZeeguuFragmentMyWordsCallbacks,
        ZeeguuWebViewInterface.ZeeguuWebViewInterfaceCallbacks,
        ZeeguuWebViewFragment.ZeeguuWebViewCallbacks,
        ZeeguuConnectionManager.ZeeguuConnectionManagerCallbacks,
        ZeeguuAccount.ZeeguuAccountCallbacks,
        ZeeguuDialogCallbacks {

    private FragmentManager fragmentManager = getFragmentManager();

    // Fragments
    private DataFragment dataFragment;
    private NavigationDrawerFragment navigationDrawerFragment;
    private FeedOverviewFragment feedOverviewFragment;
    private FeedEntryListFragment feedEntryListFragment;
    private FeedEntryFragment feedEntryFragment;
    private BrowserFragment browserFragment;
    private FeedEntryCompatibilityFragment feedEntryCompatibilityFragment;
    private SettingsFragment settingsFragment;
    private MyWordsFragment myWordsFragment;

    // Dialogs
    private ZeeguuLoginDialog zeeguuLoginDialog;
    private ZeeguuLogoutDialog zeeguuLogoutDialog;
    private ZeeguuCreateAccountDialog zeeguuCreateAccountDialog;

    private ActionMode actionMode;
    private ZeeguuTranslationActionMode translationActionMode;

    private SharedPreferences sharedPref;
    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;

    private String currentFragment;

    private boolean browser = false;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;

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

        browserFragment = (BrowserFragment) fragmentManager.findFragmentByTag("browser");
        if (browserFragment == null) browserFragment = new BrowserFragment();

        feedEntryCompatibilityFragment = (FeedEntryCompatibilityFragment) fragmentManager.findFragmentByTag("feedItemCompatibility");
        if (feedEntryCompatibilityFragment == null) feedEntryCompatibilityFragment = new FeedEntryCompatibilityFragment();

        myWordsFragment = (MyWordsFragment) fragmentManager.findFragmentByTag("myWords");
        if (myWordsFragment == null) myWordsFragment = new MyWordsFragment();

        settingsFragment = (SettingsFragment) fragmentManager.findFragmentByTag("settings");
        if (settingsFragment == null) settingsFragment = new SettingsFragment();

        // Action Mode
        if (browser)
            translationActionMode = new ZeeguuTranslationActionMode(browserFragment);
        else
            translationActionMode = new ZeeguuTranslationActionMode(feedEntryFragment);

        // Data fragment
        createDataFragment();

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
            navigationDrawerFragment.selectItem(0);
        }
    }

    private void createDataFragment() {
        if (dataFragment == null) {
            // Add the fragment
            dataFragment = new DataFragment();
            addFragment(dataFragment, "data");
            // Create objects, store in data fragment
            //dataFragment.setConnectionManager(this);
        }
    }

    private void restoreDataFragment() {
        dataFragment = (DataFragment) fragmentManager.findFragmentByTag("data");
        if (dataFragment != null) dataFragment.onRestore(this);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        if (browser) {
            switch (position + 1) {
                case 1:
                    title = "Browser";
                    switchFragment(browserFragment, "browser");
                    break;
                case 2:
                    title = getString(R.string.title_myWords);
                    switchFragment(myWordsFragment, "myWords");
                    break;
                case 3:
                    title = getString(R.string.title_settings);
                    switchFragment(settingsFragment, "settings");
                    break;
            }
        }
        else {
            switch (position + 1) {
                case 1:
                    switchFragment(feedOverviewFragment, "feedOverview");
                    break;
                case 2:
                    title = getString(R.string.title_feedEntry);
                    if (currentApiVersion >= android.os.Build.VERSION_CODES.KITKAT)
                        switchFragment(feedEntryFragment, "feedEntry");
                    else
                        switchFragment(feedEntryCompatibilityFragment, "feedEntryCompatibility");
                    break;
                case 3:
                    title = getString(R.string.title_myWords);
                    switchFragment(myWordsFragment, "myWords");
                    break;
                case 4:
                    title = getString(R.string.title_settings);
                    switchFragment(settingsFragment, "settings");
                    break;
            }
        }
    }

    private void addFragment(Fragment fragment, String tag) {
        fragmentManager.beginTransaction()
            .add(fragment, tag)
            .commit();
    }

    private void switchFragment(Fragment fragment, String tag) {
        fragmentManager.beginTransaction()
            .replace(R.id.container, fragment, tag)
            .addToBackStack(tag)
            .commit();
        currentFragment = tag;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
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

        // Back button
        if (id == android.R.id.home) {
            onBackPressed();
        }
        if (id == R.id.action_settings) {
            title = getString(R.string.title_settings);
            switchFragment(settingsFragment, "settings");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Allow to use the Android back button to navigate back in the WebView
     */
    @Override
    public void onBackPressed() {
        browserFragment.goBack();

        if (feedEntryFragment.goBack() && fragmentManager.getBackStackEntryCount() > 1)
            fragmentManager.popBackStack();

        //super.onBackPressed();
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        actionMode = mode;

        if (currentFragment.equals("feedEntry") || currentFragment.equals("browser")) {
            translationActionMode.onPrepareActionMode(mode, mode.getMenu());
            translationActionMode.onCreateActionMode(mode, mode.getMenu());
        }

        super.onSupportActionModeStarted(mode);
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        actionMode = null;

        if (currentFragment.equals("feedEntry") || currentFragment.equals("browser"))
            translationActionMode.onDestroyActionMode(mode);

        super.onSupportActionModeFinished(mode);
    }

    public void onActionItemClicked(MenuItem item) {
        // Handle custom action mode clicks
        if (actionMode != null) {
            if (currentFragment.equals("feedEntry") || currentFragment.equals("browser"))
                translationActionMode.onActionItemClicked(actionMode, item);
        }
    }

    public void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public ZeeguuWebViewFragment getWebViewFragment() {
        if (browser)
            return browserFragment;
        else
            return feedEntryFragment;
    }

    public NavigationDrawerFragment getNavigationDrawerFragment() {
        return navigationDrawerFragment;
    }

    public boolean isBrowserEnabled() {
        return browser;
    }

    public ZeeguuConnectionManager getConnectionManager() {
        return dataFragment.getConnectionManager();
    }

    // Zeeguu interface methods
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

    @Override
    public void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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

    @Override
    public void displayErrorMessage(String error, boolean isToast) {
        if (isToast)
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        else {
            if (browser)
                browserFragment.setTranslation(error);
            else
                feedEntryFragment.setTranslation(error);
        }
    }

    @Override
    public void setTranslation(String translation) {
        if (browser)
            browserFragment.setTranslation(translation);
        else
            feedEntryFragment.setTranslation(translation);

    }

    @Override
    public void highlight(String word) {
        if (sharedPref.getBoolean("pref_zeeguu_highlight_words", true)) {
            if (browser)
                browserFragment.highlight(word);
            else
                feedEntryFragment.highlight(word);
        }
    }

    @Override
    public void displayFeedEntryList(ArrayList<FeedEntry> entries) {
        feedEntryListFragment.setEntries(entries);
        switchFragment(feedEntryListFragment, "feedEntryList");
    }

    /**
     * Set the content, color and style of the action bar for this fragment
     */
    @Override
    public void setActionBar(String title, boolean displayBackButton, int statusBarColor, int actionBarColor) {
        ActionBar actionBar = getSupportActionBar();

        // Title
        if (!title.equals("")) {
            this.title = title;
            actionBar.setTitle(title);
        }

        // Display back arrow
        if (displayBackButton)
            navigationDrawerFragment.displayDrawerToggle(false);
        else
            navigationDrawerFragment.displayDrawerToggle(true);

        // Set color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(statusBarColor);
        actionBar.setBackgroundDrawable(new ColorDrawable(actionBarColor));
    }

    @Override
    public void resetActionBar() {
        // Use default colors
        setActionBar("", false,
                getResources().getColor(R.color.status_bar_gray),
                getResources().getColor(R.color.action_bar_gray));
    }
}