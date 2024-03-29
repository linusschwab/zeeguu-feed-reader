package ch.unibe.scg.zeeguufeedreader.Preferences;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.unibe.scg.zeeguufeedreader.R;

public class PreferenceCategoryCustom extends PreferenceCategory {
    public PreferenceCategoryCustom(Context context) {
        super(context);
    }

    public PreferenceCategoryCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreferenceCategoryCustom(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTextColor(getContext().getResources().getColor(R.color.accent_color_red));
    }
}