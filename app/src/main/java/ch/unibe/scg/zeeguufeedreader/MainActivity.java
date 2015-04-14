package ch.unibe.scg.zeeguufeedreader;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.Toast;

import ch.unibe.scg.zeeguufeedreader.FeedItemCompatibility.FeedItemCompatibilityFragment;

/**
 *  Activity to display and switch between the fragments
 */
public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private FragmentManager fragmentManager = getSupportFragmentManager();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private WebViewFragment webViewFragment = new WebViewFragment();
    private FeedOverviewFragment feedOverviewFragment = new FeedOverviewFragment();
    private FeedItemFragment feedItemFragment = new FeedItemFragment();
    private FeedItemCompatibilityFragment feedItemCompatibilityFragment = new FeedItemCompatibilityFragment();

    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;

    private ActionMode actionMode = null;
    private String currentFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.AppThemeBlue);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Display Feed Overview
        // TODO: Mark "Feed List" as active in the navigation drawer
        onNavigationDrawerItemSelected(0);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        switch (position + 1) {
            case 1:
                mTitle = getString(R.string.title_section1);
                switchFragment(feedOverviewFragment, "feedOverviewFragment");
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                if (currentApiVersion >= android.os.Build.VERSION_CODES.KITKAT)
                    switchFragment(feedItemFragment, "feedItemFragment");
                else
                    switchFragment(feedItemCompatibilityFragment, "feedItemCompatibilityFragment");
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                switchFragment(webViewFragment, "webViewFragment");
                break;
            case 4:
                getWindow().setStatusBarColor(getResources().getColor(R.color.darkred));
                break;
            case 5:
                getWindow().setStatusBarColor(getResources().getColor(R.color.black));
                break;
        }
    }

    private void switchFragment(Fragment fragment, String currentFragment) {
        fragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit();
        this.currentFragment = currentFragment;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }

        //Menu translationMenu = menu;
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
        if (actionMode == null && currentFragment.equals("feedItemFragment")) {
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
                Toast.makeText(this, "Word saved to your wordlist", Toast.LENGTH_SHORT).show();
                actionMode.finish(); // Action picked, so close the CAB
                break;
            case R.id.action_context:
                feedItemFragment.extractContextFromPage();
                actionMode.finish();
                break;
            case R.id.action_highlight:
                feedItemFragment.highlight();
                actionMode.finish();
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
}