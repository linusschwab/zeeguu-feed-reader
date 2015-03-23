package ch.unibe.scg.zeeguufeedreader;

import android.app.Activity;
import android.text.Html;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class TranslationActionMode implements ActionMode.Callback {

    private TextViewSelection mTextView;
    private TextViewSelection translationBar;
    private Activity mActivity;

    public TranslationActionMode(View mainView, Activity activity) {
        mTextView = (TextViewSelection) mainView.findViewById(R.id.feed_item_content);
        translationBar = (TextViewSelection) mainView.findViewById(R.id.feed_item_translation);
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
        inflater.inflate(R.menu.translation, menu);

        return true;
    }

    // Called when action mode is first created. The menu supplied
    // will be used to generate action buttons for the action mode
    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        translationBar.setVisibility(View.VISIBLE);
        translationBar.setText(Html.fromHtml("<h2>" + mTextView.getSelectedText() + "</h2>"));
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_bookmark:
                Toast.makeText(mActivity, "Word saved to your wordlist", Toast.LENGTH_SHORT).show();
                actionMode.finish(); // Action picked, so close the CAB
                return true;
//              case R.id.action_translate:
//              Toast.makeText(mActivity, "Translation: " + mTextView.getSelectedText(), Toast.LENGTH_LONG).show();
//              return true;
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