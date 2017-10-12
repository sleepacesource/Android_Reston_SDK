package com.restonsdk.demo.util;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ActivityUtil {
    protected static final String TAG = ActivityUtil.class.getSimpleName();

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels; // 屏幕宽度（像素）
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels; // 屏幕高度（像素）
    }

    /**
     * 描述：获取像素密度
     *
     * @return
     */
    public static int getScreenDenisity(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.densityDpi; // 屏幕高度（像素）
    }

    public static Bitmap screenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        return screenShot(view);
    }

    public static Bitmap screenShot(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        if (bitmap == null) {
            return null;
        }

        // 获取状态栏高度
        Rect frame = new Rect();
        view.getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int screenWidth = view.getWidth();
        int screenHeight = view.getHeight();

        Bitmap b = Bitmap.createBitmap(bitmap, 0, statusBarHeight, screenWidth,
                screenHeight - statusBarHeight);
		/*
		 * Bitmap b = Bitmap.createBitmap(bitmap, 0, 0, screenWidth,
		 * screenHeight);
		 */
        view.destroyDrawingCache();

        return b;
    }

    public static void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
                    MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    public static int getListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        int totalHeight = 0;
        if (listAdapter != null) {
            int count = listAdapter.getCount();
            for (int i = 0; i < count; i++) {
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            totalHeight = totalHeight
                    + (listView.getDividerHeight() * (count - 1));
        }
        return totalHeight;
    }

    /**
     * 描述：获取app的版本号versionCode
     *
     * @return
     */
    public static int getAppInfo(Context context) {
        PackageInfo info = null;

        PackageManager manager = context.getPackageManager();

        try {

            info = manager.getPackageInfo(context.getPackageName(), 0);

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return info.versionCode;
    }
    /**
     * <p>判断当前手机信号是否是 GT-N71开头的</p>
     * <p> </p>
     * 2015年10月26日   wenlong
     */
    public static boolean judgePhoneVersion() {
        String version = android.os.Build.MODEL;// 手机型号
        String[] BadVersions = { "GT-N71" };
        for (int i = 0; i < BadVersions.length; i++) {
            String bad = BadVersions[i];
            if (version.startsWith(bad)) {
                return true;
            }
        }

        return false;
    }

    /**
     * <li>判断 activity 生命周期是否结束</li>
     * @param   activity
     * @return true:生命周期未结束       false:生命周期结束了
     */
    public static boolean isActivityAlive(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return false;
        }
        return true;
    }
}




