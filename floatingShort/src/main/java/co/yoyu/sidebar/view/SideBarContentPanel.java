package co.yoyu.sidebar.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import co.yoyu.sidebar.ContentAdapter;
import co.yoyu.sidebar.db.AppInfoCacheDB;
import co.yoyu.sidebar.db.AppInfoModel;
import co.yoyu.sidebar.drag.DragController;
import co.yoyu.sidebar.drag.DragSource;
import co.yoyu.sidebar.drag.DragView;
import co.yoyu.sidebar.drag.DropTarget;
import co.yoyu.sidebar.utils.Constant;


public class SideBarContentPanel extends GridView implements OnItemLongClickListener, DragSource, DropTarget {

    private SideBar mSideBar;

    private DragController mDragController;
    
    private AppInfoModel mDragInfo;
    private View mDragView;

    public SideBarContentPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SideBarContentPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SideBarContentPanel(Context context) {
        super(context);
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        setOnItemLongClickListener(this);
    }

    public void setSideBar(SideBar sidebar) {
        mSideBar = sidebar;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        AppInfoModel info = (AppInfoModel)parent.getItemAtPosition(position);
        info.isDrag = true;
        ((ContentAdapter)getAdapter()).notifyDataSetChanged();
        mDragInfo = info;
        mDragView = view;
        mSideBar.setMode(SideBar.MODE_EDIT_DRAG);
        mDragController.startDrag(mDragView, this, mDragInfo, DragController.DRAG_ACTION_MOVE);
        return true;
    }

    @Override
    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        if(source != this) {
            AppInfoModel info = (AppInfoModel)dragInfo;
            info.isDrag = false;
            ((ContentAdapter)this.getAdapter()).addAndNotify(info);
            info.container = Constant.CONTAINER_CONTENT_PANEL;
            AppInfoCacheDB.getInstance(getContext()).updateSideBar(info);
        }//end if
    }

    @Override
    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDragLeave(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        // TODO Auto-generated method stub
        ((ContentAdapter)this.getAdapter()).notifyDataSetChanged();
        if(source == this)
            return false;
        return true;
    }

    @Override
    public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo, Rect recycle) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDragController(DragController dragger) {
        mDragController = dragger;
    }

    @Override
    public void onDropCompleted(View target, boolean success) {
        // TODO Auto-generated method stub

        if (success&&target!=null){
            if (target != this && mDragInfo != null) {
                ((ContentAdapter)getAdapter()).removeAndNotify(mDragInfo);
                if (mDragView instanceof DropTarget) {
                    mDragController.removeDropTarget((DropTarget)mDragView);
                }
            }
        } else {
            //TODO
            mDragInfo.isDrag = false;
            ((ContentAdapter)getAdapter()).notifyDataSetChanged();
        }

        mDragInfo = null;
    }
}
