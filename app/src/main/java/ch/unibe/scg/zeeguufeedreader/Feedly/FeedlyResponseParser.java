package ch.unibe.scg.zeeguufeedreader.Feedly;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class FeedlyResponseParser {

    /**
     *
     *  @param json:
     *  {
     *      "state": "...",
     *      "plan": "standard",
     *      "access_token": "AQAAF4iTvPam_M4_dWheV_5NUL8E...",
     *      "id": "c805fcbf-3acf-4302-a97e-d82f9d7c897f",
     *      "expires_in": 3920,
     *      "refresh_token": "AQAA7rJ7InAiOjEsImEiOiJmZWVk...",
     *      "token_type": "Bearer"
     *  }
     */
    public static Map<String, String> parseAuthenticationToken(JSONObject json) {
        try {
            String refreshToken = json.getString("refresh_token");

            String accessToken = json.getString("access_token");

            long timestamp = System.currentTimeMillis() / 1000;
            long expiresIn = Long.parseLong(json.getString("expires_in"));
            String expirationTime = Long.toString(timestamp + expiresIn);

            String userId = json.getString("id");

            Map<String, String> response = new HashMap<>();
            response.put("refresh_token", refreshToken);
            response.put("access_token", accessToken);
            response.put("access_token_expiration", expirationTime);
            response.put("user_id", userId);

            return response;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String parseErrorMessage(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject json = new JSONObject(responseBody);

            return json.getString("errorMessage");

        } catch (UnsupportedEncodingException | JSONException e ) {
            e.printStackTrace();
        }

        return "";
    }
}
