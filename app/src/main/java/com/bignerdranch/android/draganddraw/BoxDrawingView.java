package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class BoxDrawingView extends View {
  public static final String TAG = "BoxDrawingView";
  public static final int RED = 0x22ff0000;
  public static final int WHITE = 0xfff8efe0;

  private static int[] sColors = new int[] {
      RED, Color.BLACK, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.GRAY
  };

  private Box mCurrentBox = null;
  private ArrayList<Box> mBoxes = new ArrayList<Box>();
  private ArrayList<Paint> mPaints = new ArrayList<Paint>();
  private Paint mBackgroundPaint;

  // Used when creating the view in code
  public BoxDrawingView(Context context) {
    this(context, null);
  }

  // Used when inflating the view from XML
  public BoxDrawingView(Context context, AttributeSet attrs) {
    super(context, attrs);

    // Paint the background off-white
    mBackgroundPaint = new Paint();
    mBackgroundPaint.setColor(WHITE);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    PointF curr = new PointF(event.getX(), event.getY());
    //Log.i(TAG, String.format("Received event at x=%f, y=%f :", curr.x, curr.y));
    Paint paint = null;
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        // Reset drawing state
        mCurrentBox = new Box(curr);
        mBoxes.add(mCurrentBox);
        paint = new Paint();
        paint.setColor(RED);
        mPaints.add(paint);
        break;
      case MotionEvent.ACTION_MOVE:
        if (mCurrentBox != null) {
          mCurrentBox.setCurrent(curr);
          paint = new Paint();
          paint.setColor(sColors[mPaints.size() % sColors.length]);
          mPaints.add(paint);
          invalidate();
        }
        break;
      case MotionEvent.ACTION_UP:
        mCurrentBox = null;
        break;
      case MotionEvent.ACTION_CANCEL:
        mCurrentBox = null;
        break;
    }
    return true;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    // Fill the background
    canvas.drawPaint(mBackgroundPaint);
    int i = 0;
    for (Box box : mBoxes) {
      float left = Math.min(box.getOrigin().x, box.getCurrent().x);
      float right = Math.max(box.getOrigin().x, box.getCurrent().x);
      float top = Math.min(box.getOrigin().y, box.getCurrent().y);
      float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);
      canvas.drawRect(left, top, right, bottom, mPaints.get(i));
      i++;
    }
  }

  @Override
  protected Parcelable onSaveInstanceState(){
    Parcelable parcelable = super.onSaveInstanceState();
    Bundle bundle = new Bundle();
    bundle.putParcelable("parcelable", parcelable);

    //mCurrentBox
    if (mCurrentBox != null) {
      bundle.putBoolean("has_current_box", true);
      bundle.putFloat("current_origin_x", mCurrentBox.getOrigin().x);
      bundle.putFloat("current_origin_y", mCurrentBox.getOrigin().y);
      bundle.putFloat("current_current_x", mCurrentBox.getCurrent().x);
      bundle.putFloat("current_current_y", mCurrentBox.getCurrent().y);
    } else {
      bundle.putBoolean("has_current_box", false);
    }

    //mBoxes
    bundle.putInt("num_boxes", mBoxes.size());
    for (int i=0; i < mBoxes.size(); i++) {
      Box box = mBoxes.get(i);
      bundle.putFloat("box" + i + "origin_x", box.getOrigin().x);
      bundle.putFloat("box" + i + "origin_y", box.getOrigin().y);
      bundle.putFloat("box" + i + "current_x", box.getCurrent().x);
      bundle.putFloat("box" + i + "current_y", box.getCurrent().y);
    }

    //mPaints
    bundle.putInt("num_paints", mPaints.size());
    for (int i=0; i < mPaints.size(); i++) {
      Paint paint = mPaints.get(i);
      bundle.putInt("paint" + i, paint.getColor());
      i++;
    }

    Log.i(TAG, "in onSaveInstanceState " + bundle.toString());
    return bundle;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    Log.i(TAG, "in onRestoreState " + state.toString());
    if (state instanceof Bundle) {
      Bundle bundle = (Bundle) state;
      super.onRestoreInstanceState(bundle.getParcelable("parcelable"));

      if (bundle.getBoolean("has_current_box")) {
        mCurrentBox = new Box(new PointF(bundle.getFloat("current_origin_x"), bundle.getFloat("current_origin_y")));
        mCurrentBox.setCurrent(new PointF(bundle.getFloat("current_current_x"), bundle.getFloat("current_current_y")));
      } else {
        mCurrentBox = null;
      }

      //mBoxes
      int numBoxes = bundle.getInt("num_boxes");
      mBoxes = new ArrayList<Box>(numBoxes);
      for (int i=0; i < numBoxes; i++) {
        Box box = new Box(new PointF(bundle.getFloat("box" + i + "origin_x"), bundle.getFloat("box" + i + "origin_y")));
        box.setCurrent(new PointF(bundle.getFloat("box" + i + "current_x"), bundle.getFloat("box" + i + "current_y")));
        mBoxes.add(box);
      }

      //mPaints
      int numPaints = bundle.getInt("num_paints");
      mPaints = new ArrayList<Paint>(numPaints);
      for (int i=0; i < numPaints; i++) {
        Paint paint = new Paint();
        paint.setColor(bundle.getInt("paint" + i));
        mPaints.add(paint);
      }
      // Paint the background off-white
      mBackgroundPaint = new Paint();
      mBackgroundPaint.setColor(WHITE);
    }
    else {
      super.onRestoreInstanceState(state);
    }
  }
}