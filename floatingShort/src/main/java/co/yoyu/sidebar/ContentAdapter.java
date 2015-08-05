
package co.yoyu.sidebar;

import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import co.yoyu.sidebar.R;
import co.yoyu.sidebar.cache.LocalImageCache;
import co.yoyu.sidebar.db.AppInfoModel;
import co.yoyu.sidebar.view.SideBar;


public class ContentAdapter extends ArrayAdapter<AppInfoModel> {
    class ViewHolder {
        ImageView icon;
        TextView name;
        int position;
    }

    LayoutInflater mInflater;
    PackageManager mPackageManager;
    int mTextViewResourceId;
    private SideBar mSideBar;
    public ContentAdapter(SideBar context, int textViewResourceId, List<AppInfoModel> objects) {
        super(context, textViewResourceId, objects);
        mSideBar = context;
        mInflater = LayoutInflater.from(context);
        mPackageManager = context.getPackageManager();
        mTextViewResourceId = textViewResourceId;
    }

    public void add(AppInfoModel app) {
        add(app, -1);
    }

    public void addAndNotify(AppInfoModel app) {
        addAndNotify(app, -1);
    }

    public void add(AppInfoModel app, int index) {
        if(index < 0 || index > super.getCount())
            super.add(app);
        else
            super.insert(app, index);
    }

    public void addAndNotify(AppInfoModel app, int index) {
        add(app, index);
        notifyDataSetChanged();
    }

    public void remove(AppInfoModel app) {
        super.remove(app);
    }

    public void removeAndNotify(AppInfoModel app) {
        remove(app);
        mSideBar.notifyDataSize(this.getCount());
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final AppInfoModel app = getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(mTextViewResourceId, parent, false);
            holder = new ViewHolder();

            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.name = (TextView) convertView.findViewById(R.id.name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final CharSequence label = app.title;
        Bitmap drawable = LocalImageCache.getInstance(mSideBar).getBitmapFromLocalOrMemory(app.iconkey);
        if(drawable==null||drawable.isRecycled()){
           Intent intent = new Intent();
           ComponentName componentName = new ComponentName(app.packageName,app.className);
           intent.setComponent(componentName);
           ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
           BitmapDrawable bitmapDrawable = (BitmapDrawable) resolveInfo.loadIcon(mPackageManager);
           drawable = bitmapDrawable.getBitmap();
           LocalImageCache.getInstance(mSideBar).put(app.iconkey, drawable);
        }
        holder.name.setText(label);
        if (app.isDrag) {
            holder.icon.setImageResource(app.iconInt);
        } else {
            if (drawable != null)
                holder.icon.setImageBitmap(drawable);
        }
        return convertView;
    }

    public void chageLocal() {
        // TODO Auto-generated method stub
        for(int i = 0;i<getCount();i++){
            AppInfoModel app = getItem(i);
            Intent intent = new Intent();
            ComponentName componentName = new ComponentName(app.packageName,app.className);
            intent.setComponent(componentName);
            ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
            app.title = resolveInfo.loadLabel(mPackageManager).toString();
            notifyDataSetChanged();
        }
    }
}
