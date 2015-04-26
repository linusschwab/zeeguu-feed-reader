package ch.unibe.scg.zeeguufeedreader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class ZeeguuAccount {

    private SharedPreferences sharedPref;

    // User Information
    private String email;
    private String password;
    private String sessionID;
    private String languageNative;
    private String languageLearning;

    public ZeeguuAccount(Activity activity) {
        sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        email = sharedPref.getString("pref_zeeguu_username", "");
        password = sharedPref.getString("pref_zeeguu_password", "");
    }

    /*
        Boolean Checks
    */
    // TODO: Write tests!
    public boolean isUserLoggedIn() {
        return !(email == null || email.equals("")) && !(password == null || password.equals(""));
    }

    public boolean isUserInSession() {
        return !(sessionID == null || sessionID.equals(""));
    }

    /*
        Getter and Setter
    */
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getLanguageNative() {
        return languageNative;
    }

    public void setLanguageNative(String languageNative) {
        this.languageNative = languageNative;
    }

    public String getLanguageLearning() {
        return languageLearning;
    }

    public void setLanguageLearning(String languageLearning) {
        this.languageLearning = languageLearning;
    }
}
