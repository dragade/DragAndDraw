package com.bignerdranch.android.draganddraw;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class DragAndDrawFragment extends Fragment {
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_drag_and_draw, container, false);

    Toast.makeText(getActivity(), "Hi Liani, let's play!", Toast.LENGTH_LONG).show();

    return v;
  }

//  @Override
//  protected Parcelable onSaveInstanceState() {
//  }
//
//  @Override
//  protected void onRestoreInstanceState(Parcelable state) {
//  }
}