package ch.unibe.scg.zeeguufeedreader.Core;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.Stack;

import ch.unibe.scg.zeeguufeedreader.FeedOverview.Category;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyCallbacks;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyConnectionManager;
import ch.unibe.scg.zeeguufeedreader.Preferences.MainSettingsFragment;
import ch.unibe.scg.zeeguufeedreader.Preferences.PreferenceScreens.ZeeguuSettingsFragment;
import ch.unibe.scg.zeeguufeedreader.Preferences.SettingsCallbacks;
import ch.unibe.scg.zeeguufeedreader.R;
import ch.unibe.zeeguulibrary.Core.ZeeguuAccount;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuCreateAccountDialog;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuDialogCallbacks;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuLoginDialog;
import ch.unibe.zeeguulibrary.Dialogs.ZeeguuLogoutDialog;

public abstract class BaseActivity extends AppCompatActivity implements
        FeedlyCallbacks,
        ZeeguuAccount.ZeeguuAccountCallbacks,
        ZeeguuConnectionManager.ZeeguuConnectionManagerCallbacks,
        ZeeguuSettingsFragment.ZeeguuSettingsCallbacks,
        ZeeguuDialogCallbacks {

    private DataFragment dataFragment;

    protected Toolbar toolbar;

    private Stack<CharSequence> backStackTitle = new Stack<>();

    protected FragmentManager fragmentManager = getFragmentManager();
    protected SharedPreferences sharedPref;

    protected int currentApiVersion = android.os.Build.VERSION.SDK_INT;

    // Dialogs
    private ZeeguuLoginDialog zeeguuLoginDialog;
    private ZeeguuLogoutDialog zeeguuLogoutDialog;
    private ZeeguuCreateAccountDialog zeeguuCreateAccountDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        restoreDataFragment();

        // Dialogs
        createDialogs();

        // Data fragment
        createDataFragment();
    }

    // Set up
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

    private void createDialogs() {
        zeeguuLoginDialog = (ZeeguuLoginDialog) fragmentManager.findFragmentByTag("zeeguuLoginDialog");
        if (zeeguuLoginDialog == null) zeeguuLoginDialog = new ZeeguuLoginDialog();

        zeeguuLogoutDialog = (ZeeguuLogoutDialog) fragmentManager.findFragmentByTag("zeeguuLogoutDialog");
        if (zeeguuLogoutDialog == null) zeeguuLogoutDialog = new ZeeguuLogoutDialog();

        zeeguuCreateAccountDialog = (ZeeguuCreateAccountDialog) fragmentManager.findFragmentByTag("zeeguuCreateAccountDialog");
        if (zeeguuCreateAccountDialog == null) zeeguuCreateAccountDialog = new ZeeguuCreateAccountDialog();
    }

    protected void setUpToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
            setSupportActionBar(toolbar);
        }
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
    public void setTitle(CharSequence title) {
        if (toolbar != null)
            toolbar.setTitle(title);
        super.setTitle(title);
    }

    // Fragment management
    protected void addFragment(Fragment fragment, String tag) {
        fragmentManager.beginTransaction()
                .add(fragment, tag)
                .commit();
    }

    protected void switchFragment(Fragment fragment, String tag, CharSequence title) {
        emptyBackStack();
        fragmentManager.beginTransaction()
                .replace(R.id.content, fragment, tag)
                .commit();

        setTitle(title);
    }

    protected void switchFragmentBackstack(Fragment fragment, String tag, CharSequence title) {
        fragmentManager.beginTransaction()
                .replace(R.id.content, fragment, tag)
                .addToBackStack(tag)
                .commit();

        backStackTitle.push(getTitle());
        setTitle(title);
    }

    protected void popBackStack() {
        fragmentManager.popBackStack();
        if (!backStackTitle.isEmpty())
            setTitle(backStackTitle.pop());
    }

    private void emptyBackStack() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = fragmentManager.getBackStackEntryAt(0);
            fragmentManager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            backStackTitle = new Stack<>();
        }
    }

    public ZeeguuConnectionManager getZeeguuConnectionManager() {
        return dataFragment.getZeeguuConnectionManager();
    }

    public FeedlyConnectionManager getFeedlyConnectionManager() {
        return dataFragment.getFeedlyConnectionManager();
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

    // Not implemented Zeeguu Library methods (not needed in this app)
    public void openUrlInBrowser(String URL) {}

    public void notifyLanguageChanged(boolean isLanguageFrom) {}

    public void bookmarkWord(String bookmarkID) {}

    // Shared preferences helper methods
    public void saveString(int prefKey, String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getResources().getString(prefKey), value);
        editor.apply();
    }

    public String loadString(int prefKey) {
        return sharedPref.getString(getResources().getString(prefKey), "");
    }

    public void saveLong(int prefKey, Long value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(getResources().getString(prefKey), value);
        editor.apply();
    }

    public Long loadLong(int prefKey) {
        return sharedPref.getLong(getResources().getString(prefKey), 0);
    }

    public void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    // TODO: Move methods from MainActivity to BaseActivity?
    @Override
    public void setTranslation(String translation) {
    }

    @Override
    public void highlight(String word) {
    }

    @Override
    public void displayErrorMessage(String error, boolean isToast) {

    }

    @Override
    public void displayFeedlyAuthentication(String url) {

    }

    @Override
    public void displayMessage(String message) {

    }

    @Override
    public void updateSubscriptions(ArrayList<Category> categories) {

    }

    @Override
    public void notifyDataChanged(boolean myWordsChanged) {
    }
}
