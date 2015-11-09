package ch.unibe.scg.zeeguufeedreader.Core;

import android.app.Activity;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.HashMap;

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
    private HashMap<Integer, FeedEntry> cachedEntries = new HashMap<>();
    private int unreadCount;

    // Parameters
    private float maxDifficulty = 0.5f;
    private int urlsPerRequest = 20;
    private long requestDelay = 10000;
    private int delayMultiplier = 1;

    // State information
    boolean difficulties_received;
    boolean learnabilities_received;
    boolean contents_received;

    /**
     * Callback interface that must be implemented by the container activity
     */
    public interface ArticleRecommenderCallbacks {
        ZeeguuConnectionManager getZeeguuConnectionManager();
        ZeeguuAccount getZeeguuAccount();
        FeedlyAccount getFeedlyAccount();
        FeedOverviewFragment getFeedOverviewFragment();
        // TODO: Remove
        void displayMessage(String message);
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

    // Get full content
    public void getContentForNewEntries() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                delayMultiplier = 1;
                ArrayList<FeedEntry> entries = callback.getFeedlyAccount().getAllFeedEntries();
                ArrayList<HashMap<String, String>> urls = new ArrayList<>();

                for (FeedEntry entry : entries) {
                    if (entry.getContentFull() == null) {
                        //cachedEntries.put(entry.getId(), entry);
                        HashMap<String, String> url = new HashMap<>(2);
                        url.put("url", entry.getUrl());
                        url.put("id", String.valueOf(entry.getId()));
                        urls.add(url);
                        if (urls.size() == urlsPerRequest) {
                            SystemClock.sleep(requestDelay * delayMultiplier);
                            callback.getZeeguuConnectionManager().getContentFromUrl(urls);
                            urls = new ArrayList<>();
                        }
                    }
                }

                if (urls.size() != 0) {
                    //contents_received = false;
                    SystemClock.sleep(requestDelay * delayMultiplier);
                    callback.getZeeguuConnectionManager().getContentFromUrl(urls);
                }

                if (isActive())
                    calculateScoreForNewEntries();
            }
        });

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public void setContentForEntries(ArrayList<HashMap<String, String>> contents) {
        checkIfServerOverloaded(contents);
        // TODO: Remove
        callback.displayMessage("Received content for " + contents.size() + " URLs. Current delay: "
                                + String.valueOf((requestDelay * delayMultiplier)/1000) + "s");

        for (HashMap<String, String> content : contents) {
            //FeedEntry entry = cachedEntries.get(Integer.parseInt(content.get("id")));
            FeedEntry entry = callback.getFeedlyAccount().getEntryById(Integer.parseInt(content.get("id")));
            if (entry != null) {
                entry.setContentFull(content.get("content"));
                entry.setImage(content.get("image"));
                callback.getFeedlyAccount().saveFeedEntry(entry);
            }
        }
        //contents_received = true;
        //cachedEntries.clear();
    }

    // Calculate scores
    public void calculateScoreForNewEntries() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                ArrayList<FeedEntry> entries = callback.getFeedlyAccount().getAllFeedEntries();
                // TODO: Check feed language instead (does not exist yet)
                String language = callback.getZeeguuAccount().getLanguageLearning();

                ArrayList<HashMap<String, String>> texts = new ArrayList<>();

                // Get scores for all entries
                for (FeedEntry entry : entries) {
                    if ((entry.getDifficulty() == null || entry.getLearnabilityPercentage() == null) &&
                            (entry.getContentFull() != null && !entry.getContentFull().equals(""))) {
                        cachedEntries.put(entry.getId(), entry);
                        HashMap<String, String> text = new HashMap<>(2);
                        if (entry.getContentFull().length() > entry.getContentAsText().length())
                            text.put("content", entry.getContentFull());
                        else
                            text.put("content", entry.getContentAsText());
                        text.put("id", String.valueOf(entry.getId()));
                        texts.add(text);
                    }
                }

                if (texts.size() != 0) {
                    difficulties_received = false;
                    learnabilities_received = false;
                    callback.getZeeguuConnectionManager().getDifficultyForText(language, texts);
                    callback.getZeeguuConnectionManager().getLearnabilityForText(language, texts);
                }
            }
        });

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public void setDifficultyForEntries(ArrayList<HashMap<String, String>> difficulties) {
        for (HashMap<String, String> difficulty : difficulties) {
            FeedEntry entry = cachedEntries.get(Integer.parseInt(difficulty.get("id")));
            entry.setDifficultyAverage(Float.parseFloat(difficulty.get("score_average")));
            entry.setDifficultyMedian(Float.parseFloat(difficulty.get("score_median")));
        }
        difficulties_received = true;
        updateEntries();
    }

    public void setLearnabilityForEntries(ArrayList<HashMap<String, String>> learnabilities) {
        for (HashMap<String, String> learnability : learnabilities) {
            FeedEntry entry = cachedEntries.get(Integer.parseInt(learnability.get("id")));
            entry.setLearnabilityPercentage(Float.parseFloat(learnability.get("score")));
            entry.setLearnabilityCount(Integer.parseInt(learnability.get("count")));
        }
        learnabilities_received = true;
        updateEntries();
    }

    private void updateEntries() {
        // Make sure that both scores were received
        if (difficulties_received && learnabilities_received) {
            // Save entries
            for (FeedEntry entry : cachedEntries.values())
                callback.getFeedlyAccount().saveFeedEntry(entry);
            cachedEntries.clear();

            // Update view
            callback.getFeedlyAccount().updateDefaultCategories();
            if (callback.getFeedOverviewFragment() != null)
                callback.getFeedOverviewFragment().updateUnreadCount();
        }
    }

    // Getter-/Setter
    public float getMaxDifficulty() {
        return maxDifficulty;
    }

    public boolean isActive() {
        return callback.getZeeguuAccount().isUserInSession();
    }

    public boolean isWaitingForResponse() {
        return !difficulties_received || !learnabilities_received || !contents_received;
    }

    private void checkIfServerOverloaded(ArrayList contents) {
        if (contents.isEmpty())
            delayMultiplier += 3;
        else if (contents.size() <= (urlsPerRequest/3))
            delayMultiplier += 2;
        else if (contents.size() <= (urlsPerRequest/2))
            delayMultiplier += 1;
        else
            delayMultiplier = 1;
    }
}
