package com.bignerdranch.android.draganddraw;

import android.animation.ArgbEvaluator;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class BoxDrawingView extends View {
  public static final String TAG = "BoxDrawingView";
  public static final int RED = 0x22ff0000;
  public static final int WHITE = 0xfff8efe0;
  public static final int RED2 = 0xFFFF0000;

  public static final int PURPLE = 0xFF990099;
  public static final int LIGHT_GREEN = 0xFFCCFFCC;
  public static final int ORANGE = 0xFFFF9933;
  public static final int PINK = 0xFFFF99FF;
  public static final int BROWN = 0xFF996600;

  int mPrevBoxCount = 0;

  private static int[] sColors = new int[] {
      PURPLE, RED2, LIGHT_GREEN, ORANGE, PINK, BROWN,
      RED, Color.BLACK, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.GRAY
  };

  private Box mCurrentBox = null;
  private ArrayList<Box> mBoxes = new ArrayList<Box>();
  private ArrayList<Paint> mPaints = new ArrayList<Paint>();
  private Paint mBackgroundPaint;
  final Random mRandom = new Random(System.currentTimeMillis());

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

  private int pickColor() {
    int currentPaint = mPaints.get(mPaints.size() -1).getColor();
    int lastPaint = -1;
    if (mPaints.size() > 1) {
      lastPaint = mPaints.get(mPaints.size() -2).getColor();
    }

    int randomIdx = mRandom.nextInt(sColors.length);
    int color = sColors[randomIdx];
    for (int i=0; i < 5; i++) {
      if (currentPaint != color && lastPaint != color) {
        break;
      }
      else {
        randomIdx = mRandom.nextInt(sColors.length);
        color = sColors[randomIdx];
      }
    }
    return color;
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
        mPrevBoxCount++;

        paint = new Paint();
        paint.setColor(RED);
        mPaints.add(paint);
        break;
      case MotionEvent.ACTION_MOVE:
        if (mCurrentBox != null) {
          mCurrentBox.setCurrent(curr);
          paint = new Paint();
          paint.setColor(pickColor());
          mPaints.add(paint);
          invalidate();
        }
        break;
      case MotionEvent.ACTION_UP:
        Log.d(TAG, String.format("\t Box %d origin %s current %s",
            mBoxes.size(), mCurrentBox.getOrigin(), mCurrentBox.getCurrent()));

        mCurrentBox = null;
        if (mPrevBoxCount == 2) {
          addMiddleBox();
          mPrevBoxCount = 0;
          invalidate();
        }

        if (mBoxes.size() % 5 == 0) {
          Toast.makeText(this.getContext(), "Wow Liani! There are " + mBoxes.size() + " boxes", Toast.LENGTH_LONG).show();
        }
        break;
      case MotionEvent.ACTION_CANCEL:
        mCurrentBox = null;
        break;
    }
    return true;
  }

  private void addMiddleBox() {
    //add a new box now which is the mix of the previous two
    Box a = mBoxes.get(mBoxes.size() - 1);
    float aleft = Math.min(a.getOrigin().x, a.getCurrent().x);
    float aright = Math.max(a.getOrigin().x, a.getCurrent().x);
    float atop = Math.min(a.getOrigin().y, a.getCurrent().y);
    float abottom = Math.max(a.getOrigin().y, a.getCurrent().y);

    Box b = mBoxes.get(mBoxes.size() - 2);
    float bleft = Math.min(b.getOrigin().x, b.getCurrent().x);
    float bright = Math.max(b.getOrigin().x, b.getCurrent().x);
    float btop = Math.min(b.getOrigin().y, b.getCurrent().y);
    float bbottom = Math.max(b.getOrigin().y, b.getCurrent().y);

    PointF newOrigin = new PointF(aleft + (aright - aleft) / 2, abottom + (atop - abottom) / 2);
    PointF newCurrent = new PointF(bleft + (bright - bleft) / 2 , bbottom + (btop - bbottom) / 2);
    Log.d(TAG, String.format("\t Adding middle box origin %s current %s", newOrigin, newCurrent));

    Box middleBox = new Box(newOrigin);
    middleBox.setCurrent(newCurrent);
    Paint p = new Paint();
    Paint pa = mPaints.get(mPaints.size() -1);
    Paint pb = mPaints.get(mPaints.size() -2);
    p.setColor(mergeColor(pa.getColor(), pb.getColor()));
    mPaints.add(p);
    mBoxes.add(middleBox);
  }

  private int merge(int a, int b) {
    //return Math.min(a, b) + Math.abs(a - b);
    return (a + b) / 2;
  }

  private int mergeColor(int colorA, int colorB) {
    //to properly merge we would need to find the lower red and differences in red, then blue, then green
//    int alpha = merge(Color.alpha(colorA), Color.alpha(colorB));
//    int red = merge(Color.red(colorA), Color.red(colorB));
//    int green = merge(Color.green(colorA), Color.green(colorB));
//    int blue = merge(Color.blue(colorA), Color.blue(colorB));
//    int argb = Color.argb(alpha, red, green, blue);
//    return argb;

    //return (Integer)new ArgbEvaluator().evaluate(0.5f, colorA, colorB);

    return interpolateColor(colorA, colorB, 0.5f);
  }

  private float interpolate(float a, float b, float proportion) {
    return (a + ((b - a) * proportion));
  }

  /** Returns an interpoloated color, between <code>a</code> and <code>b</code> */
  private int interpolateColor(int a, int b, float proportion) {
    float[] hsva = new float[3];
    float[] hsvb = new float[3];
    Color.colorToHSV(a, hsva);
    Color.colorToHSV(b, hsvb);
    for (int i = 0; i < 3; i++) {
      hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
    }
    return Color.HSVToColor(hsvb);
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