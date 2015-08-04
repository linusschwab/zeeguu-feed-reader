package ch.unibe.scg.zeeguufeedreader.Core;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyConnectionManager;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

public class DataFragment extends Fragment {
    // Stored data
    private ZeeguuConnectionManager zeeguuConnectionManager;
    private FeedlyConnectionManager feedlyConnectionManager;

    // This method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        zeeguuConnectionManager = new ZeeguuConnectionManager(getActivity());
        feedlyConnectionManager = new FeedlyConnectionManager(getActivity());
    }

    public void onRestore(Activity activity) {
        // ZeeguConnectionManager
        if (zeeguuConnectionManager != null)
            zeeguuConnectionManager.onRestore(activity);
        else
            zeeguuConnectionManager = new ZeeguuConnectionManager(activity);

        // FeedlyConnectionManager
        if (feedlyConnectionManager != null)
            feedlyConnectionManager.onRestore(activity);
        else
            feedlyConnectionManager = new FeedlyConnectionManager(activity);
    }

    // Getters and Setters
    public ZeeguuConnectionManager getZeeguuConnectionManager() {
        return zeeguuConnectionManager;
    }

    public void setZeeguuConnectionManager(ZeeguuConnectionManager zeeguuConnectionManager) {
        this.zeeguuConnectionManager = zeeguuConnectionManager;
    }

    public FeedlyConnectionManager getFeedlyConnectionManager() {
        return feedlyConnectionManager;
    }

    public void setFeedlyConnectionManager(FeedlyConnectionManager feedlyConnectionManager) {
        this.feedlyConnectionManager = feedlyConnectionManager;
    }
}
