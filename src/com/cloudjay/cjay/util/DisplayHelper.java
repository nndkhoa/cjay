package com.cloudjay.cjay.util;

/**
 * @author tieubao
 */

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class DisplayHelper {

	/**
	 * This method converts density pixels into its equivalent pixels
	 * 
	 * @param ctx
	 * @param dp
	 *            number to convert to pixels
	 * @return number of pixels
	 */
	public static int dpToPx(Context ctx, int dp) {

		Resources r = ctx.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				r.getDisplayMetrics());

		return (int) px;
	}

	/**
	 * This method converts pixels into its equivalent density pixels
	 * 
	 * @param ctx
	 * @param px
	 *            number to convert to density pixels
	 * @return number of density pixels
	 */
	public static int pxToDp(Context ctx, int px) {

		DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
		int dp = Math.round(px
				/ (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));

		return dp;
	}

	/**
	 * This method returns screen width of a device
	 * 
	 * @param ctx
	 * @return screen width
	 */
	@SuppressWarnings("deprecation")
	public static int getScreenWidth(Context ctx) {

		int width;

		WindowManager wm = (WindowManager) ctx
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {

			Point size = new Point();
			display.getSize(size);

			width = size.x;

		} else {

			width = display.getWidth();

		}

		return width;
	}

	/**
	 * This method returns screen height of a device
	 * 
	 * @param ctx
	 * @return screen height
	 */

	@SuppressWarnings("deprecation")
	public static int getScreenHeight(Context ctx) {

		int height;

		WindowManager wm = (WindowManager) ctx
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {

			Point size = new Point();
			display.getSize(size);

			height = size.y;

		} else {

			height = display.getHeight();

		}

		return height;

	}

	public static void showSoftwareKeyboard(Context context, EditText editext) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(editext, InputMethodManager.SHOW_IMPLICIT);
	}

	public static void dismissSoftwareKeyboard(Context context,
			EditText editText) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	public static void showProgressDialog(Context context,
			ProgressDialog progressDialog, String message) {

		if (progressDialog == null) {
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage(message);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setCancelable(false);
		}

		progressDialog.show();
	}

	public static void dismissProgressDialog(ProgressDialog progressDialog) {

		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
}