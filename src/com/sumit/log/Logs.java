package com.sumit.log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

/**
 * custom log class that show notification for developers only
 * 
 * @code LOG.d(tag,msg);
 * 
 */
public final class Logs {

	public final String TAG = getClass().getSimpleName();

	// private static android.util.Log LOG;
	public static final int VERBOSE = android.util.Log.VERBOSE;
	public static final int DEBUG = android.util.Log.DEBUG;
	public static final int INFO = android.util.Log.INFO;
	public static final int WARN = android.util.Log.WARN;
	public static final int ERROR = android.util.Log.ERROR;
	public static final int ASSERT = android.util.Log.ASSERT;

	public static int VERBOSE_COLOR = Color.BLACK;
	public static int DEBUG_COLOR = Color.BLUE;
	public static int INFO_COLOR = Color.GREEN;
	public static int WARN_COLOR = Color.MAGENTA;
	public static int ERROR_COLOR = Color.RED;
	public static int ASSERT_COLOR = Color.RED;

	private static final int MAX_BUFFER_SIZE = 1000;
	private static final int NOTIFICATION_ID = 1010;
	private static final String PREFS_NAME = "prefs";
	private static final String PREF_LEVEL = "level";
	private static final String PREF_FILTER = "filter";

	private static final Logs INSTANCE = new Logs();

	private Context mContext;
	private NotificationManager mNotificationManager;
	private SharedPreferences mPrefs;
	private int mIcon;
	private String mLabel;
	private PendingIntent mViewIntent;
	private PendingIntent mFilterIntent;
	private PendingIntent mLevelIntent;
	private PendingIntent mClearIntent;
	private boolean mActivityIntegrationAvailable;

	private int mLevel;
	private HashSet<String> mFilterOptions = new HashSet<String>();
	private String mFilter;
	private ArrayList<LogEntry> mEntries = new ArrayList<LogEntry>();

	private Logs() {
	}

	@Override
	protected void finalize() throws Throwable {
		// attempt to cancel notification
		if (mNotificationManager != null) {
			mNotificationManager.cancel(NOTIFICATION_ID);
		}
		super.finalize();
	}

	public static void initialize(Context context) {
		INSTANCE.mContext = context;
		INSTANCE.mIcon = R.drawable.ic_launcher;
		INSTANCE.mLabel = context.getString(context.getApplicationInfo().labelRes);

		INSTANCE.mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		INSTANCE.mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

		INSTANCE.mLevel = INSTANCE.mPrefs.getInt(PREF_LEVEL, VERBOSE);
		INSTANCE.mFilter = INSTANCE.mPrefs.getString(PREF_FILTER, null);

		Intent intent = new Intent(context, LogActivity.class);
		INSTANCE.mViewIntent = PendingIntent.getActivity(context, 0, intent, 0);

		Intent filterIntent = new Intent(context, LogActivity.class);
		filterIntent.putExtra(LogActivity.DATA, LogActivity.FILTER_LOG);
		INSTANCE.mFilterIntent = PendingIntent.getActivity(context, 4, filterIntent, 0);

		Intent levelIntent = new Intent(context, LogActivity.class);
		levelIntent.putExtra(LogActivity.DATA, LogActivity.LEVEL);
		INSTANCE.mLevelIntent = PendingIntent.getActivity(context, 2, levelIntent, 0);

		Intent clearIntent = new Intent(context, LogActivity.class);
		clearIntent.putExtra(LogActivity.DATA, LogActivity.CLEAR_LOG);
		INSTANCE.mClearIntent = PendingIntent.getActivity(context, 3, clearIntent, 0);

		INSTANCE.mActivityIntegrationAvailable = isActivityAvailable(context, LogActivity.class.getName());
	}

	public static String getNotificationFilter() {
		return INSTANCE.mFilter;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static void setNotifactionFilter(String tag) {
		if (INSTANCE.mContext != null) {
			INSTANCE.mFilter = tag;
			INSTANCE.updateNotification();
			Editor editor = INSTANCE.mPrefs.edit();
			editor.putString(PREF_FILTER, tag);
			editor.apply();
		}
	}

	public static int getNotifactionLevel() {
		return INSTANCE.mLevel;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static void setNotifactionLevel(int level) {
		if (INSTANCE.mContext != null) {
			INSTANCE.mLevel = level;
			INSTANCE.updateNotification();
			Editor editor = INSTANCE.mPrefs.edit();
			editor.putInt(PREF_LEVEL, level);
			editor.apply();
		}
	}

	public static ArrayList<LogEntry> getLogBuffer() {
		return new ArrayList<LogEntry>(INSTANCE.mEntries);
	}

	public static ArrayList<String> getFilterOptions() {
		return new ArrayList<String>(INSTANCE.mFilterOptions);
	}

	public static void clearLogbuffer() {
		if (INSTANCE.mContext != null) {
			INSTANCE.mEntries = new ArrayList<LogEntry>();
			INSTANCE.mNotificationManager.cancel(NOTIFICATION_ID);
		}
	}

	private void addToEntryBuffer(int level, String tag, String msg) {
		LogEntry entry = new LogEntry(level, System.currentTimeMillis(), tag, msg);
		mEntries.add(0, entry);
		if (mEntries.size() > MAX_BUFFER_SIZE) {
			mEntries.remove(mEntries.size() - 1);
		}
		mFilterOptions.add(tag);
	}

	private synchronized void doNotify(int level, String tag, String msg) {
		addToEntryBuffer(level, tag, msg);
		updateNotification();
		// doToast(msg);
	}

	private void updateNotification() {

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext).setSmallIcon(mIcon)
				.setContentTitle(mLabel);

		if (mActivityIntegrationAvailable) {
			mBuilder.addAction(0, "Filter", mFilterIntent).addAction(0, "Level", mLevelIntent)
					.addAction(0, "Clear", mClearIntent).setContentIntent(mViewIntent);
		}

		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

		int count = 0;
		for (int i = 0; i < mEntries.size(); i++) {
			if ((mLevel == VERBOSE || mLevel == mEntries.get(i).getLevel())
					&& (mFilter == null || mFilter.equals(mEntries.get(i).getTag()))) {
				if (count < 10) {
					if (count == 0)
						mBuilder.setContentText(mEntries.get(i).getText());
					inboxStyle.addLine(mEntries.get(i).getText());
				}
				count++;
			}
		}
		mBuilder.setNumber(count);
		mBuilder.setStyle(inboxStyle);

		// issue the notification
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

	}

	private static boolean isActivityAvailable(Context context, String className) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent();
		final String packageName = context.getApplicationInfo().packageName;
		intent.setClassName(packageName, className);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	public static int getColor(int level) {
		switch (level) {
		case Logs.VERBOSE:
			return getVERBOSE_COLOR();
		case Logs.DEBUG:
			return getDEBUG_COLOR();
		case Logs.INFO:
			return getINFO_COLOR();
		case Logs.WARN:
			return getWARN_COLOR();
		case Logs.ERROR:
			return getERROR_COLOR();
		case Logs.ASSERT:
			return getASSERT_COLOR();
		default:
			return getDEBUG_COLOR();
		}
	}

	public static void v(String tag, String msg) {
		if (INSTANCE.mContext != null) {
			INSTANCE.doNotify(VERBOSE, tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (INSTANCE.mContext != null) {
			INSTANCE.doNotify(DEBUG, tag, msg);
		}
	}

	// public static void i(String msg) {
	// i("Alexxo", msg);
	// }

	public static void i(String tag, String msg) {
		if (INSTANCE.mContext != null) {
			INSTANCE.doNotify(INFO, tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (INSTANCE.mContext != null) {
			INSTANCE.doNotify(WARN, tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (INSTANCE.mContext != null) {
			INSTANCE.doNotify(ERROR, tag, msg);
		}
	}

	public static int getVERBOSE_COLOR() {
		return VERBOSE_COLOR;
	}

	public static int getDEBUG_COLOR() {
		return DEBUG_COLOR;
	}

	public static int getINFO_COLOR() {
		return INFO_COLOR;
	}

	public static int getWARN_COLOR() {
		return WARN_COLOR;
	}

	public static int getERROR_COLOR() {
		return ERROR_COLOR;
	}

	public static int getASSERT_COLOR() {
		return ASSERT_COLOR;
	}

	public static void setVERBOSE_COLOR(int verbose) {
		VERBOSE_COLOR = verbose;
	}

	public static void setDEBUG_COLOR(int debug) {
		DEBUG_COLOR = debug;
	}

	public static void setINFO_COLOR(int info) {
		INFO_COLOR = info;
	}

	public static void setWARN_COLOR(int warn) {
		WARN_COLOR = warn;
	}

	public void setERROR_COLOR(int error) {
		ERROR_COLOR = error;
	}

	public static void setASSERT_COLOR(int aSSERT_COLOR) {
		ASSERT_COLOR = aSSERT_COLOR;
	}
}
