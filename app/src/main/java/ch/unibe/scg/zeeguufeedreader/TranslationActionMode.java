package ch.unibe.scg.zeeguufeedreader;

import android.app.Activity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class TranslationActionMode implements ActionMode.Callback {

    private TextView mTextView;
    private Activity mActivity;

    public TranslationActionMode(TextView textView, Activity activity) {
        mTextView = textView;
        mActivity = activity;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        // Remove default items
        menu.removeItem(android.R.id.selectAll);
        menu.removeItem(android.R.id.cut);
        //menu.removeItem(android.R.id.copy);

        // Set translation menu
        actionMode.getMenuInflater().inflate(R.menu.translation, menu);
        return true;
    }

    // Called when action mode is first created. The menu supplied
    // will be used to generate action buttons for the action mode
    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        actionMode.setTitle(getSelectedText());
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.action_translate) {
            actionMode.setTitle(getSelectedText());
            return true;
        }

        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {

    }

    private String getSelectedText() {
        int min = 0;
        int max = mTextView.getText().length();

        if (mTextView.isFocused()) {
            final int selectionStart = mTextView.getSelectionStart();
            final int selectionEnd = mTextView.getSelectionEnd();

            min = Math.max(0, Math.min(selectionStart, selectionEnd));
            max = Math.max(0, Math.max(selectionStart, selectionEnd));
        }

        return mTextView.getText().subSequence(min, max).toString().trim();
    }
}
