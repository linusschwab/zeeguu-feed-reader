package ch.unibe.scg.zeeguufeedreader;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewSelection extends TextView {

    private TextViewSelection translationBar;
    private String newline = System.getProperty("line.separator");
    private int tempSelStart = 0;
    private int tempSelEnd = 0;

    public TextViewSelection(Context context) {
        super(context);
    }

    public TextViewSelection(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewSelection(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TextViewSelection(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        // Make sure that the translation only gets changed if the selection changed
        if (translationBar != null && (selStart != tempSelStart || selEnd != tempSelEnd)) {
            tempSelStart = selStart;
            tempSelEnd = selEnd;
            translationBar.setText(Html.fromHtml("<h2>" + getSelectedText() + "</h2>"));
        }
        super.onSelectionChanged(selStart, selEnd);
    }

    public void setTranslationBar (TextViewSelection translationBar) {
        this.translationBar = translationBar;
    }

    public String getSelectedText() {
        String text = getText().toString();

        int min = Math.max(0, getSelectionStart());
        int max = Math.max(0, getSelectionEnd());

        return text.substring(min, max).trim();
    }
}