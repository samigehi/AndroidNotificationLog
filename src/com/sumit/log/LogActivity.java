package com.sumit.log;

import java.util.ArrayList;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;

// on notification click launch this activity
public class LogActivity extends Activity {

	public static final String DATA = "log_data";

	public static final int VIEW_LOG = 1;
	public static final int FILTER_LOG = 2;
	public static final int CLEAR_LOG = 3;
	public static final int LEVEL = 4;

	WebView webView;
	int action;
	int selected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		webView = new WebView(this);
		setContentView(webView, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		setTitle("Log Activity");
		webView.setWebViewClient(new Callback());

		action = getIntent().getIntExtra(DATA, VIEW_LOG);
		init(true);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		action = intent.getIntExtra(DATA, VIEW_LOG);
		init(false);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuItem item = menu.add(0, FILTER_LOG, 0, "Filter");
		if (Build.VERSION.SDK_INT >= 11) {
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		}

		item = menu.add(0, LEVEL, 0, "Level");
		if (Build.VERSION.SDK_INT >= 11) {
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		}

		item = menu.add(0, CLEAR_LOG, 0, "Clear");
		if (Build.VERSION.SDK_INT >= 11) {
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		action = item.getItemId();
		init(false);
		return true;
	}

	private void init(boolean newInstance) {

		switch (action) {
		case VIEW_LOG:
			updateLogDisplay();
			break;
		case FILTER_LOG:
			showFilterDialog(newInstance);
			break;
		case CLEAR_LOG:
			Logs.clearLogbuffer();
			finish();
			break;
		case LEVEL:
			showLevelDialog(newInstance);
			break;
		default:
			break;
		}

	}

	private void updateLogDisplay() {

		ArrayList<LogEntry> data = Logs.getLogBuffer();

		if (data != null) {
			StringBuilder body = new StringBuilder();
			body.append("<html><head>");
			body.append("</head><body><pre>");
			for (int i = data.size() - 1; i >= 0; i--) {
				LogEntry logEntry = data.get(i);
				if (Logs.getNotifactionLevel() == Logs.VERBOSE || Logs.getNotifactionLevel() == logEntry.getLevel()) {
					if (Logs.getNotificationFilter() == null
							|| Logs.getNotificationFilter().equals(logEntry.getTag())) {
						body.append(logEntry);
					}
				}
			}
			body.append("</pre></body></html>");
			webView.loadDataWithBaseURL("file:///android_asset/", body.toString(), "text/html", "utf-8", null);

		}

	}

	private void showFilterDialog(final boolean finishOnOk) {

		ArrayList<String> t = Logs.getFilterOptions();

		int j = 0;
		selected = 0;
		for (String val : t) {
			if (val.equals(Logs.getNotificationFilter())) {
				selected = j + 1;
			}
			j++;
		}

		t.add(0, "None");
		final String[] tags = t.toArray(new String[t.size()]);

		AlertDialog dlg = new AlertDialog.Builder(this).setTitle("Tag Filter")
				.setSingleChoiceItems(tags, selected, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						selected = which;
					}
				}).setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (selected == 0) {
							Logs.setNotifactionFilter(null);
						} else {
							Logs.setNotifactionFilter(tags[selected]);
						}
						if (finishOnOk) {
							finish();
						} else {
							updateLogDisplay();
						}
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (finishOnOk) {
							finish();
						}
					}
				}).create();

		dlg.show();

	}

	private void showLevelDialog(final boolean finishOnOk) {

		final String[] items = { "Verbose", "Debug", "Info", "Warn", "Error", "Assert" };
		final int[] values = { Logs.VERBOSE, Logs.DEBUG, Logs.INFO, Logs.WARN, Logs.ERROR, Logs.ASSERT };

		int i = 0;
		for (int val : values) {
			if (val == Logs.getNotifactionLevel()) {
				selected = i;
			}
			i++;
		}

		AlertDialog dlg = new AlertDialog.Builder(this).setTitle("Log Level")
				.setSingleChoiceItems(items, selected, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						selected = which;
					}
				}).setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Logs.setNotifactionLevel(values[selected]);
						if (finishOnOk) {
							finish();
						} else {
							updateLogDisplay();
						}
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (finishOnOk) {
							finish();
						}
					}
				}).create();

		dlg.show();

	}

	private class Callback extends WebViewClient {

		private Handler mScrollHandler = new Handler();

		private Runnable mScrollRunner = new Runnable() {
			public void run() {
				webView.pageDown(true);
			}
		};

		@Override
		public void onPageFinished(WebView view, String url) {
			mScrollHandler.removeCallbacks(mScrollRunner);
			mScrollHandler.postDelayed(mScrollRunner, 300);
		}

	}

}
