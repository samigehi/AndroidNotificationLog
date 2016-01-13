package com.sumit.log;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

//POJO class
public class LogEntry implements Parcelable {

	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat FMT_LOG = new SimpleDateFormat("HH:mm:ss");
	private static final int TAG_LENGTH = 10;

	private int level;
	private long time;
	private String tag;
	private String text;

	public LogEntry(int Level, long Time, String Tag, String Text) {
		level = Level;
		time = Time;
		tag = Tag;
		text = Text;
	}

	protected LogEntry(Parcel in) {
		level = in.readInt();
		time = in.readLong();
		tag = in.readString();
		text = in.readString();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(level);
		dest.writeLong(time);
		dest.writeString(tag);
		dest.writeString(text);
	}

	public static final Parcelable.Creator<LogEntry> CREATOR = new Parcelable.Creator<LogEntry>() {
		public LogEntry createFromParcel(Parcel in) {
			return new LogEntry(in);
		}

		public LogEntry[] newArray(int size) {
			return new LogEntry[size];
		}
	};


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Date d = new Date(time);

		sb.append("<span style='color:" + Logs.getColor(level) + ";'>");
		sb.append(FMT_LOG.format(d));
		sb.append(" ");
		if (tag.length() > TAG_LENGTH) {
			sb.append(tag.substring(0, TAG_LENGTH - 1) + "É");
		} else if (tag.length() < TAG_LENGTH) {
			sb.append(tag + "               ".substring(0, TAG_LENGTH - tag.length()));
		} else {
			sb.append(tag);
		}
		sb.append(" ");
		sb.append(text);
		sb.append("</span><br/>");

		return sb.toString();
	}

	public int getLevel() {
		return level;
	}

	public long getTime() {
		return time;
	}

	public String getTag() {
		return tag;
	}

	public String getText() {
		return text;
	}

}
