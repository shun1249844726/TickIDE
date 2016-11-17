package tickide.lexin.com.tickide.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ValueAnimator;

import tickide.lexin.com.tickide.R;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


/**
 * 自定义进度条
 * Created by Laer on 2016/10/13.
 */
public class MyProgressBar extends View {

    private int circle_color;//圆的颜色
    private int circle_progregg_color;//进度条的颜色
    private int text_color;//文字的颜色
    private int circle_width;//圆的宽度
    private int text_size;//文字的大小
    private float max_progress;//当前最大进度值
    private int is_sector;//是否绘扇形
    private Paint paint;
    private int style;//进度条显示的样式
    public static final int FRACTION = 1;//分数表示
    public static final int PRECENT = 2;//%表示
    public static final int EMPTY = 3;//不需要文字显示
    private int view_size;//自定义view的大小

    public MyProgressBar(Context context) {
        super(context);
        //初始化画笔
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public MyProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MyProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);

    }

    private void init(Context context, AttributeSet attrs) {
        //获取自定义的属性值
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyProgressBar);
        circle_color = typedArray.getColor(R.styleable.MyProgressBar_circle_color, Color.RED);
        circle_progregg_color = typedArray.getColor(R.styleable.MyProgressBar_circle_progress_color, Color.GREEN);
        text_color = typedArray.getColor(R.styleable.MyProgressBar_text_color, Color.WHITE);
        circle_width = typedArray.getInt(R.styleable.MyProgressBar_circle_width, 20);
        text_size = typedArray.getInt(R.styleable.MyProgressBar_text_size, 30);
        max_progress = typedArray.getInt(R.styleable.MyProgressBar_max_progress, 180);
        is_sector = typedArray.getInt(R.styleable.MyProgressBar_is_sector, 0);//0代表不用扇形表示进度，1代表用扇形表示进度
        //1代表用中文"分"表示，2代表用%表示进度，3代表圆内部是空的，不要文字
        style = typedArray.getInt(R.styleable.MyProgressBar_style, 3);
//初始化画笔
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (view_size == 0) {
            int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);
            if (wSpecSize != hSpecSize) {
                view_size = Math.max(wSpecSize, hSpecSize);
            } else
                view_size = wSpecSize;
        }
        setMeasuredDimension(view_size, view_size);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        int centre = getWidth() / 2;
        int radius = centre - circle_width / 2;
        paint.setColor(circle_color);
        paint.setStrokeWidth(circle_width);
        RectF o = new RectF(centre - radius, centre - radius, centre + radius, centre + radius);
        if (is_sector == 1) {
            //绘制背景
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawCircle(centre, centre, radius, paint);
            //绘制当前的进度
            paint.setColor(circle_progregg_color);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawArc(o, 270, max_progress, true, paint);
        } else {
            //绘制背景
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(centre, centre, radius, paint);
            //绘制当前的进度
            paint.setColor(circle_progregg_color);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawArc(o, 270, max_progress, false, paint);
        }

        //绘制文字
        paint.setColor(text_color);
        paint.setTextSize(text_size);
        paint.setStrokeWidth(0);//记得设置画笔的宽度为0，否则文字绘制可能会乱
        switch (style) {
            case FRACTION:
                String content = (int) (max_progress / 3.6) + "分";
                float text_width = paint.measureText(content);
                canvas.drawText(content, centre - text_width / 2, centre + text_size / 2, paint);

                break;
            case PRECENT:
                String content2 ="";
                if (max_progress == 0){
                    content2 = "下载";
                }else if (max_progress == 360){

                    content2 = "成功";

                }else {
                    content2 = (int) (max_progress / 3.6) + "%";

                }
                float text_width2 = paint.measureText(content2);
                Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
                int baseline = (getMeasuredHeight() - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
                canvas.drawText(content2,centre - text_width2 / 2, baseline, paint);
//                canvas.drawText(content2, centre - text_width2 / 2, centre + text_size / 2, paint);
                break;
            case EMPTY:

                break;
        }
        super.onDraw(canvas);
    }

    /**
     * 设置圆环的颜色
     *
     * @param circle_color
     * @return
     */
    public MyProgressBar setCircle_color(int circle_color) {
        this.circle_color = circle_color;
        return this;
    }

    /**
     * 设置进度条的颜色
     *
     * @param circle_progregg_color
     * @return
     */
    public MyProgressBar setCircle_progregg_color(int circle_progregg_color) {
        this.circle_progregg_color = circle_progregg_color;
        return this;
    }

    /**
     * 设置text的颜色
     *
     * @param text_color
     * @return
     */
    public MyProgressBar setText_color(int text_color) {
        this.text_color = text_color;
        return this;
    }

    /**
     * 设置圆环的宽度
     *
     * @param circle_width
     * @return
     */
    public MyProgressBar setCircle_width(int circle_width) {
        this.circle_width = circle_width;
        return this;
    }

    /**
     * 设置文字的大小
     *
     * @param text_size
     * @return
     */
    public MyProgressBar setText_size(int text_size) {
        this.text_size = text_size;
        return this;
    }

    /**
     * 设置当前最大的进度
     *
     * @param max_progress
     * @return
     */
    public MyProgressBar setMax_progress(int max_progress) {
        this.max_progress = (float) (max_progress * 3.6);
        return this;
    }

    /**
     * 设置是否需要用扇形展示
     *
     * @param is_sector "1"代表用扇形展示，"0"代表用圆弧展示
     * @return
     */
    public MyProgressBar setIs_sector(int is_sector) {
        this.is_sector = is_sector;
        return this;
    }


    /**
     * 设置样式
     *
     * @param style "FRACTION或是1"代表用"分"展示，"PRECENT或是2"代表用"%"展示，"EMPTY或是3"代表不需要文字展示
     * @return
     */
    public MyProgressBar setStyle(int style) {
        this.style = style;
        return this;
    }

    /**
     * 注意：此方法只用在通过代码new出该自定义view时
     * 设置view的大小
     *
     * @param viewSize 大小
     * @return
     */
    public MyProgressBar setViewsize(int viewSize) {
        this.view_size = viewSize;
        return this;
    }
}