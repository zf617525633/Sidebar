
package co.yoyu.sidebar.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;

/**
 * @ClassName: ApkInfo.java
 * @Description: 通用apk信息处理类
 * @author Melvin
 * @version V1.0
 * @Date 2012-12-4 下午2:30:24
 */
public class ApkUtils {
    private static final int FLAG_EXTERNAL_STORAGE = 262144;

    /**
     * TODO 判断对象是否为空
     * 
     * @author Melvin
     * @date 2013-4-23
     * @return boolean
     */
    private static boolean isNull(Object object) {
        if (object == null || object.equals("")) {
            return true;
        }
        return false;
    }

    /**
     * TODO 获取安装路径
     * 
     * @param context
     * @author Melvin
     * @date 2013-4-23
     * @return String
     */
    public static String getInstallPath(Context context) {
        if (context == null)
            return null;
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo = getAppInfo(pm, context.getPackageName());
        return appInfo.sourceDir;
    }

    /**
     * TODO 获取app信息
     * 
     * @param pm {@link PackageManager}
     * @param packageName
     * @author Melvin
     * @date 2013-4-23
     * @return ApplicationInfo
     */
    public static ApplicationInfo getAppInfo(PackageManager pm, String packageName) {
        ApplicationInfo appInfo = null;
        if (pm == null || isNull(packageName))
            return appInfo;
        try {
            int flag = PackageManager.GET_UNINSTALLED_PACKAGES;
            appInfo = pm.getApplicationInfo(packageName, flag);
        } catch (Exception e) {
        }
        return appInfo;
    }

    /**
     * TODO 根据包名获取程序名
     * 
     * @param context {@link Context}
     * @param packageName
     * @author Melvin
     * @date 2013-4-23
     * @return String
     */
    public static String getAppNameByPackageName(Context context, String packageName) {
        if (context == null || isNull(packageName))
            return null;
        PackageManager pm = context.getPackageManager();
        ApplicationInfo applicationInfo = getAppInfo(pm, packageName);
        if (applicationInfo != null) {
            CharSequence appName = pm.getApplicationLabel(applicationInfo);
            if (appName != null)
                return appName.toString();

        }
        return null;
    }

    /**
     * TODO 获取已安装App ICON
     * 
     * @param context {@link Context}
     * @param packageName
     * @author Melvin
     * @date 2013-5-9
     * @return Drawable
     */
    public static Drawable getAppIcon(Context context, String packageName) {
        if (context == null || isNull(packageName))
            return null;
        PackageManager pm = context.getPackageManager();
        ApplicationInfo applicationInfo = getAppInfo(pm, packageName);
        if (applicationInfo != null) {
            return applicationInfo.loadIcon(pm);

        }
        return null;
    }

    /**
     * TODO 根据包名获取版本名
     * 
     * @param context {@link Context}
     * @param packageName
     * @author Melvin
     * @date 2013-4-23
     * @return String
     */
    public static String getAppVersionName(Context context, String packageName) {
        String versionName = "0.0.0";
        if (context == null) {
            return versionName;
        }
        PackageManager packageManager = context.getPackageManager();
        try {
            if (packageName == null) {
                packageName = context.getPackageName();
            }
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            versionName = packageInfo.versionName;
        } catch (NameNotFoundException e) {
        }

        return versionName;
    }

    /**
     * TODO 根据包名获取版本号
     * 
     * @param context {@link Context}
     * @param packageName 包名 传入null 则取当前包名
     * @author Melvin
     * @date 2013-4-23
     * @return int
     */
    public static int getAppVersionCode(Context context, String packageName) {
        int versionCode = 0;
        if (context == null) {
            return versionCode;
        }
        PackageManager packageManager = context.getPackageManager();
        try {
            if (packageName == null) {
                packageName = context.getPackageName();
            }
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            versionCode = packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            return 0;
        }

        return versionCode;
    }

    /**
     * TODO 获取activity名
     * 
     * @param context {@link Context}
     * @author Melvin
     * @date 2013-4-23
     * @return String
     */
    public static String getActivityName(Context context) {
        if (context == null) {
            return null;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (checkPermissions(context, "android.permission.GET_TASKS")) {
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            return cn.getShortClassName();
        } else {
            return null;
        }

    }

    /**
     * TODO 获取当前运行app包名
     * 
     * @param context {@link Context}
     * @author Melvin
     * @date 2013-4-23
     * @return String
     */
    public static String getPackageName(Context context) {
        if (context == null) {
            return null;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (checkPermissions(context, "android.permission.GET_TASKS")) {
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            return cn.getPackageName();
        } else {
            return null;
        }

    }

    /**
     * TODO 检查权限
     * 
     * @param context {@link Context}
     * @param permission
     * @author Melvin
     * @date 2013-4-23
     * @return boolean
     */
    public static boolean checkPermissions(Context context, String permission) {
        if (context == null || isNull(permission)) {
            return false;
        }
        PackageManager localPackageManager = context.getPackageManager();
        return localPackageManager.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * TODO 获取已安装apk信息
     * 
     * @param context {@link Context}
     * @author Melvin
     * @date 2013-4-23
     * @return String
     *         packageName|versionCode,packageName|versionCode,packageName
     *         |versionCode,packageName|versionCode
     */
    public static String getAllInstalledAppInfo(Context context) {
        if (context == null) {
            return null;
        }
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        StringBuilder installedAppInfo = new StringBuilder();
        boolean first = true;
        for (PackageInfo packageInfo : packageInfos) {
            if (!first) {
                installedAppInfo.append(",");
            }
            installedAppInfo.append(packageInfo.packageName).append("|")
                    .append(packageInfo.versionCode);
            first = false;
        }
        return installedAppInfo.toString();
    }

    /**
     * TODO 获取已安装apk信息,排除预置app
     * 
     * @param context {@link Context}
     * @author Melvin
     * @date 2013-4-23
     * @return String
     *         packageName|versionCode,packageName|versionCode,packageName
     *         |versionCode,packageName|versionCode
     */
    public static String packageInstalledAppInfo(Context context) {
        if (context == null) {
            return null;
        }
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        List<PackageInfo> tempPackageInfos = new ArrayList<PackageInfo>();
        for (PackageInfo packageInfo : packageInfos) {
            // 排除系统安装
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                tempPackageInfos.add(packageInfo);
            }
        }
        StringBuilder installedAppInfo = new StringBuilder();
        boolean first = true;
        for (PackageInfo packageInfo : tempPackageInfos) {
            if (!first) {
                installedAppInfo.append(",");
            }
            installedAppInfo.append(packageInfo.packageName).append("|")
                    .append(packageInfo.versionCode);
            first = false;
        }
        return installedAppInfo.toString();
    }

    /**
     * TODO 判断apk是否没被安装
     * 
     * @param context {@link Context}
     * @param archiveFilePath
     * @author Melvin
     * @date 2013-4-23
     * @return boolean
     */
    public static boolean isUninstalledApk(Context context, String archiveFilePath) {
        if (context == null || isNull(archiveFilePath)) {
            return false;
        }

        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            return true;
        }
        return false;
    }

    /**
     * TODO 获取没有安装的app包名
     * 
     * @param context {@link Context}
     * @param archiveFilePath
     * @author Melvin
     * @date 2013-4-23
     * @return String
     */
    public static String getUninstalledAppPackageName(Context context, String archiveFilePath) {
        if (context == null || isNull(archiveFilePath)) {
            return null;
        }
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
        String packageName = "";
        if (info != null) {
            packageName = info.packageName;
        }
        return packageName;
    }

    /**
     * TODO 获取没有安装的app的版本号
     * 
     * @param context {@link Context}
     * @param archiveFilePath
     * @author Melvin
     * @date 2013-4-23
     * @return int versionCode
     */
    public static int getUninstalledAppVersionCode(Context context, String archiveFilePath) {
        if (context == null || isNull(archiveFilePath)) {
            return 0;
        }
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
        int versionCode = 0;
        if (info != null) {
            versionCode = info.versionCode;
        }
        return versionCode;
    }

    /**
     * TODO 根据包名判断是否安装
     * 
     * @param context {@link Context}
     * @param packageName 包名
     * @author Melvin
     * @date 2013-4-23
     * @return boolean
     */
    public static boolean isInstalledApk(Context context, String packageName) {
        if (context == null || isNull(packageName)) {
            return false;
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            if (packageInfo != null) {
                return true;
            }
        } catch (NameNotFoundException e) {
            return false;
        }
        return false;
    }

    /**
     * TODO 根据包名判断是否安装
     * 
     * @param context {@link Context}
     * @param packageName 包名
     * @author Melvin
     * @date 2013-4-23
     * @return int versioncode
     */
    public static int getInstalledApk(Context context, String packageName) {
        if (context == null || isNull(packageName)) {
            return -1;
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            if (packageInfo != null) {
                return packageInfo.versionCode;
            }
        } catch (NameNotFoundException e) {
            return -1;
        }
        return -1;
    }

    /**
     * TODO 根据包名,版本号判断是否已安装
     * 
     * @param context {@link Context}
     * @param packageName
     * @param versionCode
     * @author Melvin
     * @date 2013-4-23
     * @return boolean
     */
    public static boolean isInstalledApk(Context context, String packageName, String versionCode) {
        if (context == null || isNull(packageName) || isNull(versionCode)) {
            return false;
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            if (packageInfo != null) {
                String v = String.valueOf(packageInfo.versionCode);
                if (versionCode.equals(v))
                    return true;
            }
        } catch (NameNotFoundException e) {
            return false;
        }
        return false;
    }

    /**
     * TODO 根据包名判断是否有activity
     * 
     * @param context {@link Context}
     * @param packageName
     * @author Melvin
     * @date 2013-4-23
     * @return boolean
     */
    public static boolean hasActivities(Context context, String packageName) {
        if (context == null || isNull(packageName)) {
            return false;
        }
        final PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName,
                    PackageManager.GET_ACTIVITIES);
            ActivityInfo activityInfo[] = packageInfo.activities;
            if (activityInfo != null) {
                return true;
            }
        } catch (NameNotFoundException e) {
            return false;
        }
        return false;
    }

    /**
     * TODO 打开已安装的包
     * 
     * @author Melvin
     * @date 2013-4-23
     * @return void
     */
    public static void openInstalledPackage(Context context, String packageName) {
        if (context == null || isNull(packageName)) {
            return;
        }
        final PackageManager packageManager = context.getPackageManager();
        Intent queryIntent = new Intent(Intent.ACTION_MAIN);
        queryIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(queryIntent, 0);
        ActivityInfo activityInfo = null;
        String mainActivityClass = "";
        for (ResolveInfo resolveInfo : resolveInfos) {
            activityInfo = resolveInfo.activityInfo;
            if (activityInfo.packageName.equals(packageName)) {
                mainActivityClass = activityInfo.name;
                break;
            }
        }
        if (!"".equals(mainActivityClass)) {
        	Intent opentIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        	if(null == opentIntent){
        		opentIntent = new Intent();
				opentIntent.setComponent(new ComponentName(packageName, mainActivityClass));
				opentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(opentIntent);
				return;
        	}
        	opentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	context.startActivity(opentIntent); 
        }
    }

    /**
     * TODO 根据包名判断是否在最前面显示
     * 
     * @param context {@link Context}
     * @param packageName
     * @author Melvin
     * @date 2013-4-23
     * @return boolean
     */
    public static boolean isTopActivity(Context context, String packageName) {
        if (context == null || isNull(packageName)) {
            return false;
        }
        int id = context.checkCallingOrSelfPermission(android.Manifest.permission.GET_TASKS);
        if (PackageManager.PERMISSION_GRANTED != id) {
            return false;
        }

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
        if (tasksInfo.size() > 0) {
            if (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 取得包信息， 用|号隔开
     * 
     * @Title: getInstalledApp
     * @param context
     * @return 0： appname, 1 ： pacakge name, 2 :versioncode
     */
    public static String[] getInstalledApp(Context context) {
        if (context == null) {
            return null;
        }
        String[] res = new String[3];

        StringBuffer appnames = new StringBuffer();
        StringBuffer packageNames = new StringBuffer();
        StringBuffer versionCodes = new StringBuffer();
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfos) {
            if (packageInfo.applicationInfo != null) {
                appnames.append(packageInfo.applicationInfo.loadLabel(packageManager)).append("|");
                packageNames.append(packageInfo.packageName).append("|");
                versionCodes.append(packageInfo.versionCode).append("|");
            }
        }
        res[0] = appnames.toString();
        res[1] = packageNames.toString();
        res[2] = versionCodes.toString();
        return res;
    }

    /**
     * TODO 通过路径安装APK
     * 
     * @see
     * @param context
     * @param path
     * @author Melvin
     * @date 2013-4-23
     * @return void
     */
    public static void installPackage(Context context, String path) {
        if (context == null || isNull(path)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(path)),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /** auto将会根据存储空间自适应 **/
    public static final int auto = 0;

    /** 仅放在内存卡上 **/
    public static final int internalOnly = 1;

    /** preferExternal可以优先推荐应用安装到SD卡 **/
    public static final int preferExternal = 2;

    /**
     * TODO 根据包名判断是否可以移动
     * 
     * @param context {@link Context}
     * @param packageName
     * @author Melvin
     * @date 2013-4-23
     * @return int
     */
    public static int getAppinstallLocation(Context context, String packageName) {
        if (context == null || isNull(packageName)) {
            return 1;
        }
        AssetManager am;
        int installLocation = 1;
        try {
            am = context.createPackageContext(packageName, 0).getAssets();
            XmlResourceParser xml = am.openXmlResourceParser("AndroidManifest.xml");
            int eventType = xml.getEventType();
            xmlloop: while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (!xml.getName().matches("manifest")) {
                            break xmlloop;
                        } else {
                            attrloop: for (int j = 0; j < xml.getAttributeCount(); j++) {
                                if (xml.getAttributeName(j).matches("installLocation")) {
                                    switch (Integer.parseInt(xml.getAttributeValue(j))) {
                                        case auto:
                                            installLocation = 0;
                                            break;
                                        case internalOnly:
                                            installLocation = 1;
                                            break;
                                        case preferExternal:
                                            installLocation = 2;
                                            break;
                                    // default:
                                    // installLocation=0;
                                    // break;
                                    }
                                    break attrloop;
                                }
                            }
                        }
                        break;
                }
                eventType = xml.nextToken();
            }
        } catch (NameNotFoundException e) {
        } catch (XmlPullParserException e) {
        } catch (IOException e) {
        } catch (Exception e) {
        }
        return installLocation;
    }

   
    /**
     * TODO 获取所有桌面app
     * 
     * @param context {@link Context}
     * @author Melvin
     * @date 2013-4-23
     * @return List<String>
     */
    public static List<String> getHomes(Context context) {
        if (context == null) {
            return null;
        }
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = context.getPackageManager();
        // 属性
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

    private static String SCHEME = "package";

    /**
     * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.1及之前版本)
     */
    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";

    /**
     * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.2)
     */
    private static final String APP_PKG_NAME_22 = "pkg";

    /**
     * InstalledAppDetails所在包名
     */
    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";

    /**
     * InstalledAppDetails类名
     */
    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

    public static final String ACTION_APPLICATION_DETAILS_SETTINGS = "android.settings.APPLICATION_DETAILS_SETTINGS";

    /**
     * TODO 调用系统InstalledAppDetails界面显示已安装应用程序的详细信息。 对于Android 2.3（Api Level
     * 9）以上，使用SDK提供的接口； 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）。
     * 
     * @param context {@link Context}
     * @param packageName
     * @author Melvin
     * @date 2013-4-23
     * @return void
     */
    public static void showInstalledAppDetails(Context context, String packageName) {
        if (context == null || isNull(packageName)) {
            return;
        }
        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) { // 2.3（ApiLevel 9）以上，使用SDK提供的接口
            intent.setAction(ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else { // 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）
            // 2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22 : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 判断是否安装在SD卡的
     * 
     * @param pm
     * @param packageName
     * @return
     */
    public static boolean isInstallOnSDCard(PackageManager pm, String packageName) {
        ApplicationInfo appInfo;
        try {
            appInfo = pm.getApplicationInfo(packageName, 0);
            if ((appInfo.flags & FLAG_EXTERNAL_STORAGE) != 0) {
                return true;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 卸载应用程序
     * 
     * @param context
     * @param packageName
     */
    public static void unInstallPackage(Context context, String packageName) {
        Uri packageUri = Uri.fromParts("package", packageName, null);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
        uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(uninstallIntent);
    }

    /**
     * 卸载应用程序
     * 
     * @param context
     * @param packageName
     */
    public static void unInstallPackageForResult(Activity activity, String packageName,
            int requestCode) {
        Uri packageUri = Uri.fromParts("package", packageName, null);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
        // 注意:卸载这里不能使用FLAG_ACTIVITY_NEW_TASK 标签，不然广播不能被接收
        // uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(uninstallIntent, requestCode);
    }
}
