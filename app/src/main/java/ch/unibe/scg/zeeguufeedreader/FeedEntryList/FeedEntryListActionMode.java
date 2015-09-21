package ch.unibe.scg.zeeguufeedreader.FeedEntryList;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyAccount;
import ch.unibe.scg.zeeguufeedreader.R;

public class FeedEntryListActionMode implements ActionMode.Callback {

    private FeedEntryListActionModeCallbacks callback;

    private View selectedView;
    private int selectedPosition;
    private FeedEntry entry;

    public interface FeedEntryListActionModeCallbacks {
        FeedlyAccount getFeedlyAccount();

        FeedEntry getEntry(int position);
        void updateView(int position);

        void actionModeFinished();
    }

    public FeedEntryListActionMode(View view, int position, FeedEntryListFragment fragment) {
        selectedView = view;
        selectedPosition = position;

        // Make sure that the interface is implemented in the fragment
        try {
            callback = (FeedEntryListActionModeCallbacks) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException("Fragment must implement FeedEntryListActionModeCallbacks");
        }

        entry = callback.getEntry(selectedPosition);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.feed_entry_list, menu);

        if (!entry.isRead()) {
            MenuItem read_unread_toggle = menu.findItem(R.id.action_read_unread_toggle);
            read_unread_toggle.setIcon(R.drawable.ic_action_circle);
            read_unread_toggle.setTitle(R.string.action_mark_as_read);
        }

        if (entry.isFavorite()) {
            MenuItem favorite_toggle = menu.findItem(R.id.action_favorite_toggle);
            favorite_toggle.setIcon(R.drawable.ic_action_star);
            favorite_toggle.setTitle(R.string.action_favorite_remove);
        }

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        // TODO: Make sure that the selection is not lost if out of view in ListView
        selectedView.setSelected(true);

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_mark_as_read_above:

                mode.finish();
                return true;
            case R.id.action_mark_as_read_below:

                mode.finish();
                return true;
            case R.id.action_read_unread_toggle:
                entry.setRead(!entry.isRead());
                callback.getFeedlyAccount().saveFeedEntry(entry);
                callback.updateView(selectedPosition);
                mode.finish();
                return true;
            case R.id.action_favorite_toggle:
                entry.setFavorite(!entry.isFavorite());
                callback.getFeedlyAccount().saveFeedEntry(entry);
                callback.updateView(selectedPosition);
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (selectedView != null) {
            selectedView.setSelected(false);
            selectedView = null;
        }
        callback.actionModeFinished();
    }
}
