package ch.unibe.scg.zeeguufeedreader;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.text.Html;
import android.text.method.LinkMovementMethod;
// import android.view.ActionMode;
// import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FeedItemFragment extends Fragment {

    private TextView mTextView;

    /**
     * The system calls this when creating the fragment. Within your implementation, you should
     * initialize essential components of the fragment that you want to retain when the fragment
     * is paused or stopped, then resumed.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * The system calls this when it's time for the fragment to draw its user interface for the
     * first time. To draw a UI for your fragment, you must return a View from this method that
     * is the root of your fragment's layout. You can return null if the fragment does not
     * provide a UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = (View) inflater.inflate(R.layout.fragment_feed_item, container, false);
        mTextView = (TextView) mainView.findViewById(R.id.feed_item_content);

        // Make links clickable
        mTextView.setMovementMethod(LinkMovementMethod.getInstance());

        // Set content
        mTextView.setText(Html.fromHtml("<h2>Title</h2><br><p>this is <u>underlined</u> text</p> <br><br> <p>this is a <a href=\"http://google.ch\">link</a></p>"));

        // Set custom action mode for the translation
        mTextView.setCustomSelectionActionModeCallback(new TranslationActionMode(mTextView, getActivity()));

        return mainView;
    }

    /**
     * The system calls this method as the first indication that the user is leaving the fragment
     * (though it does not always mean the fragment is being destroyed). This is usually where you
     * should commit any changes that should be persisted beyond the current user session
     * (because the user might not come back).
     */
    @Override
    public void onPause() {
        super.onPause();
    }
}
