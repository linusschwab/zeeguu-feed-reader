package ch.unibe.scg.zeeguufeedreader.Core;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

/**
 * Allows to get the application context in any class (outside of an activity).
 * Note: the application context does not support all context capabilities: https://possiblemobile.com/2013/06/context/
 * See: http://stackoverflow.com/a/4391811
 */
public class ContextManager extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext(){
        return context;
    }
}
