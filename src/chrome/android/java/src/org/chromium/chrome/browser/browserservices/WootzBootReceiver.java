package org.chromium.chrome.browser.browserservices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Boot receiver to restart background work after device reboot or app updates
 */
public class WootzBootReceiver extends BroadcastReceiver {
    private static final String TAG = "WootzBootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received broadcast action: " + action);
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.d(TAG, "Device boot completed - rescheduling background work");
            rescheduleWork(context);
            
        } else if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
                   Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            Log.d(TAG, "Package updated - rescheduling background work");
            rescheduleWork(context);
            
        } else if (Intent.ACTION_PACKAGE_RESTARTED.equals(action)) {
            Log.d(TAG, "Package restarted - rescheduling background work");
            rescheduleWork(context);
        }
    }
    
    private void rescheduleWork(Context context) {
        try {
            // Reschedule work after boot/update
            WootzWorkScheduler scheduler = WootzWorkScheduler.getInstance(context);
            scheduler.schedulePeriodicWork();
            
            // Optionally schedule immediate work
            scheduler.scheduleImmediateWork();
            
            Log.d(TAG, "Background work successfully rescheduled");
            
        } catch (Exception e) {
            Log.e(TAG, "Error rescheduling background work", e);
        }
    }
}