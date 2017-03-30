package com.rhoadster91.blockedittext;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.view.View;

/**
 * Created by girish on 3/31/17.
 */

public class EditableLinearLayout extends LinearLayout implements View.OnFocusChangeListener {

    private boolean numericOnly;

    public EditableLinearLayout(Context context) {
        super(context);
        initialize(context);
    }

    public EditableLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context);
    }

    public EditableLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    public EditableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, 0);
    }

    private void initialize(Context context) {
        setFocusableInTouchMode(true);
        setFocusable(true);
        setOnFocusChangeListener(this);
        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (hasFocus) {
            imm.showSoftInput(v, 0);
        } else {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    // Here is where the magic happens
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection connection = super.onCreateInputConnection(outAttrs);
        outAttrs.inputType = numericOnly ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        return  connection;
    }

    public void setNumericOnly(boolean numericOnly) {
        this.numericOnly = numericOnly;
    }

}
