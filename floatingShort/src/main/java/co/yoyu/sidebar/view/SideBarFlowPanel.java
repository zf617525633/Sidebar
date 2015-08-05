package co.yoyu.sidebar.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;
import co.yoyu.sidebar.FlowAdapter;
import co.yoyu.sidebar.R;
import co.yoyu.sidebar.db.AppInfoCacheDB;
import co.yoyu.sidebar.db.AppInfoModel;
import co.yoyu.sidebar.drag.DragController;
import co.yoyu.sidebar.drag.DragSource;
import co.yoyu.sidebar.drag.DragView;
import co.yoyu.sidebar.drag.DropTarget;
import co.yoyu.sidebar.utils.Constant;


public class SideBarFlowPanel extends ListView implements OnItemLongClickListener, OnItemClickListener, DragSource, DropTarget {

    private DragController mDragController;
    private float mCellHeight;
    private AppInfoModel mDragInfo;
    private View mDragView;

    private SideBar mSideBar;

    public SideBarFlowPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SideBarFlowPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SideBarFlowPanel(Context context) {
        super(context);
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        setOnItemLongClickListener(this);
        setOnItemClickListener(this);
        mCellHeight = this.getResources().getDimension(R.dimen.workspace_cell_height);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        if(mSideBar.getMode() == SideBar.MODE_EDIT) {
            AppInfoModel info = (AppInfoModel)parent.getItemAtPosition(position);
            mDragInfo = info;
            mDragView = view;
            mDragController.startDrag(mDragView, this, mDragInfo, DragController.DRAG_ACTION_MOVE);
        }//end if
        return true;
    }

    @Override
    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        if(source != this) {
            AppInfoModel info = (AppInfoModel)dragInfo;
            int topPosition = getFirstVisiblePosition();
            int cPostion = y + topPosition;
            int pos = (int) ((cPostion-1)/mCellHeight);
            info.isDrag = false;
            ((FlowAdapter)this.getAdapter()).addAndNotify(info, pos+1);
            info.container = Constant.CONTAINER_FLOW_PANEL;
            AppInfoCacheDB.getInstance(getContext()).updateSideBar(info);
        }
    }

    @Override
    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        // TODO Auto-generated method stub
        if(source != this) {
            AppInfoModel info = (AppInfoModel)dragInfo;
            int topPosition = getFirstVisiblePosition();
            int cPostion = y + topPosition;
            int pos = (int) ((cPostion-1)/mCellHeight);
            ((FlowAdapter)this.getAdapter()).addAndNotify(info, pos+1);
            info.container = Constant.CONTAINER_FLOW_PANEL;
            AppInfoCacheDB.getInstance(getContext()).updateSideBar(info);
        }//end if
    }

    @Override
    public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        AppInfoModel info = (AppInfoModel) dragInfo;
        info.isDrag = true;
        int topPosition = getFirstVisiblePosition();
        int cPostion = y + topPosition;
        int pos = (int) ((cPostion - 1) / mCellHeight);
        ((FlowAdapter) this.getAdapter()).changePosition(pos + 1, info);

    }

    @Override
    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        // TODO Auto-generated method stub
        System.out.println("onDrop");
        if(source!=this){
            AppInfoModel info = (AppInfoModel)dragInfo;
           ((FlowAdapter)this.getAdapter()).removeAndNotify(info);
        }
        
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
//        AppInfoModel info = (AppInfoModel)dragInfo;
//        info.isDrag = false;
//        ((FlowAdapter)this.getAdapter()).notifyDataSetChanged();
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
                ((FlowAdapter)getAdapter()).removeAndNotify(mDragInfo);
                if (mDragView instanceof DropTarget) {
                    mDragController.removeDropTarget((DropTarget)mDragView);
                }
            }
        } else {
            mDragInfo.isDrag = false;
            ((FlowAdapter)getAdapter()).notifyDataSetChanged();//TODO
        }

        mDragInfo = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mSideBar.getMode() == SideBar.MODE_EXPEND) {
            AppInfoModel info = (AppInfoModel) parent.getItemAtPosition(position);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName(info.packageName, info.className);
            try {
                getContext().startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.activity_not_found, Toast.LENGTH_SHORT);
            }
            if(mSideBar.getPosition() == SideBar.POSITION_LEFT_HALF){
                mSideBar.leftExpendToNormal();
            }else if(mSideBar.getPosition() == SideBar.POSITION_RIGHT_HALF){
                mSideBar.rightExpendToNormal();
            }
        }// end if
    }

    public void setSideBar(SideBar sidebar) {
        mSideBar = sidebar;
    }
}
