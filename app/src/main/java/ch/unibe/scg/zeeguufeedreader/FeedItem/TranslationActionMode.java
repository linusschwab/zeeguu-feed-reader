package ch.unibe.scg.zeeguufeedreader.FeedItem;

import android.app.Activity;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import ch.unibe.scg.zeeguufeedreader.R;

/**
 *  Action mode callback to display the translation of selected text and add bookmarks.
 */
public class TranslationActionMode implements ActionMode.Callback {

    private FeedItemFragment feedItemFragment;
    private Activity activity;

    public TranslationActionMode(FeedItemFragment feedItemFragment) {
        this.feedItemFragment = feedItemFragment;
        activity = feedItemFragment.getActivity();
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        // Remove the default menu items (select all, copy, paste, search)
        menu.clear();

        // Remove menu items individually:
        // menu.removeItem(android.R.id.[id_of_item_to_remove])

        // Inflate a menu resource providing context menu items
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.translation, menu);

        return true;
    }

    // Called when action mode is first created. The menu supplied
    // will be used to generate action buttons for the action mode
    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        // Show translation bar
        feedItemFragment.getTranslationBar().setVisibility(View.VISIBLE);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_bookmark:
                feedItemFragment.extractContextFromPage();
                actionMode.finish(); // Action picked, so close the CAB
                return true;
            case R.id.action_unhighlight:
                feedItemFragment.unhighlight();
                actionMode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        feedItemFragment.getTranslationBar().setVisibility(View.GONE);
        feedItemFragment.getTranslationBar().setText("");
    }
}