package org.chromium.chrome.browser;

import org.chromium.chrome.browser.base.SplitChromeApplication;
import android.app.Application;
import android.util.Log;
import androidx.work.Configuration;
import androidx.work.WorkManager;
import org.chromium.chrome.browser.browserservices.WootzWorkScheduler;

public class WootzApplication extends SplitChromeApplication implements Configuration.Provider {
    private static final String TAG = "WootzApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize WorkManager with custom configuration
        WorkManager.initialize(this, getWorkManagerConfiguration());
        
        // Schedule background work on app start
        WootzWorkScheduler scheduler = WootzWorkScheduler.getInstance(this);
        scheduler.schedulePeriodicWork();
        
        Log.d(TAG, "Application created and background work scheduled");
    }
    
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setMaxSchedulerLimit(50)
            .build();
    }
}