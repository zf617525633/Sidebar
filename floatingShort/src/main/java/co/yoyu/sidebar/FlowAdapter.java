
package co.yoyu.sidebar;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import co.yoyu.sidebar.R;
import co.yoyu.sidebar.cache.LocalImageCache;
import co.yoyu.sidebar.db.AppInfoModel;


public class FlowAdapter extends BaseAdapter {

    private Context mContext;
    private List<AppInfoModel> activityInfoList;
    private LayoutInflater inflater;
    PackageManager mPackageManager;
    public FlowAdapter(Context context, List<AppInfoModel> activityInfoList) {
        this.mContext = context;
        this.activityInfoList = activityInfoList;
        inflater =LayoutInflater.from(mContext);
        mPackageManager = context.getPackageManager();
        registerListener();
    }

    public List<AppInfoModel> getActivityInfoList(){
        return activityInfoList;
    }
    
    public void add(AppInfoModel app) {
        add(app, -1);
    }

    public void addAndNotify(AppInfoModel app) {
        addAndNotify(app, -1);
    }

    public void add(AppInfoModel app, int index) {
        if(index < 0 || index > activityInfoList.size())
            activityInfoList.add(app);
        else
            activityInfoList.add(index, app);
    }

    public void addAndNotify(AppInfoModel app, int index) {
        add(app, index);
        notifyDataSetChanged();
    }

    public void remove(AppInfoModel app) {
        activityInfoList.remove(app);
    }

    public void removeAndNotify(AppInfoModel app) {
        remove(app);
        notifyDataSetChanged();
    }

    
    /***
     * 注册卸载通知监听
     * 
     * @author Melvin
     * @return void
     */
    private void registerListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        mContext.registerReceiver(installedDataChangeReciever, filter);
    }
    
    private BroadcastReceiver installedDataChangeReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Intent.ACTION_PACKAGE_REMOVED)){
                final String packageName = intent.getData().getSchemeSpecificPart();
                removeAndNotify(packageName);
            }
        }
    };
    
    private void removeAndNotify(String packageName){
        ArrayList<AppInfoModel> appToRemoveList = new ArrayList<AppInfoModel>();
        for(AppInfoModel model:activityInfoList){
            if(model.packageName.equals(packageName)){
                appToRemoveList.add(model);
            }
        }
        for(AppInfoModel model:appToRemoveList){
            activityInfoList.remove(model);
            notifyDataSetChanged();
        }
    }
    
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return activityInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return activityInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder = null;
        final AppInfoModel app = activityInfoList.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.app_square, null);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final CharSequence title = app.title;
        Bitmap bitmap = LocalImageCache.getInstance(mContext).getBitmapFromLocalOrMemory(app.iconkey);
        if(bitmap==null||bitmap.isRecycled()){
            Intent intent = new Intent();
            ComponentName componentName = new ComponentName(app.packageName,app.className);
            intent.setComponent(componentName);
            ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
            BitmapDrawable bitmapDrawable = (BitmapDrawable) resolveInfo.loadIcon(mPackageManager);
            bitmap = bitmapDrawable.getBitmap();
            LocalImageCache.getInstance(mContext).put(app.iconkey, bitmap);
         }
        if (app.isDrag) {
            holder.icon.setImageResource(app.iconInt);
        } else {
            if (bitmap != null) {
                holder.icon.setImageBitmap(bitmap);
            } else {
                holder.icon.setImageResource(R.drawable.ic_launcher);
            }
        }
        
        holder.name.setText(title);
        return convertView;
    }

    class ViewHolder {
        ImageView icon;
        TextView name;
    }
    
    public void onDestory(){
        unRegisterListener();
    }
    
    private void unRegisterListener() {
        mContext.unregisterReceiver(installedDataChangeReciever);
    }
    
    

    public void chageLoacal() {
        // TODO Auto-generated method stub
        for(int i = 0;i<activityInfoList.size();i++){
            AppInfoModel app = activityInfoList.get(i);
            Intent intent = new Intent();
            ComponentName componentName = new ComponentName(app.packageName,app.className);
            intent.setComponent(componentName);
            ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);  
            app.title = resolveInfo.loadLabel(mPackageManager).toString();
            notifyDataSetChanged();
        }
    }

    public void changePosition(int i, AppInfoModel info) {
        // TODO Auto-generated method stub
        activityInfoList.remove(info);
        if(i>activityInfoList.size()){
            activityInfoList.add(info);
        }else{
            activityInfoList.add(i, info);
        }
        notifyDataSetChanged();
    }
    
    public void notifyAllDataChange(){
        for(int i = 0;i<activityInfoList.size();i++){
            AppInfoModel app = activityInfoList.get(i);
            app.isDrag = false;
            notifyDataSetChanged();
        }
    }
}
