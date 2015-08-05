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

package co.yoyu.sidebar.drag;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import co.yoyu.sidebar.view.SideBar;

/**
 * A ViewGroup that coordinated dragging across its dscendants
 */
public class DragLayer extends FrameLayout {
    private DragController mDragController;

    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        
    }

    public void setDragController(DragController controller) {
        mDragController = controller;
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragController.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDragController.setWindowToken(getWindowToken());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = mDragController.onInterceptTouchEvent(ev); 
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = mDragController.onTouchEvent(ev);
        return result;
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        return mDragController.dispatchUnhandledMove(focused, direction);
    }

    public void setSideBar(final SideBar sideBar) {
        mDragController.setSideBar(sideBar);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }
}
