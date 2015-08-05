package co.yoyu.sidebar.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class SideBarLinearLayout extends LinearLayout {

    private View mTopView;
    public SideBarLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SideBarLinearLayout(Context context) {
        super(context);
    }

    public void setTopView(View v) {
        mTopView = v;
    }

    public void dispatchDraw(Canvas c) {
        super.dispatchDraw(c);
        final long drawingTime = getDrawingTime();
        if (mTopView != null && ((mTopView.getVisibility() == View.VISIBLE) || mTopView.getAnimation() != null)) {
            super.drawChild(c, mTopView, drawingTime);
        }
    }
}
