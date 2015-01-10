package ee.vincent.clearsky.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by jakob on 4.01.2015.
 */
public class ListDividerDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    public ListDividerDecoration(Context context) {
        final TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.listDivider});
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        if (mDivider == null)
            return;

        if (parent.getChildPosition(view) < 1)
            return;

        outRect.top = mDivider.getIntrinsicHeight();
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {

        if (mDivider == null) {
            super.onDrawOver(c, parent, state);
            return;
        }

        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();

        for (int i=1; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final int size = mDivider.getIntrinsicHeight();
            final int top = child.getTop() - size;
            final int bottom = top + size;
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

}