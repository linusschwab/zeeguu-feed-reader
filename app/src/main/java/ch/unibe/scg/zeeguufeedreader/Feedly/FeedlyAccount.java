package ch.unibe.scg.zeeguufeedreader.Feedly;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ch.unibe.zeeguulibrary.MyWords.Item;
import ch.unibe.zeeguulibrary.MyWords.MyWordsHeader;
import ch.unibe.zeeguulibrary.MyWords.MyWordsItem;

public class FeedlyAccount {

    private Activity activity;
    private SharedPreferences sharedPref;

    // User Information
    private String email;
    private String authenticationCode;
    private String accessToken;
    private String refreshToken;

    public FeedlyAccount(Activity activity) {
        this.activity = activity;
        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public void onRestore(Activity activity) {
        this.activity = activity;
    }

    /**
     * Save login information in preferences if they are correct (if server sent sessionID)
     */
    public void saveLoginInformation() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("pref_feedly_email", email);
        editor.putString("pref_feedly_authentication_code", authenticationCode);
        editor.apply();
    }

    public void load() {
        email = sharedPref.getString("pref_feedly_email", "");
        authenticationCode = sharedPref.getString("pref_feedly_authentication_code", "");
    }

    public void logout() {
        // Delete variables
        email = "";
        authenticationCode = "";

        // Delete preferences
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("pref_feedly_email", "");
        editor.putString("pref_feedly_authentication_code", "");
        editor.apply();
    }

    // Boolean Checks
    // TODO: Write tests!
    public boolean isUserLoggedIn() {
        return !(authenticationCode == null || authenticationCode.equals(""));
    }

    //public boolean isUserInSession() {
    //    return !(sessionID == null || sessionID.equals(""));
    //}

    public boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Getters and Setters
    public String getAuthenticationCode() {
        return authenticationCode;
    }

    public void setAuthenticationCode(String authenticationCode) {
        this.authenticationCode = authenticationCode;
    }
}