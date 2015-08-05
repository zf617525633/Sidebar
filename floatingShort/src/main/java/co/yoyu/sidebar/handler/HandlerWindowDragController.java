/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

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
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.MotionEvent;

import co.yoyu.sidebar.view.SideBar;

/**
 * Class for initiating a drag within a view or across multiple views.
 */
public class HandlerWindowDragController {

    public static final int VIBRATE_DURATION = 100;

    private Context mContext;
    private final Vibrator mVibrator;

    private SideBar mSideBar;

    private boolean mDragging;
    /**
     * Used to create a new DragLayer from XML.
     *
     * @param context The application's context.
     */
    public HandlerWindowDragController(Context context) {
        mContext = context;
        mVibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setSideBar(SideBar sidebar) {
        mSideBar = sidebar;
    }

    /**
     * Starts a drag.
     * 
     * @param v The view that is being dragged
     * @param source An object representing where the drag originated
     * @param dragInfo The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *        {@link #DRAG_ACTION_COPY}
     */
    public void startDrag() {
        mDragging = true;
        mVibrator.vibrate(VIBRATE_DURATION);

    }

    /**
     * Call this from a drag source view like this:
     *
     * <pre>
     *  @Override
     *  public boolean dispatchKeyEvent(KeyEvent event) {
     *      return mDragController.dispatchKeyEvent(this, event)
     *              || super.dispatchKeyEvent(event);
     * </pre>
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragging;
    }

    /**
     * Stop dragging without dropping.
     */
    public void cancelDrag() {
        endDrag();
    }

    private void endDrag() {
        if (mDragging) {
            mDragging = false;
            mSideBar.setMode(SideBar.MODE_EXPEND);
        }
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();


        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                endDrag();
                break;
        }

        return mDragging;
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onTouchEvent(MotionEvent ev) {

        if (!mDragging) {
            return false;
        }

        boolean consumed = mSideBar.getWindow().doWindowTouchEvent(mSideBar.getWindowBody(), ev);
        final int action = ev.getAction();

        switch (action) {

        case MotionEvent.ACTION_UP:
            endDrag();

            break;
        case MotionEvent.ACTION_CANCEL:
            cancelDrag();
        }

        return consumed;
    }

    boolean isDraging(){
        return mDragging;
    }

    public void dispatchTouch(MotionEvent ev) {
        mSideBar.getWindow().doWindowTouchEvent(mSideBar.getWindowBody(), ev);
    }
}