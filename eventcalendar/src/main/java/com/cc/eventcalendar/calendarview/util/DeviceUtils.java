package com.cc.eventcalendar.calendarview.util;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

@SuppressLint("NewApi")
public class DeviceUtils {
    private DeviceUtils() {
        // Avoid construct outside.
    }

    private static String LOG_TAG = DeviceUtils.class.getSimpleName();

    /**
     * The user-visible version string. E.g., "1.0" or "3.4b5".
     *
     * @return
     */
    public static String getOS() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * The end-user-visible name for the end product.
     *
     * @return
     */
    public static String getDeviceModel() {

        return android.os.Build.MODEL;
    }

    /**
     * The user-visible SDK version of the framework in its raw String
     * representation.
     *
     * @return
     */
    public static String getSDKVersion() {
        return String.valueOf(android.os.Build.VERSION.SDK_INT);
    }

    /**
     * Get the network status string. if network is not available return
     * "Network is off", otherwise return the string like: "Network type:Wifi".
     */
    public static String getNetworkStatus(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            return "Network is off";
        }

        return "Network type:" + activeNetworkInfo.getTypeName();
    }

    public static String getIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.d(LOG_TAG, ex.toString());
        }
        return "";
    }

    /**
     * get the size of the screen.
     */
    public static DisplayMetrics getScreenSize(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static Bitmap resizeImageBitmap(Bitmap bm, int size, boolean isWidth) {
        if (bm == null) {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        if (isWidth) {
            height = (int) (((float) height * size) / width);
            width = size;

        } else {
            width = (int) (((float) width * size) / height);
            height = size;
        }

        return Bitmap.createScaledBitmap(bm, width, height, true);
    }
}
