package co.yoyu.sidebar.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class ParseUtils {
    public static List<ResolveInfo> getAllApps(Context context) {
        List<ResolveInfo> apps = new ArrayList<ResolveInfo>();
        PackageManager pManager = context.getPackageManager();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN,
                null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        //获取手机内所有应用
        List<ResolveInfo>  paklist = pManager.queryIntentActivities(mainIntent, 0);
        for (int i = 0; i < paklist.size(); i++) {
            ResolveInfo pak = (ResolveInfo) paklist.get(i);
         //判断是否为非系统预装的应用程序
         if (!((pak.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0)) {
             
          apps.add(pak);
         }
        }
        return apps;
    }
}
