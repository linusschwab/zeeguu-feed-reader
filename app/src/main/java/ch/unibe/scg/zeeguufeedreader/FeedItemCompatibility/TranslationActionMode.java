package ch.unibe.scg.zeeguufeedreader.FeedItemCompatibility;

import android.app.Activity;
import android.text.Html;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import ch.unibe.scg.zeeguufeedreader.R;

/**
 *  Action mode callback to display the translation of selected text and add words to the wordlist.
 */
public class TranslationActionMode implements ActionMode.Callback {

    private TextViewSelection mTextView;
    private TextView translationBar;
    private Activity mActivity;

    public TranslationActionMode(View mainView, Activity activity) {
        mTextView = (TextViewSelection) mainView.findViewById(R.id.feed_item_content);
        translationBar = (TextView) mainView.findViewById(R.id.feed_item_translation);
        mActivity = activity;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        // Remove default items
        menu.removeItem(android.R.id.selectAll);
        menu.removeItem(android.R.id.cut);
        //menu.removeItem(android.R.id.copy);

        // Inflate a menu resource providing context menu items
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.translation_compatibility, menu);

        return true;
    }

    // Called when action mode is first created. The menu supplied
    // will be used to generate action buttons for the action mode
    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        // Only show action mode if selection is not empty
        if (!mTextView.getSelectedText(false).equals("")) {
            translationBar.setVisibility(View.VISIBLE);
            translationBar.setText(Html.fromHtml("<h2>" + mTextView.getSelectedText(true) + "</h2>"));
            return true;
        }
        else {
            mTextView.clearFocus();
            return false;
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_bookmark:
                Toast.makeText(mActivity, "Word saved to your wordlist", Toast.LENGTH_SHORT).show();
                actionMode.finish(); // Action picked, so close the CAB
                return true;
            case R.id.action_context:
                Toast.makeText(mActivity, mTextView.getTranslationContext(), Toast.LENGTH_LONG).show();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        translationBar.setVisibility(View.GONE);
        translationBar.setText("");
    }
}