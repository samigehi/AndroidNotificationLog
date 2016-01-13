package com.sumit.log;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

// custom log class
// @auther Sumeet Kumar
public abstract class LOG {
	// custom log class
	// enable = false for release version
	public static boolean ENABLE = BuildConfig.DEBUG;
	public static boolean NOTYFY = false;
	public static final int VERBOSE = Logs.VERBOSE;
	public static final int DEBUG = Logs.DEBUG;
	public static final int INFO = Logs.INFO;
	public static final int WARN = Logs.WARN;
	public static final int ERROR = Logs.ERROR;
	public static final int ASSERT = Logs.ASSERT;

	public final static String TAG = "APP_NAME";

	public synchronized static void Create(Context context) {
		Logs.initialize(context);
	}

	public static void v(String tag, String msg) {
		message(VERBOSE, tag, msg);
	}

	public static void debug(String msg) {
		message(DEBUG, TAG, msg);
	}

	public static void d(String tag, String msg) {
		message(DEBUG, tag, msg);
	}

	public static void info(String msg) {
		message(INFO, TAG, msg);
	}

	public static void i(String tag, String msg) {
		message(INFO, tag, msg);
	}

	public static void w(String tag, String msg) {
		message(WARN, tag, msg);
	}

	public static void error(String msg) {
		message(ERROR, TAG, msg);
	}

	public static void e(String tag, String msg) {
		message(ERROR, tag, msg);
	}

	public static void toast(Context c, String msg) {
		Toast.makeText(c, msg, 0).show();
	}

	public static void toast(Context c, String tag, String msg) {
		Toast.makeText(c, tag + ": " + msg, 0).show();
	}

	private static void message(int id, String tag, String msg) {
		if (ENABLE) {
			if (NOTYFY) {
				notify(id, tag, msg);
				logcat(id, tag, msg);
				// 1st notify then print log in logcat
			} else
				logcat(id, tag, msg);
		}

	}

	// general logs to show in logcat and also in notification
	// for developers only
	private static void notify(int id, String tag, String msg) {
		if (ENABLE) {
			switch (id) {
			case DEBUG:
				com.sumit.log.Logs.d(tag, msg);
				break;
			case ERROR:
				com.sumit.log.Logs.e(tag, msg);
				break;
			case INFO:
				com.sumit.log.Logs.i(tag, msg);
				break;
			case WARN:
				com.sumit.log.Logs.w(tag, msg);
				break;
			case VERBOSE:
				com.sumit.log.Logs.v(tag, msg);
				break;
			default:
				com.sumit.log.Logs.d(tag, msg);
				break;
			}
		}

	}

	// general logs to show in logcat/debug
	private static void logcat(int id, String tag, String msg) {
		if (ENABLE) {
			switch (id) {
			case DEBUG:
				Log.d(tag, msg);
				break;
			case ERROR:
				Log.e(tag, msg);
				break;
			case INFO:
				Log.i(tag, msg);
				break;
			case WARN:
				Log.w(tag, msg);
				break;
			case VERBOSE:
				Log.v(tag, msg);
				break;
			default:
				Log.d(tag, msg);
				break;
			}
		}

	}
}
