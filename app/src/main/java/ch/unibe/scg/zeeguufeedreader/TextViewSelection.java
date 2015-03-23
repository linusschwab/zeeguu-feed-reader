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
    private int min = 0;
    private int max = 0;

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
            translationBar.setText(Html.fromHtml("<h2>" + getSelectedText(true) + "</h2>"));
        }
        super.onSelectionChanged(selStart, selEnd);
    }

    public void setTranslationBar (TextViewSelection translationBar) {
        this.translationBar = translationBar;
    }

    public TextViewSelection getTranslationBar() {
        return translationBar;
    }

    public String getSelectedText(boolean selectCompleteWord) {
        String text = getText().toString();

        if (this.isFocused()) {
            min = Math.max(0, getSelectionStart());
            max = Math.max(0, getSelectionEnd());
        }

        if (selectCompleteWord) {
            // Select complete word if partially selected (to the left side)
            if (min != 0 && min != text.length()) {
                String current = text.substring(min, min + 1);
                String next = text.substring(min - 1, min);

                while (min != 0
                        && !(current.equals(" ") || next.equals(" "))
                        && !(current.equals(newline) || next.equals(newline))) {
                    next = text.substring(min - 1, min);
                    min -= 1;
                }
            }

            // Select complete word if partially selected (to the right side)
            if (max != 0 && max != text.length()) {
                String current = text.substring(max - 1, max);
                String next = text.substring(max, max + 1);

                while (max != text.length()
                        && !(current.equals(" ") || next.equals(" "))
                        && !(current.equals(newline) || next.equals(newline))) {
                    next = text.substring(max, max + 1);
                    max += 1;
                }
            }
        }

        return text.substring(min, max).trim();
    }
}