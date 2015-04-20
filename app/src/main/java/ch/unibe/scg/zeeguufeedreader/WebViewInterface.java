package ch.unibe.scg.zeeguufeedreader;

import android.app.Activity;
import android.text.Html;
import android.webkit.JavascriptInterface;
import android.widget.TextView;
import android.widget.Toast;

public class WebViewInterface {
    private Activity context;

    /** Instantiate the interface and set the context */
    public WebViewInterface(Activity context) {
        this.context = context;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void updateTranslation(final String selection) {
        MainActivity activity = (MainActivity) context;
        activity.getConnectionManager().getTranslation(selection, "EN", "DE");
    }
}
