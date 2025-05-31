package org.chromium.chrome.browser.browserservices;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.Data;
import org.chromium.base.ContextUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WootzBackgroundWorker extends Worker {
    private static final String TAG = "WootzBackgroundWorker";
    private static final String WOOTZ_JOBS_KEY = "Chrome.Wootzapp.Jobs";
    private static final String WOOTZ_RESULTS_KEY = "Chrome.Wootzapp.JobsResult";

    public WootzBackgroundWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Background work started");
        
        try {
            processJobs();
            Log.d(TAG, "Background work completed successfully");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Background work failed", e);
            return Result.retry(); // Will retry with exponential backoff
        }
    }

    private void processJobs() {
        SharedPreferences prefs = ContextUtils.getAppSharedPreferences();
        String jobsJson = prefs.getString(WOOTZ_JOBS_KEY, "[]");
        String resultsJson = prefs.getString(WOOTZ_RESULTS_KEY, "[]");
        
        try {
            JSONArray jobs = new JSONArray(jobsJson);
            JSONArray results = new JSONArray(resultsJson);
            
            // Limit results size (keep last 100 results)
            while (results.length() > 100) {
                results.remove(0);
            }
            
            // Process each URL in jobs list
            for (int i = 0; i < jobs.length(); i++) {
                String url = jobs.getString(i);
                Log.d(TAG, "Processing URL: " + url);
                String response = fetchUrl(url);
                
                JSONObject result = new JSONObject();
                result.put("url", url);
                result.put("timestamp", System.currentTimeMillis());
                result.put("response", response);
                result.put("worker_id", getId().toString());
                
                results.put(result);
            }
            
            // Save results atomically
            prefs.edit()
                .putString(WOOTZ_RESULTS_KEY, results.toString())
                .putLong("last_work_time", System.currentTimeMillis())
                .apply();
                
            Log.d(TAG, "Results saved successfully, processed " + jobs.length() + " jobs");
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing jobs", e);
            throw new RuntimeException("Job processing failed", e);
        }
    }

    private String fetchUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000); // Longer timeout for background work
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "WootzApp/1.0");
            
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "HTTP Error: " + responseCode;
            }
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
            }
            
            return response.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL: " + urlString, e);
            return "Error: " + e.getMessage();
        }
    }
}