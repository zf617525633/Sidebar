/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.yoyu.sidebar.handler;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import co.yoyu.sidebar.view.SideBar;

/**
 * A ViewGroup that coordinated dragging across its dscendants
 */
public class HandlerDragLayer extends FrameLayout {

    private final String LOG_TAG = "HandlerDragLayer";
    private HandlerDragController mDragController;
    private HandlerWindowDragController mWindowDragController;
    /**SideBar*/
    private SideBar mSidebar;
    /**
     * Used to create a new DragLayer from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
    public HandlerDragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        
    }

    public void setDragController(HandlerDragController controller) {
        mDragController = controller;
    }

    public void setWindowDragControl(HandlerWindowDragController controller) {
        mWindowDragController = controller;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(mDragController == null)
            return super.dispatchKeyEvent(event);
        return mDragController.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(mSidebar == null && (mWindowDragController == null || mDragController == null))
            return super.onInterceptTouchEvent(ev);
        boolean result;
        if (mWindowDragController != null && mSidebar.getMode() == SideBar.MODE_EXPEND_DRAG) {
            result = mWindowDragController.onInterceptTouchEvent(ev);
        } else if(mDragController != null){
            result = mDragController.onInterceptTouchEvent(ev);
        } else {
            result = super.onInterceptTouchEvent(ev);
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mSidebar == null && (mWindowDragController == null || mDragController == null))
            return super.onTouchEvent(ev);
        boolean result = false;
        if (mSidebar.getMode() == SideBar.MODE_EXPEND_DRAG) {
            result = mWindowDragController.onTouchEvent(ev);
        } else if(mDragController != null){
            result = mDragController.onTouchEvent(ev);
        } else {
            result = super.onTouchEvent(ev);
        }//end if
        return result;
    }

    public void setSideBar(SideBar sideBar) {
        // TODO Auto-generated method stub
        this.mSidebar = sideBar;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        Log.d(LOG_TAG,"visbility  "+visibility);
    }
}
