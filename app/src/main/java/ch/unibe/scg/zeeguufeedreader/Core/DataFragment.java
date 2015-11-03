package ch.unibe.scg.zeeguufeedreader.Core;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import ch.unibe.scg.zeeguufeedreader.Database.DatabaseHelper;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyConnectionManager;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

public class DataFragment extends Fragment {
    // Stored data
    private ArticleRecommender articleRecommender;
    private ZeeguuConnectionManager zeeguuConnectionManager;
    private FeedlyConnectionManager feedlyConnectionManager;
    private DatabaseHelper databaseHelper;

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
        articleRecommender = new ArticleRecommender(getActivity());
        zeeguuConnectionManager = new ZeeguuConnectionManager(getActivity());
        feedlyConnectionManager = new FeedlyConnectionManager(getActivity());
        databaseHelper = getDatabaseHelper();
    }

    public void onRestore(Activity activity) {
        // ArticleRecommender
        if (articleRecommender != null)
            articleRecommender.onRestore(activity);
        else
            articleRecommender = new ArticleRecommender(activity);

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

        // Databasehelper
        databaseHelper = getDatabaseHelper();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /*
		 * Release the database helper when done.
		 */
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    // Getters and Setters
    public ZeeguuConnectionManager getZeeguuConnectionManager() {
        return zeeguuConnectionManager;
    }

    public FeedlyConnectionManager getFeedlyConnectionManager() {
        return feedlyConnectionManager;
    }

    public ArticleRecommender getArticleRecommender() {
        return articleRecommender;
    }

    public DatabaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        }
        return databaseHelper;
    }
}
