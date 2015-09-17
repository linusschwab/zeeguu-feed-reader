package ch.unibe.scg.zeeguufeedreader.Core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.graphics.Palette;
import android.util.Log;

import java.io.ByteArrayOutputStream;

public class Tools {

    /*
      Display utilities
     */
    public static float pxToDp(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float dpToPx(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    /*
      Image utilities
      See: http://stackoverflow.com/a/7620610
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        return byteArray;
    }

    public static Bitmap byteArrayToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public static int getDominantColor(Bitmap bitmap) {
        try {
            Palette palette = Palette.from(bitmap).generate();
            return palette.getDarkVibrantColor(0);
        }
        catch (IllegalArgumentException e) {
            Log.e("tools_color", e.toString());
        }

        return 0;
    }
}
