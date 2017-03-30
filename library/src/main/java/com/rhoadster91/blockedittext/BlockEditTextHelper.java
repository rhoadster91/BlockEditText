package com.rhoadster91.blockedittext;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlockEditTextHelper implements View.OnClickListener {


    private @LayoutRes int textViewId, spaceViewId = -1;
    private @DrawableRes int selectedDrawableResId;
    private @DrawableRes int unselectedDrawableResId;

    boolean inBackspaceMode = false;

    boolean allowAlphabets = true, allowNumerals = true, caseSensitive = false;

    public interface OnOTPEnteredListener {
        void onOTPEntered(String otp);
    }

    OnOTPEnteredListener mOnOTPEnteredListener;

    List<WeakReference<TextView>> mTextViews = new ArrayList<>();

    EditableLinearLayout rootView;

    WeakReference<Context> mContextRef;

    int currentSelection;

    char []letters;

    private int count;

    public BlockEditTextHelper(@LayoutRes int textViewResId, @LayoutRes int spaceViewResId, @DrawableRes int selectedDrawable, @DrawableRes int unselectedDrawable) {
        this(textViewResId, selectedDrawable, unselectedDrawable);
        this.spaceViewId = spaceViewResId;
    }


    public BlockEditTextHelper(@LayoutRes int textViewResId, @DrawableRes int selectedDrawable, @DrawableRes int unselectedDrawable) {
        this.textViewId = textViewResId;
        this.selectedDrawableResId = selectedDrawable;
        this.unselectedDrawableResId = unselectedDrawable;
    }

    public void setCount(int count) {
        this.count = count;
        letters = new char[count];
        for(int i = 0; i <letters.length; i++) {
            letters[i] = '\0';
        }
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public void setAllowedTypes(boolean allowAlphabets, boolean allowNumerals) {
        this.allowNumerals = allowNumerals;
        this.allowAlphabets = allowAlphabets;
        if (!allowAlphabets && !allowNumerals) {
            this.allowNumerals = true;
            this.allowAlphabets = true;
        }
    }

    public void setOnOTPEnteredListener(OnOTPEnteredListener onOTPEnteredListener) {
        mOnOTPEnteredListener = onOTPEnteredListener;
    }

    public View inflate(Context context) {
        rootView = new EditableLinearLayout(context);
        mContextRef = new WeakReference<>(context);
        rootView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        rootView.setOrientation(LinearLayout.HORIZONTAL);
        rootView.setNumericOnly(!allowAlphabets);
        rootView.requestDisallowInterceptTouchEvent(true);
        rootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        if (!inBackspaceMode && letters[currentSelection] == '\0') {
                            moveToPrevious();
                        } else {
                            letters[currentSelection] = '\0';
                            if (inBackspaceMode) {
                                moveToPrevious();
                            }
                            inBackspaceMode = true;
                        }
                    } else {
                        inBackspaceMode = false;
                        char c = (char) event.getUnicodeChar();
                        if ((allowAlphabets && Character.isLetter(c)) || (allowNumerals && Character.isDigit(c))) {
                            letters[currentSelection] = c;
                            moveToNext();
                        }
                    }
                    checkValidity();
                    redraw();
                }
                return false;
            }
        });
        mTextViews = new ArrayList<>();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int i = 0; i < count; i++) {
            TextView textView = (TextView) inflater.inflate(textViewId, rootView, false);
            textView.setBackgroundResource(unselectedDrawableResId);
            textView.setOnClickListener(this);
            rootView.addView(textView);
            mTextViews.add(new WeakReference<>(textView));
            if (i != count - 1 && spaceViewId != -1) {
                View space = inflater.inflate(spaceViewId, rootView, false);
                rootView.addView(space);
            }
        }
        return rootView;
    }

    private void checkValidity() {
        if (isValid()) {
            String result = String.copyValueOf(letters);
            if (mOnOTPEnteredListener != null) {
                mOnOTPEnteredListener.onOTPEntered(result);
            }
        }
    }

    private boolean isValid() {
        for(char c : letters) {
            if (c == '\0') {
                return false;
            }
        }
        return true;
    }

    private void redraw() {
        for(int i = 0; i < count; i++) {
            TextView tv = getViewAt(i);
            char c = letters[i];
            if (!caseSensitive) {
                tv.setText(Character.toString(c).toUpperCase(Locale.getDefault()));
            } else {
                tv.setText(Character.toString(c));
            }
        }
    }

    private void moveToNext() {
        int highlight = currentSelection + 1;
        if (highlight == count) {
            highlight = count - 1;
        }
        highlight(highlight);
    }

    private void moveToPrevious() {
        int highlight = currentSelection - 1;
        if (highlight == -1) {
            highlight = 0;
        }
        highlight(highlight);
    }


    @Override
    public void onClick(View v) {
        int index = getClickedView(v);
        if(index != -1) {
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            rootView.requestFocus();
            rootView.onFocusChange(rootView, true);
            imm.showSoftInput(rootView, InputMethodManager.SHOW_FORCED);
            int lastClickedPosition = getLastValidCharPosition();
            int highlightPosition;
            if (lastClickedPosition >= index) {
                highlightPosition = index;
            } else {
                highlightPosition = lastClickedPosition + 1;
            }
            if (highlightPosition == count) {
                highlightPosition = count - 1;
            }
            highlight(highlightPosition);
        }
    }

    private int getClickedView(View v) {
        int i = -1;
        for(WeakReference<TextView> tvRef : mTextViews) {
            i++;
            TextView tv = tvRef.get();
            if (tv == null) {
                continue;
            }
            if (tv == v) {
                return i;
            }
        }
        return -1;
    }

    private TextView getViewAt(int index) {
        WeakReference<TextView> tvRef = mTextViews.get(index);
        return tvRef.get();
    }

    private void highlight(int index) {
        for(int i = 0; i < count; i++) {
            getViewAt(i).setBackgroundResource(i == index ? selectedDrawableResId : unselectedDrawableResId);
        }
        currentSelection = index;
    }

    private int getLastValidCharPosition() {
        int last = -1;
        for(int i = 0; i < letters.length; i++) {
            if (letters[i] != '\0') {
                last = i;
            }
        }
        return last;
    }

    public void setText(String text) {
        if (text.length() == count) {
            letters = text.toCharArray();
            checkValidity();
            redraw();
        }
    }

    public String getText() {
        if(isValid()) {
            return String.copyValueOf(letters);
        } else {
            return null;
        }
    }
}
