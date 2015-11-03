package ch.unibe.scg.zeeguufeedreader.Core;

import android.app.Activity;
import android.text.Html;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
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

    /**
     * Callback interface that must be implemented by the container activity
     */
    public interface ArticleRecommenderCallbacks {
        ZeeguuConnectionManager getZeeguuConnectionManager();
        ZeeguuAccount getZeeguuAccount();
        FeedlyAccount getFeedlyAccount();
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
        ArrayList<FeedEntry> entries = callback.getFeedlyAccount().getAllFeedEntries();
        // TODO: Check feed language instead (does not exist yet)
        String language = callback.getZeeguuAccount().getLanguageLearning();

        // Get scores for all entries
        for (FeedEntry entry : entries) {
            if (entry.getDifficulty() == null)
                callback.getZeeguuConnectionManager().getDifficultyForText(language, entry.getContentAsText(), entry.getId());
            if (entry.getLearnability() == null)
                callback.getZeeguuConnectionManager().getLearnabilityForText(language, entry.getContentAsText(), entry.getId());
        }
    }

    public void setDifficultyForEntry(float difficulty, int id) {
        FeedEntry entry = callback.getFeedlyAccount().getEntryById(id);
        entry.setDifficulty(difficulty);
        callback.getFeedlyAccount().saveFeedEntry(entry);
    }

    public void setLearnabilityForEntry(float learnability, int id) {
        FeedEntry entry = callback.getFeedlyAccount().getEntryById(id);
        entry.setLearnability(learnability);
        callback.getFeedlyAccount().saveFeedEntry(entry);
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
