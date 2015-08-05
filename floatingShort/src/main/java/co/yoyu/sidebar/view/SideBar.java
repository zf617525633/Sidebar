package co.yoyu.sidebar.view;

import java.util.ArrayList;
import java.util.List;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.Utils;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import co.yoyu.sidebar.ContentAdapter;
import co.yoyu.sidebar.FlowAdapter;
import co.yoyu.sidebar.R;
import co.yoyu.sidebar.cache.LocalImageCache;
import co.yoyu.sidebar.db.AppInfoCacheDB;
import co.yoyu.sidebar.db.AppInfoModel;
import co.yoyu.sidebar.drag.DragController;
import co.yoyu.sidebar.drag.DragLayer;
import co.yoyu.sidebar.handler.HandlerDragController;
import co.yoyu.sidebar.handler.HandlerDragLayer;
import co.yoyu.sidebar.handler.HandlerWindowDragController;
import co.yoyu.sidebar.utils.Constant;
import co.yoyu.sidebar.utils.ParseUtils;


/**
 *
 */
public class SideBar extends StandOutWindow implements OnGestureListener {

    public static final int WINDOW_ID_SIDE_BAR = 1;
    public static final int WINDOW_ID_SIDE_BAR_HANDLER = 2;

    public static final int CODE_SHOW_SIDE_BAR_WINDOW = 6;
    public static final int CODE_SHOW_SIDE_BAR_HANDLER_WINDOW = 7;

    private PackageManager mPackageManager;
    private WindowManager mWindowManager;

    private ArrayList<AppInfoModel> mFlowData = new ArrayList<AppInfoModel>();
    private ArrayList<AppInfoModel> mContentData = new ArrayList<AppInfoModel>();

    private FlowAdapter mFlowAdapter = null;
    private ContentAdapter mContentAdapter = null;

    private LayoutInflater mInflater;

    private SideBarContentPanel mContentPanel;
    private SideBarFlowPanel mFlowPanel;

    private TextView mEditButton;
    private SideBarLinearLayout mSideBarLinearLayout;

    private ImageView mLeftDrag;
    private ImageView mRightDrag;

    private ImageView mWindowLeftDrag;
    private ImageView mWindowRightDrag;

    private View mFlowList;

    private DragLayer mDragLayer;
    private DragController mDragController;
    /**handler in Window body dragLayer*/
    private HandlerDragLayer mHandlerDragLayer;
    /**handler in Window dragLayer*/
    private HandlerDragLayer mWindowHandlerDragLayer;

    private HandlerDragController mWindowHandlerController;
    private HandlerWindowDragController mHandlerWindowController;

    private static final long MSG_DELAY_TIME = 3000L;
    private static final int MSG_LEFT_EXPEND_TO_NORMAL = 1;
    private static final int MSG_RIGHT_EXPEND_TO_NORMAL = 2;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_LEFT_EXPEND_TO_NORMAL:
                    leftExpendToNormal();
                    break;
                case MSG_RIGHT_EXPEND_TO_NORMAL:
                    rightExpendToNormal();
                    break;
            }
        }
    };
    /**WindowId*/
    private int mWindowID;
    /**HandlerWindowId*/
    private int mHandlerWindowID;

    //正常模式
    public static final int MODE_NORMAL = 0;

    //拖拽块模式
    public static final int MODE_DRAG = 1;

    //编辑模式
    public static final int MODE_EDIT = 2;

    //拖拽块展开模式
    public static final int MODE_EXPEND = 3;

    //拖拽块展开且拖拽模式
    public static final int MODE_EXPEND_DRAG = 4;

    //编辑模式 拖拽
    public static final int MODE_EDIT_DRAG = 5;

    private int mMode = MODE_NORMAL;

    //window body view
    private View mWindowBody;
    private View mHandlerWindowBody;

    public static final int POSITION_LEFT_HALF = 0;
    public static final int POSITION_RIGHT_HALF = 1;
    private int mPosition = POSITION_LEFT_HALF;
    private Animation mLeftEditExpanAnimation;
    /**
     * Helper for detecting touch gestures.
     */
    private GestureDetector mGestureDetector;

    public static void showSidebar(Context context) {
        sendData(context, SideBar.class, DISREGARD_ID, CODE_SHOW_SIDE_BAR_WINDOW,null, null, DISREGARD_ID);
    }

    public static void showSideBarHandler(Context context) {
        sendData(context, SideBar.class, DISREGARD_ID, CODE_SHOW_SIDE_BAR_HANDLER_WINDOW,null, null, DISREGARD_ID);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mPackageManager = getPackageManager();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mInflater = LayoutInflater.from(this);
        mDragController = new DragController(this);
        mHandlerWindowController = new HandlerWindowDragController(this);
        mHandlerWindowController.setSideBar(this);
        mGestureDetector = new GestureDetector(this, this);
        mLeftEditExpanAnimation = AnimationUtils.loadAnimation(this, R.anim.left_to_expand);
        registerBroadCastReceiver();

    }

    @Override
    public String getAppName() {
        return "Sidebar";
    }

    @Override
    public int getAppIcon() {
        return R.drawable.ic_launcher;
    }

    public int getPosition(){
        return mPosition;
    }

    
    
    /**
     * create two window attach View
     * one is body view the other is handler view
     * */
    @Override
    public void createAndAttachView(final int id, FrameLayout frame) {
        if(id == WINDOW_ID_SIDE_BAR) {
            mWindowID = id;
            // inflate the Window Body view ,it's invisable by default.
            mWindowBody = (FrameLayout) mInflater.inflate(R.layout.sidebar_frame, frame, true);
            mWindowBody.setOnTouchListener(null);

            mDragLayer = (DragLayer) mWindowBody.findViewById(R.id.drag_layer);
            mDragLayer.setDragController(mDragController);
            mDragLayer.setSideBar(this);


            mSideBarLinearLayout = (SideBarLinearLayout) mWindowBody.findViewById(R.id.sidebar);


            mFlowList = mDragLayer.findViewById(R.id.sidebar_content);

            // setup content panel ui and data
            mContentPanel = (SideBarContentPanel) mSideBarLinearLayout.findViewById(R.id.appgrid);
            mContentPanel.setDragController(mDragController);
            mContentPanel.setSideBar(this);
            loadContentData();
            mContentAdapter = new ContentAdapter(this, R.layout.app_row, mContentData);
            mContentPanel.setAdapter(mContentAdapter);

            // setup flow panel ui and data
            mFlowPanel = (SideBarFlowPanel) mSideBarLinearLayout.findViewById(R.id.flow);
            mFlowPanel.setDragController(mDragController);
            mFlowPanel.setSideBar(this);
            loadFlowData();
            mFlowAdapter = new FlowAdapter(this, mFlowData);
            mFlowPanel.setAdapter(mFlowAdapter);

            mDragController.addDropTarget(mContentPanel);
            mDragController.addDropTarget(mFlowPanel);

            // setup edit button
            mEditButton = (TextView) mSideBarLinearLayout.findViewById(R.id.edit);
            mEditButton.setOnClickListener(mEditClickListener);

            mHandlerDragLayer = (HandlerDragLayer) mWindowBody.findViewById(R.id.handler_drag_layer);
            mHandlerDragLayer.setSideBar(this);
            mHandlerDragLayer.setWindowDragControl(mHandlerWindowController);

            mLeftDrag = (ImageView) mHandlerDragLayer.findViewById(R.id.preview_left);
            mLeftDrag.setOnTouchListener(mDragTouchListener);

            mRightDrag = (ImageView) mHandlerDragLayer.findViewById(R.id.preview_right);
            mRightDrag.setOnTouchListener(mDragTouchListener);

            // asyn load content data
            asynLoadContentData();
        } else {

            mHandlerWindowID = id;
            FrameLayout windowBody = (FrameLayout) mInflater.inflate(R.layout.sidebar_handler, frame, true);
            windowBody.setOnTouchListener(null);

            mWindowHandlerController = new HandlerDragController(this);

            mWindowHandlerDragLayer = (HandlerDragLayer) windowBody.findViewById(R.id.window_handler_drag_layer);
            mWindowHandlerDragLayer.setDragController(mWindowHandlerController);
            mWindowHandlerDragLayer.setSideBar(this);


            mWindowLeftDrag = (ImageView) mWindowHandlerDragLayer.findViewById(R.id.window_preview_left);
            mWindowLeftDrag.setOnTouchListener(mDragTouchListener);

            mWindowRightDrag = (ImageView) mWindowHandlerDragLayer.findViewById(R.id.window_preview_right);
            mWindowRightDrag.setOnTouchListener(mDragTouchListener);

            int margin = getResources().getDimensionPixelSize(R.dimen.sidesar_handler_height);
            mWindowHandlerController.setSideBar(this, margin / 2);
        }
    }

    @Override
    public StandOutLayoutParams getParams(int id, Window window) {
        if(id == mWindowID) {
            return new StandOutLayoutParams(id, StandOutLayoutParams.WRAP_CONTENT, StandOutLayoutParams.MATCH_PARENT, 0, 0);
        }
        int margin = getResources().getDimensionPixelSize(R.dimen.sidesar_handler_height);
        int y = (mWindowManager.getDefaultDisplay().getHeight() - margin) /2;
        return new StandOutLayoutParams(id, StandOutLayoutParams.WRAP_CONTENT, StandOutLayoutParams.WRAP_CONTENT, 0, y);
    }

    @Override
    public int getFlags(int id) {
        return super.getFlags(id) | StandOutFlags.FLAG_ADD_FUNCTIONALITY_DROP_DOWN_DISABLE | StandOutFlags.FLAG_BODY_MOVE_ENABLE;
    }

    @Override
    public void onReceiveData(int id, int requestCode, Bundle data, Class<? extends StandOutWindow> fromCls, int fromId) {
        switch (requestCode) {
            case CODE_SHOW_SIDE_BAR_WINDOW:
//                if(getWindow(WINDOW_ID_SIDE_BAR)!=null){
//                    
//                }else{
                 show(WINDOW_ID_SIDE_BAR);
//                }
                break;
            case CODE_SHOW_SIDE_BAR_HANDLER_WINDOW:
//                if(getWindow(WINDOW_ID_SIDE_BAR_HANDLER)!=null){
//                    
//                }else{
                  show(WINDOW_ID_SIDE_BAR_HANDLER);
//                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouchBody(final int id, final Window window, final View view, MotionEvent event) {
        removeCloseExpendMessage();

        if(id == mWindowID && (event.getAction() == MotionEvent.ACTION_CANCEL
                || event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_OUTSIDE)) {
            doBackOperate();
        }

        if(!Utils.isSet(window.flags, StandOutFlags.FLAG_WINDOW_MOVE_ENABLE)) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            final StandOutLayoutParams params = (StandOutLayoutParams) window.getLayoutParams();
            if (mPosition == POSITION_RIGHT_HALF && params.x < mWindowManager.getDefaultDisplay().getWidth() / 2 - mFlowList.getWidth()) { // not

                mPosition = POSITION_LEFT_HALF;
                changeHandlerPosition(mRightDrag, mLeftDrag);

                mLeftDrag.setVisibility(View.VISIBLE);
                mRightDrag.setVisibility(View.GONE);
                mFlowList.setVisibility(View.VISIBLE);
                mFlowList.setBackgroundResource(R.drawable.bar_left);

                StandOutLayoutParams originalParams = getParams(id, window);
                params.width = originalParams.width;
                params.height = originalParams.height;

                mSideBarLinearLayout.removeAllViews();

                setLeftHandlerDragLayerPosition();
                System.out.println("AddView");
                mSideBarLinearLayout.addView(mFlowList);
                mSideBarLinearLayout.addView(mHandlerDragLayer);
                mSideBarLinearLayout.addView(mContentPanel);

                mContentPanel.setVisibility(View.GONE);

                mSideBarLinearLayout.requestLayout();
                mSideBarLinearLayout.invalidate();
                updateViewLayout(id, params);
            } else if(mPosition == POSITION_LEFT_HALF && params.x >= mWindowManager.getDefaultDisplay().getWidth() / 2 - mFlowList.getWidth()) {

                mPosition = POSITION_RIGHT_HALF;
                changeHandlerPosition(mLeftDrag, mRightDrag);

                mLeftDrag.setVisibility(View.GONE);
                mRightDrag.setVisibility(View.VISIBLE);
                mFlowList.setVisibility(View.VISIBLE);
                mFlowList.setBackgroundResource(R.drawable.bar_right);

                StandOutLayoutParams originalParams = getParams(id, window);
                params.width = originalParams.width;
                params.height = originalParams.height;
                mSideBarLinearLayout.removeAllViews();

                mContentPanel.setVisibility(View.GONE);

                mSideBarLinearLayout.addView(mHandlerDragLayer);
                mSideBarLinearLayout.addView(mContentPanel);
                mSideBarLinearLayout.addView(mFlowList);
                mSideBarLinearLayout.setTopView(mHandlerDragLayer);

                //set handler position
                setRightHandlerDragLayerPosition();

                mSideBarLinearLayout.requestLayout();
                mSideBarLinearLayout.invalidate();
                updateViewLayout(id, params);
            }//end if
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL
                || event.getAction() == MotionEvent.ACTION_UP) {
            doActionUp(id, window);
        }//end if
        return false;
    }

    private void setLeftHandlerDragLayerPosition() {

        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).leftMargin = (int)getResources().getDimensionPixelSize(R.dimen.flow_handler_left_margin);
        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).rightMargin = 0;
    }

    private void setRightHandlerDragLayerPosition() {
        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).leftMargin = 0;
        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).rightMargin = (int)getResources().getDimensionPixelSize(R.dimen.flow_handler_left_margin);
    }

    void doBackOperate() {
        if(mPosition == POSITION_LEFT_HALF && mMode == MODE_EXPEND) {
            leftExpendToNormal();
        } else if(mPosition == POSITION_RIGHT_HALF && mMode == MODE_EXPEND) {
            rightExpendToNormal();
        } 
    }

    private void changeHandlerPosition(ImageView oldView, ImageView newView) {

        FrameLayout.LayoutParams oldParams = (FrameLayout.LayoutParams) oldView.getLayoutParams();
        int topMargin = oldParams.topMargin;

        FrameLayout.LayoutParams newParams = (FrameLayout.LayoutParams) newView.getLayoutParams();
        newParams.topMargin = topMargin;
    }

    /**
     * TODO 拖动结束的时候设置
     * 
     * @author user
     * @date 2013-6-3
     * @return void
     */
    private void doActionUp(final int id, final Window window) {

        getWindow().removeFlag(StandOutFlags.FLAG_WINDOW_MOVE_ENABLE);
        getHandlerWindow().removeFlag(StandOutFlags.FLAG_WINDOW_MOVE_ENABLE);

        final StandOutLayoutParams params = (StandOutLayoutParams) window.getLayoutParams();

        // if touch edge,这个时候是大块的
        if (params.x <= - mFlowList.getWidth()) {
            //TO LEFT NORMAL
            expendDragToLeftNormal(id, params, window);
        } else if (params.x > - mFlowList.getWidth() && params.x <= mWindowManager.getDefaultDisplay().getWidth() / 2- mFlowList.getWidth()) {
            //TO LEFT EXPEND
            expendDragToLeftExpend(id, params, window);
        } else if (params.x > mWindowManager.getDefaultDisplay().getWidth() / 2- mFlowList.getWidth()
                && params.x <= mWindowManager.getDefaultDisplay().getWidth() - mFlowList.getWidth()) {
            //TO RIGHT EXPEND
            expendDragToRightExpend(id, params, window);
        } else {
            //TO RIGHT NORMAL
            expendDragToRightNormal(id, params, window);
        }
    }

    public String getPersistentNotificationMessage(int id) {
        return "Click to close all windows.";
    }

    public Intent getPersistentNotificationIntent(int id) {
        return StandOutWindow.getCloseAllIntent(this, SideBar.class);
    }

    private OnTouchListener mDragTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent ev) {
            if(ev.getAction() == MotionEvent.ACTION_DOWN) {
                mHandlerWindowController.dispatchTouch(ev);
                mWindowHandlerController.dispatchTouch(ev);
            }
            boolean retValue = mGestureDetector.onTouchEvent(ev);
            return retValue;
        }
    };

    private OnClickListener mEditClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            removeCloseExpendMessage();
            if(mContentPanel.isShown()){
                editToExpend();
            } else {
                expendToEdit();
            }//end if
        }
    };

    private void loadFlowData() {
        mFlowData.clear();
        mFlowData = AppInfoCacheDB.getInstance(SideBar.this).getAllFlowPanelItems();
    }

    private void loadContentData() {
        mContentData.clear();
    }

    private void asynLoadContentData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<AppInfoModel> contentPanelItems = AppInfoCacheDB.getInstance(SideBar.this).getAllContentPanelItems();
                if (contentPanelItems.size() > 0) {
                    mContentData.clear();
                    for (AppInfoModel appInfoModel : contentPanelItems) {
                        final AppInfoModel model = appInfoModel;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mContentData.add(model);
                                mContentAdapter.notifyDataSetChanged();
                            }// end run
                        });// end post
                    }// end for
                } else {
                    final List<ResolveInfo> systemInfo = ParseUtils.getAllApps(SideBar.this);
                    for (ResolveInfo resolveApp : systemInfo) {
                        final AppInfoModel appInfoModel = new AppInfoModel();
                        appInfoModel.packageName = resolveApp.activityInfo.packageName;
                        appInfoModel.className = resolveApp.activityInfo.name;
                        appInfoModel.title = resolveApp.loadLabel(mPackageManager).toString();
                        appInfoModel.iconkey = resolveApp.activityInfo.packageName
                                + appInfoModel.title;
                        appInfoModel.container = Constant.CONTAINER_CONTENT_PANEL;
                        long result = AppInfoCacheDB.getInstance(SideBar.this).insertSiderBarItems(appInfoModel);
                        if (result == -1)
                            continue;
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) resolveApp
                                .loadIcon(mPackageManager);
                        LocalImageCache.getInstance(SideBar.this).put(appInfoModel.iconkey,
                                bitmapDrawable.getBitmap());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mContentData.add(appInfoModel);
                                mContentAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                }
          }
        }).start();
    }

    //when destory clear all database data and save new data from listView
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unRigisterBroadcastReceiver();
        if(mFlowAdapter!=null)
           mFlowAdapter.onDestory();
        AppInfoCacheDB.getInstance(this).deleteAllFlowPanelItems();
        if(mFlowPanel!=null){
            ArrayList<AppInfoModel> list = (ArrayList<AppInfoModel>) ((FlowAdapter)mFlowPanel.getAdapter()).getActivityInfoList();
            for(AppInfoModel model:list){
                model.container = Constant.CONTAINER_FLOW_PANEL;
                try {
                    AppInfoCacheDB.getInstance(this).insertSiderBarItems(model);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setMode(int mode) {
        this.mMode = mode;
    }

    public int getMode() {
        return mMode;
    }

    public Window getWindow() {
        return super.getWindow(mWindowID);
    }

    public Window getHandlerWindow() {
        return super.getWindow(mHandlerWindowID);
    }

    public View getWindowBody() {
        return mWindowBody;
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        System.out.println("velocityX="+velocityX);
        if (mMode == MODE_NORMAL) {
            if(mPosition == POSITION_RIGHT_HALF) {
                if(velocityX < 0) {
                    rightNormalToExpend();
                }//end if
            } else {
                if(velocityX > 0) {
                    leftNormalToExpend();
                }//end if
            }//end if
            return true;
        } else if(mMode == MODE_EXPEND) {
            if(mPosition == POSITION_RIGHT_HALF) {
                if(velocityX > 0) {
                    rightExpendToNormal();
                }//end if
            } else {
                if(velocityX < 0) {
                    leftExpendToNormal();
                }//end if
            }//end if
        }//end if
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        if(mMode == MODE_NORMAL) {
            normalToDrag();
        } else if(mMode == MODE_EXPEND) {
            expendToExpendDrag();
        }//end if
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {

        if(mPosition == POSITION_LEFT_HALF) {
            if (mMode == MODE_NORMAL) {
                leftNormalToExpend();
            } else if (mMode == MODE_EXPEND) {
                leftExpendToNormal();
            }//end if
        } else {
            if (mMode == MODE_NORMAL) {
                rightNormalToExpend();
            } else if (mMode == MODE_EXPEND) {
                rightExpendToNormal();
            }
        }//end if
        return true;
    }

    private void expendToEdit() {
        mEditButton.setText(R.string.edit_button_state_ok);
        mHandlerDragLayer.setVisibility(View.GONE);
        if(mPosition == POSITION_RIGHT_HALF){
            mFlowList.setBackgroundResource(R.drawable.bar_right);
            mContentPanel.setVisibility(View.VISIBLE);
            mSideBarLinearLayout.setBackgroundResource(R.drawable.multiwindow_edit_bg);
            StandOutLayoutParams layoutParams =  (StandOutLayoutParams)getWindow(mWindowID).getLayoutParams();
            layoutParams.x = 0;
            layoutParams.y = 0;
            updateViewLayout(mWindowID, layoutParams);
            mMode = MODE_EDIT;
            getWindow(mWindowID).removeFlag(StandOutFlags.FLAG_WINDOW_MOVE_ENABLE);
        } else {
            mFlowList.setBackgroundResource(R.drawable.bar_left);

            mLeftEditExpanAnimation.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation arg0) {
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    mContentPanel.setVisibility(View.VISIBLE);
                    mSideBarLinearLayout.setBackgroundResource(R.drawable.multiwindow_edit_bg);
                    mSideBarLinearLayout.requestLayout();
                    mMode = MODE_EDIT;
                    getWindow(mWindowID).removeFlag(StandOutFlags.FLAG_WINDOW_MOVE_ENABLE);
                }
            });
            mContentPanel.startAnimation(mLeftEditExpanAnimation);
        }
        removeCloseExpendMessage();

    }

    //edit state to expend
    private void editToExpend() {
        mEditButton.setText(R.string.edit_button_state_edit);
        mContentPanel.setVisibility(View.GONE);
        mSideBarLinearLayout.setBackgroundResource(0);
        mMode = MODE_EXPEND;
        mHandlerDragLayer.setVisibility(View.VISIBLE);
        getWindow(mWindowID).addFlag(StandOutFlags.FLAG_WINDOW_MOVE_ENABLE);
        if(mPosition == POSITION_RIGHT_HALF){
            StandOutLayoutParams layoutParams =  (StandOutLayoutParams)getWindow(mWindowID).getLayoutParams();
            layoutParams.y = 0;
            layoutParams.x = mWindowManager.getDefaultDisplay().getWidth() - mFlowList.getWidth() - mRightDrag.getWidth()
                    -getResources().getDimensionPixelSize(R.dimen.flow_handler_left_margin);
            updateViewLayout(mWindowID, layoutParams);
            mFlowList.setBackgroundResource(R.drawable.bar_right);
            sendCloseRightExpendMessage();
        } else {
            sendCloseLeftExpendMessage();
            mFlowList.setBackgroundResource(R.drawable.bar_left);
        }
    }

    //normal state to handler drag state
    private void normalToDrag() {
        Window window = getWindow(mHandlerWindowID);
        mMode = MODE_DRAG;
        window.addFlag(StandOutFlags.FLAG_WINDOW_MOVE_ENABLE);
        mWindowHandlerController.startDrag();
        removeCloseExpendMessage();
    }

    //handler expend state to handler expend drag state
    private void expendToExpendDrag() {
        Window window = getWindow(mWindowID);
        mMode = MODE_EXPEND_DRAG;
        window.addFlag(StandOutFlags.FLAG_WINDOW_MOVE_ENABLE);
        mHandlerWindowController.startDrag();
        removeCloseExpendMessage();
    }

    //expend drag to left expend
    private void expendDragToLeftExpend(int id, StandOutLayoutParams params, Window window) {
        mLeftDrag.setVisibility(View.VISIBLE);
        mRightDrag.setVisibility(View.GONE);
        mFlowList.setVisibility(View.VISIBLE);
        mFlowList.setBackgroundResource(R.drawable.bar_left);
        StandOutLayoutParams originalParams = getParams(id, window);
        params.y = 0;
        params.x = 0;
        params.width = originalParams.width;
        params.height = originalParams.height;
        updateViewLayout(id, params);
        setMode(MODE_EXPEND);
        sendCloseLeftExpendMessage();
    }

    //expend drag to left normal
    private void expendDragToLeftNormal(int id, StandOutLayoutParams params, Window window) {

        mLeftDrag.setVisibility(View.VISIBLE);

        Window handlerWindow = getHandlerWindow();
        StandOutLayoutParams handlerParams = handlerWindow.getLayoutParams();
        handlerParams.y = ((FrameLayout.LayoutParams)mLeftDrag.getLayoutParams()).topMargin;
        updateViewLayout(mHandlerWindowID, handlerParams);

        mHandlerDragLayer.setVisibility(View.GONE);
        mRightDrag.setVisibility(View.GONE);
        mFlowList.setVisibility(View.GONE);

        params.y = 0;
        params.x = 0;
        updateViewLayout(id, params);

        mWindowHandlerDragLayer.setVisibility(View.VISIBLE);
        mWindowLeftDrag.setVisibility(View.VISIBLE);

        setMode(MODE_NORMAL);
        removeCloseExpendMessage();
    }

    //expend drag to right expend
    private void expendDragToRightExpend(int id, StandOutLayoutParams params, Window window) {
        mLeftDrag.setVisibility(View.GONE);
        mRightDrag.setVisibility(View.VISIBLE);
        mFlowList.setVisibility(View.VISIBLE);
        mFlowList.setBackgroundResource(R.drawable.bar_right);
        StandOutLayoutParams originalParams = getParams(id, window);
        params.y = 0;
        params.x = mWindowManager.getDefaultDisplay().getWidth() - mFlowList.getWidth() - mRightDrag.getWidth()
                -getResources().getDimensionPixelSize(R.dimen.flow_handler_left_margin);
        params.width = originalParams.width;
        params.height = originalParams.height;
        updateViewLayout(id, params);
        setMode(MODE_EXPEND);
        sendCloseRightExpendMessage();
    }

    //expend drag to right normal
    private void expendDragToRightNormal(int id, StandOutLayoutParams params, Window window) {
        mFlowList.setVisibility(View.GONE);
        mLeftDrag.setVisibility(View.GONE);
        mRightDrag.setVisibility(View.VISIBLE);
        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).leftMargin = 0;
        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).rightMargin = 0;
        StandOutLayoutParams originalParams = getParams(id, window);
        params.y = 0;
        params.x = mWindowManager.getDefaultDisplay().getWidth() - mRightDrag.getWidth();
        params.width = originalParams.width;
        params.height = originalParams.height;
        updateViewLayout(id, params);
        setMode(MODE_NORMAL);

        removeCloseExpendMessage();
    }

    public void leftExpendToNormal() {
        final StandOutLayoutParams params = (StandOutLayoutParams) getWindow(mHandlerWindowID).getLayoutParams();

        mWindowHandlerDragLayer.setVisibility(View.VISIBLE);
        mWindowLeftDrag.setVisibility(View.VISIBLE);
        mWindowRightDrag.setVisibility(View.INVISIBLE);
        mDragLayer.setVisibility(View.INVISIBLE);
        mHandlerDragLayer.setVisibility(View.INVISIBLE);
        mFlowList.setVisibility(View.INVISIBLE);

        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).leftMargin = 0;
        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).rightMargin = 0;
        params.x = 0;

        mMode = MODE_NORMAL;
        updateViewLayout(mHandlerWindowID, params);

        getHandlerWindow().requestFocus();
        removeCloseExpendMessage();
    }

    private void leftNormalToExpend() {

        final StandOutLayoutParams handlerParams = (StandOutLayoutParams) getWindow(mHandlerWindowID).getLayoutParams();
        final StandOutLayoutParams params = (StandOutLayoutParams) this.getParams(mWindowID, getWindow());

        mDragLayer.setVisibility(View.VISIBLE);
        mHandlerDragLayer.setVisibility(View.VISIBLE);
        mWindowHandlerDragLayer.setVisibility(View.INVISIBLE);
        mFlowList.setVisibility(View.VISIBLE);
        handlerParams.x = 0;
        updateViewLayout(mHandlerWindowID, handlerParams);

        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).leftMargin = (int)getResources().getDimensionPixelSize(R.dimen.flow_handler_left_margin);
        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).rightMargin = 0;
        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).topMargin = handlerParams.y;
        params.x = 0;
        params.y = 0;
        updateViewLayout(mWindowID, params);


        mMode = MODE_EXPEND;
        sendCloseLeftExpendMessage();
    }

    public void rightExpendToNormal() {

        final StandOutLayoutParams params = (StandOutLayoutParams) getWindow(mHandlerWindowID).getLayoutParams();

        mWindowHandlerDragLayer.setVisibility(View.VISIBLE);
        mWindowLeftDrag.setVisibility(View.INVISIBLE);
        mWindowRightDrag.setVisibility(View.VISIBLE);

        mDragLayer.setVisibility(View.INVISIBLE);
        mHandlerDragLayer.setVisibility(View.INVISIBLE);
        mFlowList.setVisibility(View.INVISIBLE);

        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).leftMargin = 0;
        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).rightMargin = 0;

        mMode = MODE_NORMAL;
        params.x = mWindowManager.getDefaultDisplay().getWidth() - mRightDrag.getWidth();

        updateViewLayout(mHandlerWindowID, params);
        getHandlerWindow().requestFocus();
        removeCloseExpendMessage();
    }

    private void rightNormalToExpend() {
        final StandOutLayoutParams handlerParams = (StandOutLayoutParams) getWindow(mHandlerWindowID).getLayoutParams();
        final StandOutLayoutParams params = (StandOutLayoutParams) this.getParams(mWindowID, getWindow());

        mDragLayer.setVisibility(View.VISIBLE);
        mHandlerDragLayer.setVisibility(View.VISIBLE);
        mWindowHandlerDragLayer.setVisibility(View.INVISIBLE);
        mFlowList.setVisibility(View.VISIBLE);
        updateViewLayout(mHandlerWindowID, handlerParams);

        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).leftMargin = 0;
        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).rightMargin = (int)getResources().getDimensionPixelSize(R.dimen.flow_handler_left_margin);
        ((LinearLayout.LayoutParams)mHandlerDragLayer.getLayoutParams()).topMargin = handlerParams.y;

        mMode = MODE_EXPEND;
        params.y = 0;
        params.x = mWindowManager.getDefaultDisplay().getWidth() - mFlowList.getWidth() - mRightDrag.getWidth()
                -getResources().getDimensionPixelSize(R.dimen.flow_handler_left_margin);

        updateViewLayout(mWindowID, params);
        sendCloseRightExpendMessage();
    }

    public boolean onKeyEvent(int id, Window window, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:

                    if(mPosition == POSITION_LEFT_HALF && mMode == MODE_EXPEND) {
                        leftExpendToNormal();
                    } else if(mPosition == POSITION_RIGHT_HALF && mMode == MODE_EXPEND) {
                        rightExpendToNormal();
                    } else if(mMode == MODE_EDIT) {
                        editToExpend();
                        return true;
                    }//end if
                    break;
            }

        } else if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    if (event.isLongPress()) {
                        SideBar.this.stopSelf();
                        android.os.Process.killProcess(Process.myPid());
                    }

                    break;

                default:
                    break;
            }
        }
        return false;
    }

    /**
     * TODO 
     * @author zhangf
     * @date 2013-6-17
     * @return void
     */
    public void notifyDataSize(int count) {
        // TODO Auto-generated method stub

    }

    private void registerBroadCastReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
        SideBar.this.registerReceiver(mConfigChangeReceiver, intentFilter);
    }

    BroadcastReceiver mConfigChangeReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            mContentAdapter.chageLocal();
            mFlowAdapter.chageLoacal();
            mEditButton.setText(R.string.edit_button_state_edit);
        }
    };

    private void unRigisterBroadcastReceiver(){
        unregisterReceiver(mConfigChangeReceiver);
    }

    /**
     * close left handler
     */
    private void sendCloseLeftExpendMessage() {
        removeCloseExpendMessage();
        mHandler.sendEmptyMessageDelayed(MSG_LEFT_EXPEND_TO_NORMAL, MSG_DELAY_TIME);
    }

    private void removeCloseExpendMessage() {
        if(mHandler.hasMessages(MSG_LEFT_EXPEND_TO_NORMAL)) {
            mHandler.removeMessages(MSG_LEFT_EXPEND_TO_NORMAL);
        }
        if(mHandler.hasMessages(MSG_RIGHT_EXPEND_TO_NORMAL)) {
            mHandler.removeMessages(MSG_RIGHT_EXPEND_TO_NORMAL);
        }
    }

    private void sendCloseRightExpendMessage() {
        removeCloseExpendMessage();
        mHandler.sendEmptyMessageDelayed(MSG_RIGHT_EXPEND_TO_NORMAL, MSG_DELAY_TIME);
    }
    
}
