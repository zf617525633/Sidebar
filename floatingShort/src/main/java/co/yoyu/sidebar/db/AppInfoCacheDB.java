package co.yoyu.sidebar.db;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import co.yoyu.sidebar.R;
import co.yoyu.sidebar.cache.LocalImageCache;
import co.yoyu.sidebar.utils.ApkUtils;
import co.yoyu.sidebar.utils.Constant;


public class AppInfoCacheDB {

    private final static String APPINFO_ITEM_CACHE = "appInfoCache";

    private AppInfoDBHelper mAppInfoOpenHelper;

    private static AppInfoCacheDB mAppInfoCacheDB;

    private static final String APPS_DATABASE = "appInfo.db"; //modify by wangsf

    /** 根据type来删除资源 */
    private static final String PACKAGE_SELECTION = "package_name = ? and class_name = ? ";

    public static AppInfoCacheDB getInstance(Context context) {
        if (mAppInfoCacheDB == null)
            mAppInfoCacheDB = new AppInfoCacheDB(context);
        return mAppInfoCacheDB;
    }

    private AppInfoCacheDB(Context context) {
        mAppInfoOpenHelper = new AppInfoDBHelper(context);
    }


    /**
     * 根据module更新db
     * @author zhangf
     * @date 2013-6-9
     * @return void
     */
    public void updateSideBar(AppInfoModel info){
        SQLiteDatabase db = mAppInfoOpenHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("container", info.container);
        db.update(APPINFO_ITEM_CACHE, values, PACKAGE_SELECTION, new String[]{info.packageName, info.className});
    }

    /**
     * 获得所有siderbar的数据
     * 
     * @return
     */
    public ArrayList<AppInfoModel> getAllFlowPanelItems() {
        SQLiteDatabase db = mAppInfoOpenHelper.getReadableDatabase();
        ArrayList<AppInfoModel> lists = new ArrayList<AppInfoModel>();
        Cursor c = db.query(APPINFO_ITEM_CACHE, null, "container = ?", new String[]{Constant.CONTAINER_FLOW_PANEL + ""}, null, null, null);
        AppInfoModel item = null;
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    item = new AppInfoModel();
                    item.title = c.getString(c.getColumnIndex("app_name"));
                    item.iconkey = c.getString(c.getColumnIndex("icon_key"));
                    item.container = c.getInt(c.getColumnIndex("container"));
                    item.packageName = c.getString(c.getColumnIndex("package_name"));
                    item.className = c.getString(c.getColumnIndex("class_name"));
                    lists.add(item);
                }// end if
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }// end if

        if (db != null && db.isOpen()) {
            db.close();
        }// end if
        return lists;
    }


    /**
     * 获取缓存
     * no use
     * @param flag
     * @return
     */
    public ArrayList<AppInfoModel> getAllSideBarItems() {
        SQLiteDatabase db = mAppInfoOpenHelper.getReadableDatabase();
        ArrayList<AppInfoModel> lists = new ArrayList<AppInfoModel>();
        Cursor c = db.query(APPINFO_ITEM_CACHE, null, "container = ?", new String[]{Constant.CONTAINER_FLOW_PANEL + ""}, null, null, null);
        AppInfoModel item = null;
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    item = new AppInfoModel();
                    item.title = c.getString(c.getColumnIndex("app_name"));
                    item.iconkey = c.getString(c.getColumnIndex("icon_key"));
                    item.container = c.getInt(c.getColumnIndex("container"));
                    item.packageName = c.getString(c.getColumnIndex("package_name"));
                    item.className = c.getString(c.getColumnIndex("class_name"));
                    lists.add(item);
                }// end if
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }// end if

        if (db != null && db.isOpen()) {
            db.close();
        }// end if
        return lists;
    }

    /**
     * 取得内容盘里的item
     * @author zhangf
     * @date 2013-6-9
     * @return ArrayList<AppInfoModel>
     */
    public ArrayList<AppInfoModel> getAllContentPanelItems() {
        SQLiteDatabase db = mAppInfoOpenHelper.getReadableDatabase();
        ArrayList<AppInfoModel> lists = new ArrayList<AppInfoModel>();
        Cursor c = db.query(APPINFO_ITEM_CACHE, null, "container = ?", new String[]{Constant.CONTAINER_CONTENT_PANEL + ""}, null, null, null);
        AppInfoModel item = null;
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    item = new AppInfoModel();
                    item.title = c.getString(c.getColumnIndex("app_name"));
                    item.iconkey = c.getString(c.getColumnIndex("icon_key"));
                    item.container = c.getInt(c.getColumnIndex("container"));
                    item.packageName = c.getString(c.getColumnIndex("package_name"));
                    item.className = c.getString(c.getColumnIndex("class_name"));
                    lists.add(item);
                }// end if
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }// end if

        if (db != null && db.isOpen()) {
            db.close();
        }// end if
        return lists;
    }
    

    /**
     * 插入数据库
     */
    public long insertSiderBarItems(AppInfoModel item){
        long result = -1;
        SQLiteDatabase db = mAppInfoOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("app_name", item.title.toString());
        values.put("icon_key", item.iconkey);
        values.put("container", item.container);
        values.put("package_name", item.packageName);
        values.put("class_name", item.className);
        try {
            result = db.insert(APPINFO_ITEM_CACHE, null, values);
        } catch (SQLiteConstraintException e) {
        }

        if (db != null && db.isOpen()) {
            db.close();
        }// end if
        return result;
    }

    /**
     * 删掉所有浮动盘里的item
     * @author zhangf
     * @date 2013-6-9
     * @return void
     */
    public void deleteAllFlowPanelItems(){
          SQLiteDatabase db = mAppInfoOpenHelper.getWritableDatabase();
          db.delete(APPINFO_ITEM_CACHE,  "container = ?", new String[]{Constant.CONTAINER_FLOW_PANEL + ""});
          if (db != null && db.isOpen()) {
                db.close();
          }// end if  
    }



    private static class AppInfoDBHelper extends SQLiteOpenHelper {
        private static final int VERSION = 1;

        private Context mContext;
        /** 
         * 在SQLiteOpenHelper的子类当中，必须有该构造函数 
         * @param context   上下文对象 
         * @param name      数据库名称 
         * @param factory 
         * @param version   当前数据库的版本，值必须是整数并且是递增的状态 
         */  
        public AppInfoDBHelper(Context context, String name, CursorFactory factory,  
                int version) {  
            //必须通过super调用父类当中的构造函数  
            super(context, name, factory, version);
            mContext = context;
        }

        public AppInfoDBHelper(Context context, String name, int version) {
            this(context, name, null, version);
        }

        public AppInfoDBHelper(Context context) {
            this(context, APPS_DATABASE, VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + APPINFO_ITEM_CACHE
                    + "(_id INTEGER PRIMARY KEY," + "icon_key text," + "package_name TEXT," + "class_name TEXT,"
                     + "version_code TEXT," + "container INTEGER,"  + "app_name TEXT" + ", UNIQUE(package_name, class_name))");
            loadFlowData(db);
        }


        /**
         * 解析xml
         * @author zhangf
         * @date 2013-6-9
         * @return void
         */
        private void loadFlowData(SQLiteDatabase db) {
            int count = 0;
            ContentValues values = new ContentValues();
            XmlResourceParser xrp = mContext.getResources().getXml(R.xml.default_sidebar);
            try {
                while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                    if (xrp.getEventType() == XmlResourceParser.START_TAG) {
                        values.clear();
                        String tagName = xrp.getName();// 获取标签的名字
                        if (tagName.equals(Constant.TAG_ICON)) {
                            AppInfoModel dashModel = new AppInfoModel();

                            //package name
                            String packageName = xrp.getAttributeValue(null, Constant.ATTR_PACKAGE_NAME);
                            dashModel.packageName = packageName;

                            //class name
                            String className = xrp.getAttributeValue(null, Constant.ATTR_CLASS_NAME);
                            dashModel.className = className;

                            //version code
                            String versionCode = xrp.getAttributeValue(null, Constant.ATTR_VERSION);
                            dashModel.versionCode = versionCode;

                            //title
                            String title = xrp.getAttributeValue(null, Constant.ATTR_TITLE);
                            dashModel.title = title;

                            dashModel.iconkey = packageName + title;
                            dashModel.container = Constant.CONTAINER_FLOW_PANEL;
                            if (ApkUtils.isInstalledApk(mContext, packageName)) {
                                try {
                                    BitmapDrawable bitmapDrawable = (BitmapDrawable) mContext.getPackageManager().getApplicationIcon(packageName);
                                    Bitmap bitmap = bitmapDrawable.getBitmap();
                                    LocalImageCache.getInstance(mContext).put(dashModel.iconkey, bitmap);

                                } catch (NameNotFoundException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }// end try

                                insertFlowData(db, dashModel);
                            }// end if
                            count++;
                        }

                    }
                    xrp.next();
                }
            } catch (XmlPullParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + APPINFO_ITEM_CACHE);
            onCreate(db);
        }

        /**
         * 从xml解析出来数据插入到浮动盘中
         * @author zhangf
         * @date 2013-6-9
         * @return void
         */
        private void insertFlowData(SQLiteDatabase db, AppInfoModel item) {
                ContentValues values = new ContentValues();
                values.put("app_name", item.title.toString());
                values.put("icon_key", item.iconkey);
                values.put("container", item.container);
                values.put("package_name", item.packageName);
                values.put("class_name", item.className);
            try {
                db.insert(APPINFO_ITEM_CACHE, null, values);
            } catch (SQLiteConstraintException e) {
            }
        }
    }

}
