package com.parsibit.nice_edit_text;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import java.lang.reflect.Field;

@SuppressLint("AppCompatCustomView")
public class NiceEditText extends android.widget.EditText {

    private final static int TYPE_TEXT_VARIATION_PASSWORD = 129;
    private final static int TYPE_NUMBER_VARIATION_PASSWORD = 18;
    private final static int DEFAULT_PADDING = 15;
    private final int DEFAULT_COLOR = Color.parseColor("#B6B6B6");
    private boolean isClearIconVisible = false;
    private boolean isRTL = true;
    private int mBackgroundColor;
    private int padding, paddingLeft, paddingTop, paddingRight, paddingBottom;
    private String fontName;
    private float mStrokeWidth = 1;
    private float mCornerRadius;
    private Drawable imgCloseButton = ContextCompat.getDrawable(getContext(), android.R.drawable.ic_menu_close_clear_cancel);
    private Drawable drawableEnd;
    private boolean isPassword = false;
    private boolean isShowingPassword = false;
    private int clearIconTint;
    private int hideShowIconTint;
    private boolean isBorderView = false;

    public NiceEditText(Context context) {
        super(context);
        init(context, null);
    }

    public NiceEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NiceEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.NiceEditText);
            padding = array.getDimensionPixelSize(R.styleable.NiceEditText_android_padding, -1);
            paddingLeft = array.getDimensionPixelSize(R.styleable.NiceEditText_android_paddingLeft, DEFAULT_PADDING);
            paddingTop = array.getDimensionPixelSize(R.styleable.NiceEditText_android_paddingTop, DEFAULT_PADDING);
            paddingRight = array.getDimensionPixelSize(R.styleable.NiceEditText_android_paddingRight, DEFAULT_PADDING);
            paddingBottom = array.getDimensionPixelSize(R.styleable.NiceEditText_android_paddingBottom, DEFAULT_PADDING);
            isClearIconVisible = array.getBoolean(R.styleable.NiceEditText_edtSetClearIconVisible, isClearIconVisible);
            isRTL = array.getBoolean(R.styleable.NiceEditText_edtSetRTL, false);
            isBorderView = array.getBoolean(R.styleable.NiceEditText_edtSetBorderView, isBorderView);
            int mNormalColor = array.getColor(R.styleable.NiceEditText_edtSetBorderColor, DEFAULT_COLOR);
            mBackgroundColor = array.getColor(R.styleable.NiceEditText_edtSetBackgroundColor, Color.TRANSPARENT);
            mStrokeWidth = array.getDimension(R.styleable.NiceEditText_edtSetStrokeWidth, mStrokeWidth);
            hideShowIconTint = array.getColor(R.styleable.NiceEditText_edtHideShowPasswordIconTint, DEFAULT_COLOR);
            clearIconTint = array.getColor(R.styleable.NiceEditText_edtClearIconTint, DEFAULT_COLOR);
            this.fontName = array.getString(R.styleable.NiceEditText_edtSetFont);
            // set corner radius
            mCornerRadius = array.getDimension(R.styleable.NiceEditText_edtSetCornerRadius, 1);


            if (isBorderView) {
                setBackGroundOfLayout(getShapeBackground(mNormalColor));
                setCursorColor(mNormalColor);
            } else {
                padding(false);
            }

            if (getInputType() == TYPE_TEXT_VARIATION_PASSWORD || getInputType() == TYPE_NUMBER_VARIATION_PASSWORD) {
                isPassword = true;
                this.setMaxLines(1);
            } else if (getInputType() == EditorInfo.TYPE_CLASS_PHONE) {
                this.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
            }

            if (!isPassword && isClearIconVisible) {
                handleClearButton();
            }

            if (isPassword) {
                if (!TextUtils.isEmpty(getText())) {
                    showPasswordVisibilityIndicator(true);
                } else {
                    showPasswordVisibilityIndicator(false);
                }
            }

            if(isRTL)
                setRtl();

            setOnTouchListener(new OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    NiceEditText editText = NiceEditText.this;
                    if(isRTL) {
                        if (editText.getCompoundDrawables()[0] == null)
                            return false;

                        if (event.getAction() != MotionEvent.ACTION_UP)
                            return false;

                        if (isPassword) {
                            if (event.getX() < drawableEnd.getIntrinsicWidth()) {
                                togglePasswordVisibility();
                                event.setAction(MotionEvent.ACTION_CANCEL);
                            }
                        } else if (isClearIconVisible) {
                            if (event.getX() < imgCloseButton.getIntrinsicWidth()) {
                                editText.setText("");
                                NiceEditText.this.handleClearButton();
                            }
                        }

                    } else {
                        if (editText.getCompoundDrawables()[2] == null)
                            return false;

                        if (event.getAction() != MotionEvent.ACTION_UP)
                            return false;

                        if (isPassword) {
                            if (event.getX() > editText.getWidth() - editText.getPaddingRight() - drawableEnd.getIntrinsicWidth()) {
                                togglePasswordVisibility();
                                event.setAction(MotionEvent.ACTION_CANCEL);
                            }
                        } else if (isClearIconVisible) {
                            if (event.getX() > editText.getWidth() - editText.getPaddingRight() - imgCloseButton.getIntrinsicWidth()) {
                                editText.setText("");
                                NiceEditText.this.handleClearButton();
                            }
                        }
                    }

                    return false;
                }
            });

            setFont();
            array.recycle();
        }
    }

    public void setCursorColor(@ColorInt int color) {
        try {
            // Get the cursor resource id
            Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
            field.setAccessible(true);
            int drawableResId = field.getInt(this);

            // Get the editor
            field = TextView.class.getDeclaredField("mEditor");
            field.setAccessible(true);
            Object editor = field.get(this);

            // Get the drawable and set a color filter
            Drawable drawable = ContextCompat.getDrawable(this.getContext(), drawableResId);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            Drawable[] drawables = {drawable, drawable};

            // Set the drawables
            field = editor.getClass().getDeclaredField("mCursorDrawable");
            field.setAccessible(true);
            field.set(editor, drawables);
        } catch (Exception ignored) {
        }
    }

    private void setRtl() {
        setTextAlignment(TEXT_ALIGNMENT_VIEW_END);
    }

    private void setFont() {
        if (fontName != null) {
            try {
                setTypeface(Typefaces.get(getContext(), fontName));
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * This method is used to set the rectangle box on EditText
     */
    private void setBackGroundOfLayout(Drawable shape) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(shape);
        } else {
            setBackgroundDrawable(shape);
        }
        padding(true);
    }

    private void padding(boolean isRound) {
        int extraPadding;
        int extrapad;
        if (isRound) {
            extraPadding = 5;
            extrapad = 0;
        } else {
            extrapad = 5;
            extraPadding = 0;
        }
        if (padding != -1) {
            super.setPadding(padding + extraPadding, padding, padding, padding + extrapad);
        } else {
            super.setPadding(paddingLeft + extraPadding, paddingTop, paddingRight, paddingBottom + extrapad);
        }
    }

    /**
     * This method is used to draw the rectangle border view with color
     */
    @SuppressLint("WrongConstant")
    private Drawable getShapeBackground(@ColorInt int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(mCornerRadius);
        shape.setColor(mBackgroundColor);
        shape.setStroke((int) mStrokeWidth, color);

        return shape;
    }

    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private void handleClearButton() {
        if (isClearIconVisible) {
            DrawableCompat.setTint(imgCloseButton, clearIconTint);

            if(isRTL) {
                imgCloseButton.setBounds(0, 0, 43, 43);

                if (this.getText().length() == 0)
                    this.setCompoundDrawables(null, this.getCompoundDrawables()[1], this.getCompoundDrawables()[2], this.getCompoundDrawables()[3]);
                else
                    this.setCompoundDrawables(imgCloseButton, this.getCompoundDrawables()[1], this.getCompoundDrawables()[2], this.getCompoundDrawables()[3]);
            }
            else {
                imgCloseButton.setBounds(0, 0, 43, 43);

                if (this.getText().length() == 0)
                    this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], null, this.getCompoundDrawables()[3]);
                else
                    this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], imgCloseButton, this.getCompoundDrawables()[3]);
            }

        }
    }

    @Override
    public void onTextChanged(CharSequence s, int i, int i1, int i2) {
        try {
            if (isPassword) {
                if (s.length() > 0) {
                    showPasswordVisibilityIndicator(true);
                } else {
                    isShowingPassword = false;
                    maskPassword();
                    showPasswordVisibilityIndicator(false);
                }
            } else if (isClearIconVisible)
                NiceEditText.this.handleClearButton();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPasswordVisibilityIndicator(boolean show) {
        if (show) {
            Drawable original = isShowingPassword ?
                    ContextCompat.getDrawable(getContext(), R.drawable.ic_visibility_on) :
                    ContextCompat.getDrawable(getContext(), R.drawable.ic_visibility_off);
            original.mutate();

//            if(isRTL)
//                original.setBounds(44, 0, 0, 44);
//            else
//                original.setBounds(0, 0, 44, 44);

            original.setBounds(0, 0, 43, 43);

            DrawableCompat.setTint(original, hideShowIconTint);
            drawableEnd = original;

            if(isRTL)
                this.setCompoundDrawables(original, this.getCompoundDrawables()[1], this.getCompoundDrawables()[2], this.getCompoundDrawables()[3]);
//                this.setCompoundDrawables(original, null, null, null);
            else
                this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], original, this.getCompoundDrawables()[3]);
        } else {
            if(isRTL)
                this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], null, this.getCompoundDrawables()[3]);
            else
                this.setCompoundDrawables(null, this.getCompoundDrawables()[1], this.getCompoundDrawables()[2], this.getCompoundDrawables()[3]);
        }
    }

    //make it visible
    private void unmaskPassword() {
        setTransformationMethod(null);
    }

    //hide it
    private void maskPassword() {
        setTransformationMethod(PasswordTransformationMethod.getInstance());
    }

    public void togglePasswordVisibility() {
        // Store the selection
        int selectionStart = this.getSelectionStart();
        int selectionEnd = this.getSelectionEnd();
        // Set transformation method to show/hide password
        if (isShowingPassword) {
            maskPassword();
        } else {
            unmaskPassword();
        }
        // Restore selection
        this.setSelection(selectionStart, selectionEnd);
        // Toggle flag and show indicator
        isShowingPassword = !isShowingPassword;
        showPasswordVisibilityIndicator(true);
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
        setFont();
    }
}
