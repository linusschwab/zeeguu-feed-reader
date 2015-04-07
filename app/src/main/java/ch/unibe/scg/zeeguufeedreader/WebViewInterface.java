package ch.unibe.scg.zeeguufeedreader;

import android.app.Activity;
import android.text.Html;
import android.webkit.JavascriptInterface;
import android.widget.TextView;
import android.widget.Toast;

public class WebViewInterface {
    private Activity context;
    private TextView translationBar;

    /** Instantiate the interface and set the context */
    public WebViewInterface(Activity context, TextView translationBar) {
        this.context = context;
        this.translationBar = translationBar;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void updateTranslation(final String selection) {
        // UI changes must be done in the main thread
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                translationBar.setText(Html.fromHtml("<h2>" + selection + "</h2>"));
            }
        });
    }
}
