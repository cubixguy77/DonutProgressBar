package com.robsessions.donutprogressbar;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

public class DonutProgress extends View {
    private Paint finishedPaint;
    private Paint textPaint;
    private Paint glowPaint;
    private Paint transparentPaint;

    private ObjectAnimator glowAnim;

    private RectF finishedOuterRect = new RectF();
    private RectF glowRect = new RectF();

    private float textSize;
    private int textColor;
    private int finishedStrokeColor;
    private float finishedStrokeWidth;

    /* State properties */
    private String text = null;
    private boolean isGlowing = false;
    private int progress = 0;
    private float partialArcFillAnimProgress = 0f;
    private float partialGlowAnimProgress = 0f;
    private float partialFiveFillAnimProgress = 0f;


    private final float default_stroke_width;
    private final int color_finished = Color.rgb(255, 202, 40);
    private final int color_unfinished = Color.rgb(100, 181, 246);
    private final int color_5 = Color.rgb(255, 202, 40);
    private final int color_4 = Color.rgb(255, 213, 79);
    private final int color_3 = Color.rgb(255, 224, 130);
    private final int color_2 = Color.rgb(255, 236, 179);
    private final int color_1 = Color.rgb(255, 248, 230);
    private final int color_background = Color.rgb(33, 150, 243);
    private final int color_glow = color_unfinished;

    private final int default_finished_color = Color.rgb(66, 145, 241);
    private final int default_text_color = Color.rgb(66, 145, 241);
    private final float default_text_size;
    private final int min_size;

    private static final String INSTANCE_STATE = "saved_instance";
    private static final String INSTANCE_TEXT_COLOR = "text_color";
    private static final String INSTANCE_TEXT_SIZE = "text_size";
    private static final String INSTANCE_TEXT = "text";
    private static final String INSTANCE_PROGRESS = "progress";
    private static final String INSTANCE_FINISHED_STROKE_WIDTH = "finished_stroke_width";

    public DonutProgress(Context context) {
        this(context, null);
    }

    public DonutProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DonutProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        default_text_size = Utils.sp2px(getResources(), 18);
        min_size = (int) Utils.dp2px(getResources(), 100);
        default_stroke_width = Utils.dp2px(getResources(), 10);

        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DonutProgress, defStyleAttr, 0);
        initByAttributes(attributes);
        attributes.recycle();

        initPainters();
    }

    protected void initPainters() {
        if (textPaint == null) {
            textPaint = new Paint();
            textPaint.setColor(color_finished);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(200);
            textPaint.setAntiAlias(true);
        }

        if (finishedPaint == null) {
            finishedPaint = new Paint();
            finishedPaint.setColor(finishedStrokeColor);
            finishedPaint.setStyle(Paint.Style.STROKE);
            finishedPaint.setAntiAlias(true);
            finishedPaint.setStrokeWidth(finishedStrokeWidth);
        }

        finishedOuterRect.set(finishedStrokeWidth, finishedStrokeWidth, getWidth() - finishedStrokeWidth, getHeight() - finishedStrokeWidth);

        if (glowPaint == null) {
            glowPaint = new Paint();
            glowPaint.setColor(color_glow);
            glowPaint.setStyle(Paint.Style.FILL);
        }

        if (transparentPaint == null) {
            transparentPaint = new Paint();
            transparentPaint.setColor(color_background);
            transparentPaint.setStyle(Paint.Style.FILL);
        }

        // kicking off this anim will result in periodic calls to setPartialGlowAnimProgress(newValue)
        if (glowAnim == null) {
            glowAnim = ObjectAnimator.ofFloat(this, "partialGlowAnimProgress", 0f, 100f);
            glowAnim.setDuration(2000);
            glowAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            glowAnim.setRepeatMode(ValueAnimator.RESTART);
            glowAnim.setRepeatCount(ObjectAnimator.INFINITE);
        }
    }

    protected void initByAttributes(TypedArray attributes) {
        finishedStrokeColor = attributes.getColor(R.styleable.DonutProgress_donut_finished_color, default_finished_color);
        finishedStrokeWidth = attributes.getDimension(R.styleable.DonutProgress_donut_finished_stroke_width, default_stroke_width);
        textColor = attributes.getColor(R.styleable.DonutProgress_donut_text_color, default_text_color);
        textSize = attributes.getDimension(R.styleable.DonutProgress_donut_text_size, default_text_size);
    }

    @Override
    public void invalidate() {
        initPainters();
        super.invalidate();
    }

    public float getFinishedStrokeWidth() {
        return finishedStrokeWidth;
    }

    public int getProgress() {
        return progress;
    }

    public void reset() {
        setPartialFiveFillAnimProgress(0f);
        setProgress(1);
    }

    public void setProgress(final int newProgress) {
        ObjectAnimator arcFillAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.animator.progress_anim);
        arcFillAnim.setInterpolator(new DecelerateInterpolator());
        arcFillAnim.setTarget(this);
        arcFillAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progress = newProgress;
                setPartialArcFillAnimProgress(0f);

                if (newProgress == 5) {
                    ObjectAnimator fiveFillAnim = ObjectAnimator.ofFloat(DonutProgress.this, "partialFiveFillAnimProgress", 0f, 100f);
                    fiveFillAnim.setDuration(700);
                    fiveFillAnim.setInterpolator(new LinearInterpolator());
                    fiveFillAnim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            setGlow(true);
                        }
                    });
                    fiveFillAnim.start();
                }
                else {
                    setGlow(false);
                }

                invalidate();
            }
        });
        arcFillAnim.start();
    }

    /* Value ranges from 0 - 100
     * Represents the animations progress as a sector fills up or empties
     */
    public void setPartialArcFillAnimProgress(float partialArcFillAnimProgress) {
        this.partialArcFillAnimProgress = partialArcFillAnimProgress;
        invalidate();
    }

    /* Value ranges from 0 - 100
     * Represents the animations progress where the fifth sector overtakes all others upon reaching 5x
     */
    public void setPartialFiveFillAnimProgress(float partialFiveFillAnimProgress) {
        this.partialFiveFillAnimProgress = partialFiveFillAnimProgress;
        invalidate();
    }

    public float getTextSize() {
        return textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public String getText() {
        return text;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));
    }

    private int measure(int measureSpec) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = min_size;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (this.isGlowing) {
            drawGlow(canvas);
        }

        drawAllSectors(canvas);

        if (this.partialFiveFillAnimProgress > 0) {
            drawGlobalArc(canvas, finishedOuterRect, this.partialFiveFillAnimProgress, finishedPaint);
        }

        drawText(canvas, this.progress + "x");
    }

    private void drawAllSectors(Canvas canvas) {
        drawArc(canvas, 1);
        drawArc(canvas, 2);
        drawArc(canvas, 3);
        drawArc(canvas, 4);
        drawArc(canvas, 5);
    }

    private void drawGlow(Canvas canvas) {
        int pad = 50;
        canvas.drawCircle(getWidth() / 2,getHeight() / 2, getWidth() / 2 - pad + (pad/100f * partialGlowAnimProgress), glowPaint); // outer glow
        canvas.drawCircle(getWidth() / 2,getHeight() / 2, getWidth() / 2 - finishedStrokeWidth, transparentPaint); // fills donut hole with background color
    }

    private void setPartialGlowAnimProgress(float partialGlowAnimProgress) {
        this.partialGlowAnimProgress = partialGlowAnimProgress;
        glowPaint.setAlpha((255 - (int) (partialGlowAnimProgress * 2.55f)));
        invalidate();
    }

    public void setGlow(boolean isGlowing) {
        this.isGlowing = isGlowing;

        if (isGlowing) {
            glowAnim.start();
        }
        else {
            setPartialGlowAnimProgress(0);
            glowAnim.cancel();
            glowAnim.removeAllListeners();
        }
    }

    private int getColor(int sector) {
        switch (sector) {
            case 1: return color_1;
            case 2: return color_2;
            case 3: return color_3;
            case 4: return color_4;
            case 5: return color_5;
            default: return color_1;
        }
    }

    private void drawGlobalArc(Canvas canvas, RectF rect, float partialProgress, Paint paint) {
        paint.setColor(getColor(5));
        canvas.drawArc(rect, 270, -360 * partialProgress / 100, false, paint);
    }

    private void drawArc(Canvas canvas, int sector) {
        if (this.progress < sector) {
            finishedPaint.setColor(color_unfinished);
            finishedPaint.setAlpha(255);
            canvas.drawArc(finishedOuterRect, 270 - (72 * (sector-1)), -72, false, finishedPaint);

            /* Draw Partially Filled Arc during animations */
            if (this.partialArcFillAnimProgress > 0 && this.progress + 1 == sector) {
                finishedPaint.setColor(getColor(sector));
                canvas.drawArc(finishedOuterRect, 270 - (72 * (sector-1)), -72*this.partialArcFillAnimProgress /100, false, finishedPaint);
            }
        }
        else {
            finishedPaint.setColor(getColor(sector));
            canvas.drawArc(finishedOuterRect, 270 - (72 * (sector - 1)), -72, false, finishedPaint);
        }
    }

    private void drawText(Canvas canvas, String text) {
        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));

        canvas.drawText(text, xPos, yPos, textPaint);
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(INSTANCE_TEXT_COLOR, getTextColor());
        bundle.putFloat(INSTANCE_TEXT_SIZE, getTextSize());
        bundle.putFloat(INSTANCE_PROGRESS, getProgress());
        bundle.putString(INSTANCE_TEXT, getText());
        bundle.putFloat(INSTANCE_FINISHED_STROKE_WIDTH, getFinishedStrokeWidth());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            textColor = bundle.getInt(INSTANCE_TEXT_COLOR);
            textSize = bundle.getFloat(INSTANCE_TEXT_SIZE);
            finishedStrokeWidth = bundle.getFloat(INSTANCE_FINISHED_STROKE_WIDTH);
            initPainters();
            setProgress(bundle.getInt(INSTANCE_PROGRESS));
            text = bundle.getString(INSTANCE_TEXT);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }


}