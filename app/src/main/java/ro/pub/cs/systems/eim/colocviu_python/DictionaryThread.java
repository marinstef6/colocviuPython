package ro.pub.cs.systems.eim.colocviu_python;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class DictionaryThread extends Thread {

    private final Context context;
    private final String word;

    public DictionaryThread(Context context, String word) {
        this.context = context.getApplicationContext();
        this.word = word;
    }

    @Override
    public void run() {
        try {
            String enc = URLEncoder.encode(word, "UTF-8");
            String urlStr = String.format(Constants.URL_TEMPLATE, enc);

            String response = httpGet(urlStr);

            // 3a) Logcat răspuns complet
            Log.i(Constants.TAG, "[FULL RESPONSE] " + response);

            // 3b) extrage prima definiție: [0].meanings[0].definitions[0].definition
            JSONArray root = new JSONArray(response);
            JSONObject firstEntry = root.getJSONObject(0);

            JSONArray meanings = firstEntry.getJSONArray("meanings");
            JSONObject firstMeaning = meanings.getJSONObject(0);

            JSONArray defs = firstMeaning.getJSONArray("definitions");
            JSONObject firstDefObj = defs.getJSONObject(0);

            String definition = firstDefObj.optString("definition", "No definition");

            Log.i(Constants.TAG, "[PARSED DEF] " + definition);

            // cerință: trimite către client DOAR prima definiție
            Intent intent = new Intent(Constants.ACTION_DICT);
            intent.putExtra(Constants.EXTRA_DEF, definition);
            context.sendBroadcast(intent);

        } catch (Exception e) {
            Log.e(Constants.TAG, "Dictionary error: " + e.getMessage());
            Intent intent = new Intent(Constants.ACTION_DICT);
            intent.putExtra(Constants.EXTRA_DEF, "ERROR: " + e.getMessage());
            context.sendBroadcast(intent);
        }
    }

    private static String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        conn.disconnect();
        return sb.toString();
    }
}
