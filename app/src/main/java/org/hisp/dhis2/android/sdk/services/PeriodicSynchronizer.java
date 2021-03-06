/*
 *  Copyright (c) 2015, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis2.android.sdk.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.hisp.dhis2.android.sdk.controllers.Dhis2;
import org.hisp.dhis2.android.sdk.network.managers.NetworkManager;

/**
 *	This class can be activated to periodically synchronize with a DHIS 2 server to fetch newly updated meta data and
 *	data values. 
 */
public class PeriodicSynchronizer extends BroadcastReceiver {

    public static final String CLASS_TAG = "PeriodicSynchronizer";

    private static PeriodicSynchronizer periodicSynchronizer;
    private int currentInterval = 15;

    public static PeriodicSynchronizer getInstance() {
        if(periodicSynchronizer == null) periodicSynchronizer = new PeriodicSynchronizer();
        return periodicSynchronizer;
    }


	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(CLASS_TAG, " onReceive periodique ");
        String serverUrl = Dhis2.getInstance().getServer(context);
        String credentials = Dhis2.getInstance().getCredentials(context);
        Log.d(CLASS_TAG, "serverUrl: " + serverUrl);
        Log.d(CLASS_TAG, "credentials: " + credentials);
        if(serverUrl == null || credentials == null) return;
        NetworkManager.getInstance().setServerUrl(serverUrl);
        NetworkManager.getInstance().setCredentials(credentials);
        if(Dhis2.getInstance().toggle) Dhis2.getInstance().getDataValueController().synchronizeDataValues(context);
        else Dhis2.getInstance().getMetaDataController().synchronizeMetaData(context);
        Dhis2.getInstance().toggle=!Dhis2.getInstance().toggle;
	}

	/**
	 * Activates the PeriodicSynchronizer to run on a schedule time interval
	 * @param context
	 * @param minutes the time in minutes between each time the synchronizer runs.
	 */
	public void ActivatePeriodicSynchronizer(Context context, int minutes) {
		Log.d(CLASS_TAG, "activate periodic synchronizer");
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, PeriodicSynchronizer.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				1000 * 60 * minutes, pi); // Millisec * Second * Minute
	}

	/**
	 * Cancels the PeriodicSynchronizer
	 * @param context
	 */
	public void CancelPeriodicSynchronizer(Context context) {
		Log.d(CLASS_TAG, "cancel periodic synchronizer");
		Intent intent = new Intent(context, PeriodicSynchronizer.class);
		PendingIntent sender = PendingIntent
				.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

    /**
     * Returns the set update interval in minutes
     * @param context
     * @return
     */
    public static int getInterval(Context context) {
        int frequencyIndex = Dhis2.getUpdateFrequency(context);
        int minutes = 15;
        switch (frequencyIndex) {
            case 0: //1 minutes
                minutes = 1;
                break;
            case 1: //15 minutes
                minutes = 15;
                break;
            case 2: //1 hour
                minutes = 1 * 60;
                break;
            case 3:// 1 day
                minutes = 1 * 60 * 24;
                break;
        }
        return minutes;
    }

    /**
     * ReActivates the PeriodicSyncronizer if the time interval has changed.
     * @return
     */
    public static void reActivate(Context context) {
        int interval = getInterval(context);
        if (interval != getInstance().currentInterval) {
            getInstance().CancelPeriodicSynchronizer(context);
            getInstance().ActivatePeriodicSynchronizer(context, interval);
            getInstance().currentInterval = interval;
        }
    }
}