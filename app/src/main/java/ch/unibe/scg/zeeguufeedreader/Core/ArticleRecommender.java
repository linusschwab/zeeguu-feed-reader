package ch.unibe.scg.zeeguufeedreader.Core;

import android.app.Activity;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.FeedOverviewFragment;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyAccount;
import ch.unibe.zeeguulibrary.Core.ZeeguuAccount;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

/**
 * Part of the intelligent article recommender. Manages the recommended articles, the main
 * calculation is done on the Zeeguu server.
 */
public class ArticleRecommender {
    private Activity activity;
    private ArticleRecommenderCallbacks callback;

    // Entries
    private ArrayList<FeedEntry> recommendedEntries = new ArrayList<>();
    private int unreadCount;

    // Parameters
    private int numberOfEntries = 100;

    boolean difficulties;
    boolean learnabilities;

    /**
     * Callback interface that must be implemented by the container activity
     */
    public interface ArticleRecommenderCallbacks {
        ZeeguuConnectionManager getZeeguuConnectionManager();
        ZeeguuAccount getZeeguuAccount();
        FeedlyAccount getFeedlyAccount();
        FeedOverviewFragment getFeedOverviewFragment();
    }

    public ArticleRecommender(Activity activity) {
        this.activity = activity;

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ArticleRecommenderCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ArticleRecommenderCallbacks");
        }
    }

    /**
     * Method that must be called after the activity is restored (for example on screen rotation),
     * otherwise the callbacks will still go to the old/destroyed activity!
     */
    public void onRestore(Activity activity) {
        this.activity = activity;
        callback = (ArticleRecommenderCallbacks) activity;
    }

    private void calculateUnreadCount() {
        int unreadCounter = 0;

        for (FeedEntry entry : recommendedEntries) {
            if (!entry.isRead())
                unreadCounter++;
        }

        unreadCount = unreadCounter;
    }

    // Calculate scores
    public void calculateScoreForNewEntries() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                ArrayList<FeedEntry> entries = callback.getFeedlyAccount().getAllFeedEntries();
                // TODO: Check feed language instead (does not exist yet)
                String language = callback.getZeeguuAccount().getLanguageLearning();

                ArrayList<String> texts = new ArrayList<>();
                ArrayList<Integer> ids = new ArrayList<>();

                // Get scores for all entries
                for (FeedEntry entry : entries) {
                    if (entry.getDifficulty() == null || entry.getLearnability() == null) {
                        texts.add(entry.getContentAsText());
                        ids.add(entry.getId());
                    }
                }

                if (texts.size() != 0 && ids.size() != 0) {
                    difficulties = false;
                    learnabilities = false;
                    callback.getZeeguuConnectionManager().getDifficultyForText(language, texts, ids);
                    callback.getZeeguuConnectionManager().getLearnabilityForText(language, texts, ids);
                }
            }
        });

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public void setDifficultyForEntries(ArrayList<Float> scores, ArrayList<Integer> ids) {
        for (int i = 0; i < scores.size(); i++) {
            FeedEntry entry = callback.getFeedlyAccount().getEntryById(ids.get(i));
            entry.setDifficulty(scores.get(i));
            callback.getFeedlyAccount().saveFeedEntry(entry);
        }
        difficulties = true;
        updateEntries();
    }

    public void setLearnabilityForEntries(ArrayList<Float> scores, ArrayList<Integer> ids) {
        for (int i = 0; i < scores.size(); i++) {
            FeedEntry entry = callback.getFeedlyAccount().getEntryById(ids.get(i));
            entry.setLearnability(scores.get(i));
            callback.getFeedlyAccount().saveFeedEntry(entry);
        }
        learnabilities = true;
        updateEntries();
    }

    public void updateEntries() {
        // Make sure that both scores were received
        if (difficulties && learnabilities) {
            callback.getFeedlyAccount().updateDefaultCategories();
            if (callback.getFeedOverviewFragment() != null)
                callback.getFeedOverviewFragment().updateUnreadCount();
        }
    }

    // Getter-/Setter
    public int getEntriesCount() {
        if (recommendedEntries != null)
            return recommendedEntries.size();
        else
            return 0;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public ArrayList<FeedEntry> getRecommendedEntries() {
        return recommendedEntries;
    }

    public void setEntries(ArrayList<FeedEntry> entries) {
        if (entries.size() < numberOfEntries)
            numberOfEntries = entries.size();
        recommendedEntries = new ArrayList<>(entries.subList(0, numberOfEntries));
        calculateUnreadCount();
    }

    public boolean isActive() {
        return callback.getZeeguuAccount().isUserInSession();
    }
}
