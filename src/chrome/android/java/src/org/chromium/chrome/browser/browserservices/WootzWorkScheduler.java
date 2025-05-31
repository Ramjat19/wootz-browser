package org.chromium.chrome.browser.browserservices;

import android.content.Context;
import android.util.Log;
import androidx.work.*;
import java.util.concurrent.TimeUnit;

public class WootzWorkScheduler {
    private static final String TAG = "WootzWorkScheduler";
    private static final String PERIODIC_WORK_NAME = "wootz_periodic_work";
    private static final String ONE_TIME_WORK_NAME = "wootz_onetime_work";
    
    private static WootzWorkScheduler instance;
    private WorkManager workManager;
    
    private WootzWorkScheduler(Context context) {
        workManager = WorkManager.getInstance(context);
    }
    
    public static synchronized WootzWorkScheduler getInstance(Context context) {
        if (instance == null) {
            instance = new WootzWorkScheduler(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Schedule periodic background work (minimum 15 minutes on Android)
     */
    public void schedulePeriodicWork() {
        // Android limits periodic work to minimum 15 minutes
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false) // Allow on low battery
            .setRequiresStorageNotLow(true)
            .build();
            
        PeriodicWorkRequest periodicWork = new PeriodicWorkRequest.Builder(
                WootzBackgroundWorker.class, 
                15, TimeUnit.MINUTES) // Minimum allowed interval
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .addTag("wootz_background")
            .build();
            
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
            periodicWork
        );
        
        Log.d(TAG, "Periodic work scheduled with ID: " + periodicWork.getId());
    }
    
    /**
     * Schedule immediate one-time work
     */
    public void scheduleImmediateWork() {
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();
            
        OneTimeWorkRequest immediateWork = new OneTimeWorkRequest.Builder(WootzBackgroundWorker.class)
            .setConstraints(constraints)
            .addTag("wootz_immediate")
            .build();
            
        workManager.enqueueUniqueWork(
            ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            immediateWork
        );
        
        Log.d(TAG, "Immediate work scheduled with ID: " + immediateWork.getId());
    }
    
    /**
     * Schedule work with custom delay
     */
    public void scheduleDelayedWork(long delayMinutes) {
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();
            
        OneTimeWorkRequest delayedWork = new OneTimeWorkRequest.Builder(WootzBackgroundWorker.class)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag("wootz_delayed")
            .build();
            
        workManager.enqueue(delayedWork);
        Log.d(TAG, "Delayed work scheduled for " + delayMinutes + " minutes");
    }
    
    /**
     * Cancel all scheduled work
     */
    public void cancelAllWork() {
        workManager.cancelUniqueWork(PERIODIC_WORK_NAME);
        workManager.cancelUniqueWork(ONE_TIME_WORK_NAME);
        workManager.cancelAllWorkByTag("wootz_background");
        workManager.cancelAllWorkByTag("wootz_immediate");
        workManager.cancelAllWorkByTag("wootz_delayed");
        Log.d(TAG, "All work cancelled");
    }
    
    /**
     * Get work status
     */
    public void getWorkStatus() {
        // workManager.getWorkInfosForUniqueWork(PERIODIC_WORK_NAME)
        //     .addListener(() -> {
        //         // Handle work status
        //     }, ContextCompat.getMainExecutor(/* context needed */));
    }
    
    /**
     * Check if work is running
     */
    public boolean isWorkScheduled() {
        try {
            return !workManager.getWorkInfosForUniqueWork(PERIODIC_WORK_NAME).get().isEmpty();
        } catch (Exception e) {
            Log.e(TAG, "Error checking work status", e);
            return false;
        }
    }
}