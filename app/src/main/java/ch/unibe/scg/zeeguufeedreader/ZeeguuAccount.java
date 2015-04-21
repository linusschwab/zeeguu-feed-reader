package ch.unibe.scg.zeeguufeedreader;

public class ZeeguuAccount {
    // User Information
    private String email = "feed@reader.test";
    private String password = "B22Ddvsqn4YEO9eEetJs";
    private String sessionID;
    private String languageNative;
    private String languageLearning;

    /*
        Boolean Checks
    */
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
