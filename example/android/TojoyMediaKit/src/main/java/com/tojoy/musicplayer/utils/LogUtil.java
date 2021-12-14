package com.tojoy.musicplayer.utils;

import android.util.Log;

import java.util.List;

/**
 * 通用日志类
 * @author daibin
 */
public class LogUtil {
	public static boolean isOpen = true;
    public static String LOGGER = "Logger";
    public static final void init(String logFile, int level) {
        LogImpl.init(logFile, level);
    }

	public static final void v(String tag, String msg) {
		if(isOpen){
			LogImpl.v(tag, buildMessage(msg));
		}
	}

	public static final void v(String tag, String msg, Throwable thr) {
		if(isOpen){
			LogImpl.v(tag, buildMessage(msg), thr);
		}
	}

	public static final void d(String tag, String msg) {
		if(isOpen){
			LogImpl.d(tag, buildMessage(msg));
		}
	}

	public static final void d(String tag, String msg, Throwable thr) {
		if(isOpen){
			LogImpl.d(tag, buildMessage(msg), thr);
		}
	}

	public static final void i(String tag, String msg) {
		if(isOpen){
			LogImpl.i(tag, buildMessage(msg));
		}
	}

	public static final void i(String tag, String msg, Throwable thr) {
		if(isOpen){
			LogImpl.i(tag, buildMessage(msg), thr);
		}
	}

	public static final void w(String tag, String msg) {
		if(isOpen){
			LogImpl.w(tag, buildMessage(msg));
		}
	}

	public static final void w(String tag, String msg, Throwable thr) {
		if(isOpen){
			LogImpl.w(tag, buildMessage(msg), thr);
		}
	}

	public static final void w(String tag, Throwable thr) {
		if(isOpen){
			LogImpl.w(tag, buildMessage(""), thr);
		}
	}

	public static final void e(String tag, String msg) {
		if(isOpen){
			LogImpl.e(tag, buildMessage(msg));
		}
	}

	public static final void e(String tag, String msg, Throwable thr) {
		if(isOpen){
			LogImpl.e(tag, buildMessage(msg), thr);
		}
	}
	
	public static final void ui(String msg) {
		if(isOpen){
			LogImpl.i("ui", buildMessage(msg));
		}
	}

	public static final void res(String msg) {
		if(isOpen){
			LogImpl.i("RES", buildMessage(msg));
		}
	}

	public static final void audio(String msg) {
		if(isOpen){
			LogImpl.i("AudioRecorder", buildMessage(msg));
		}
	}

	public static String getLogFileName(String cat) {
		return LogImpl.getLogFileName(cat);
	}

	private static String buildMessage(String msg) {
		return msg;
	}



	public static void T() {
		if(isOpen){
			T ("", 2);
		}

	}

	public static void T2() {
		if (isOpen) {
			T ("<---", 3);
			T ("", 2);
		}
	}

	public static void T2(String l) {
		if (isOpen) {
			T (l, 2);
			T ("<---", 3);
		}
	}

	public static void T(String l) {
		if (isOpen) {
			T (l, 2);
		}
	}

	public static void T(long l) {
		if (isOpen) {
			T (Long.toString(l), 2);
		}
	}

	////////////// type
	public static void F() {
		if (isOpen) {
			T ("", 2);
		}
	}

	public static void F2() {
		if (isOpen) {
			T ("", 2);
			T ("<---", 3);
		}
	}

	public static void F2(String l) {
		if (isOpen) {
			T (l, 2);
			T ("<---", 3);
		}
	}

	public static void F(String l) {
		if (isOpen) {
			T (l, 2);
		}
	}

	public static void F(long l) {
		if (isOpen) {
			T (Long.toString(l), 2);
		}
	}

	private static String listToString (List<Long> members) {
		if (members == null) {
			return "";
		}
		StringBuffer toStringBuffer = new StringBuffer ();
		for (Long m : members) {
			toStringBuffer.append(m);
			toStringBuffer.append(" ");
		}
		return toStringBuffer.toString();

	}

	public static void T (List<Long> members) {
		if (isOpen) {
			T (listToString (members), 2);
		}
	}

//	public static void T(AvSession s) {
//		if (s == null) {
//			return;
//		}
//		T ("SessionKey:" + s.GetSessionKey()
//				+ " type:" + s.getSessionType()
//				+ " user:" + s.getUserId()
//				+ " address:" + s.getAddress()
//				+ " members:" + listToString (s.mMemberList)
//				+ " adds:" + listToString (s.mAddedList)
//				, 2);
//	}

	public static void T(String l, int level) {
		if (isOpen) {
			StackTraceElement[] stacks = (new Exception()).getStackTrace();
			if (stacks == null || stacks.length <= level) {
				return;
			}
			StackTraceElement traceElement = stacks[level];
			if (traceElement == null) {
				return;
			}
			String cls = traceElement.getFileName();
			if (cls == null || cls.length() <= 5) {
				return;
			}

			String method = traceElement.getMethodName();
			if (method == null) {
				return;
			}

			StringBuffer toStringBuffer = new StringBuffer("[").append(method).append("]").append(
					" " ).append(l==null ? "" : l);
			String log = toStringBuffer.toString() + " ";
			Log.e(cls.substring(0, cls.length() - 5), log);
		}

	}

	public static boolean isOpen() {
		return isOpen;
	}

	public static void setOpen(boolean isOpen) {
		LogUtil.isOpen = isOpen;
	}



	// 使用Log来显示调试信息,因为log在实现上每个message有4k字符长度限制
	// 所以这里使用自己分节的方式来输出足够长度的message
	public static void show(String TAG, String str) {
		int d = str.length();
//        Log.v(TAG, str);

		if(isOpen){
			if (str.length() > 3000) {
				Log.v(TAG, "sb.length = " + str.length());
				int chunkCount = str.length() / 3000;     // integer division
				for (int i = 0; i <= chunkCount; i++) {
					int max = 3000 * (i + 1);
					if (max >= str.length()) {
						Log.v(TAG, "chunk " + i + " of " + chunkCount + ":" + str.substring(3000 * i, str.length()));
					} else {
						Log.v(TAG, "chunk " + i + " of " + chunkCount + ":" + str.substring(3000 * i, max));
					}
				}
			} else {
				Log.v(TAG, str.toString());
			}
		}
	}
}
