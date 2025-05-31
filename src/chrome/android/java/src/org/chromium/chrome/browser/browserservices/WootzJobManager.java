package org.chromium.chrome.browser.browserservices;

import android.content.SharedPreferences;
import android.util.Log;
import org.chromium.base.ContextUtils;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

public class WootzJobManager {
    private static final String TAG = "WootzJobManager";
    private static final String WOOTZ_JOBS_KEY = "Chrome.Wootzapp.Jobs";
    private static final String WOOTZ_RESULTS_KEY = "Chrome.Wootzapp.JobsResult";
    
    /**
     * Add a URL to the jobs queue
     */
    public static void addJob(String url) {
        SharedPreferences prefs = ContextUtils.getAppSharedPreferences();
        String jobsJson = prefs.getString(WOOTZ_JOBS_KEY, "[]");
        
        try {
            JSONArray jobs = new JSONArray(jobsJson);
            jobs.put(url);
            
            prefs.edit()
                .putString(WOOTZ_JOBS_KEY, jobs.toString())
                .apply();
                
            Log.d(TAG, "Job added: " + url + ", Total jobs: " + jobs.length());
            
        } catch (JSONException e) {
            Log.e(TAG, "Error adding job", e);
        }
    }
    
    /**
     * Remove a URL from the jobs queue
     */
    public static void removeJob(String url) {
        SharedPreferences prefs = ContextUtils.getAppSharedPreferences();
        String jobsJson = prefs.getString(WOOTZ_JOBS_KEY, "[]");
        
        try {
            JSONArray jobs = new JSONArray(jobsJson);
            JSONArray newJobs = new JSONArray();
            
            for (int i = 0; i < jobs.length(); i++) {
                String jobUrl = jobs.getString(i);
                if (!jobUrl.equals(url)) {
                    newJobs.put(jobUrl);
                }
            }
            
            prefs.edit()
                .putString(WOOTZ_JOBS_KEY, newJobs.toString())
                .apply();
                
            Log.d(TAG, "Job removed: " + url);
            
        } catch (JSONException e) {
            Log.e(TAG, "Error removing job", e);
        }
    }
    
    /**
     * Get all jobs
     */
    public static List<String> getAllJobs() {
        SharedPreferences prefs = ContextUtils.getAppSharedPreferences();
        String jobsJson = prefs.getString(WOOTZ_JOBS_KEY, "[]");
        List<String> jobsList = new ArrayList<>();
        
        try {
            JSONArray jobs = new JSONArray(jobsJson);
            for (int i = 0; i < jobs.length(); i++) {
                jobsList.add(jobs.getString(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting jobs", e);
        }
        
        return jobsList;
    }
    
    /**
     * Clear all jobs
     */
    public static void clearAllJobs() {
        SharedPreferences prefs = ContextUtils.getAppSharedPreferences();
        prefs.edit()
            .putString(WOOTZ_JOBS_KEY, "[]")
            .apply();
        Log.d(TAG, "All jobs cleared");
    }
    
    /**
     * Get last work execution time
     */
    public static long getLastWorkTime() {
        SharedPreferences prefs = ContextUtils.getAppSharedPreferences();
        return prefs.getLong("last_work_time", 0);
    }
    
    /**
     * Get results count
     */
    public static int getResultsCount() {
        SharedPreferences prefs = ContextUtils.getAppSharedPreferences();
        String resultsJson = prefs.getString(WOOTZ_RESULTS_KEY, "[]");
        
        try {
            JSONArray results = new JSONArray(resultsJson);
            return results.length();
        } catch (JSONException e) {
            Log.e(TAG, "Error getting results count", e);
            return 0;
        }
    }
}