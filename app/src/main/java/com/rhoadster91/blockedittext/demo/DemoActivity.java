package com.rhoadster91.blockedittext.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.rhoadster91.blockedittext.BlockEditTextHelper;

public class DemoActivity extends AppCompatActivity implements BlockEditTextHelper.OnOTPEnteredListener {

    BlockEditTextHelper mOptHelper = new BlockEditTextHelper(R.layout.text_view, R.layout.space_view, R.drawable.drawable_selected, R.drawable.drawable_unselected);

    FrameLayout rootLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = (FrameLayout) findViewById(R.id.root_view);

        mOptHelper.setCount(6);
        mOptHelper.setAllowedTypes(false, true);
        View otpView = mOptHelper.inflate(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        otpView.setLayoutParams(params);
        mOptHelper.setOnOTPEnteredListener(this);
        rootLayout.addView(otpView);
    }

    @Override
    public void onOTPEntered(String otp) {
        Toast.makeText(this, otp, Toast.LENGTH_SHORT).show();
    }
}
