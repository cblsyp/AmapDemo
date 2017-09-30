package com.yixitongda.amapdemo.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2017/9/29.
 */

public class StatusBarUtils {

    private static final String TAG = StatusBarUtils.class.getSimpleName();

    public static void addStatusBarBehind(Activity activity, @ColorInt int color, int statusBarAlpha) {
//获取windowphone下面的DecorView
        ViewGroup decorview = (ViewGroup) activity.getWindow().getDecorView();

        int count = decorview.getChildCount();
        //判断是否添加了stausBarView
        if (count > 0 && decorview.getChildAt(count - 1) instanceof StatusBarView) {
            decorview.getChildAt(count - 1).setBackgroundColor(caculateStatusColor(color, statusBarAlpha));
        } else {
            //新建一个和状态栏高宽的View
            StatusBarView statusbarveiw = createStatusBarView(activity, color, statusBarAlpha);
            decorview.addView(statusbarveiw);
        }
        setRootView(activity);
    }


    public static void setTranslucentImageHeader(Activity activity, int alpha, View needoffsetView) {
        setFullScreen(activity);
        //获取windowphone下满的decorview
        ViewGroup decorview = (ViewGroup) activity.getWindow().getDecorView();

        int count = decorview.getChildCount();
        if (count > 0 && decorview.getChildAt(count - 1) instanceof StatusBarView) {
            decorview.getChildAt(count - 1).setBackgroundColor(Color.argb(alpha, 0, 0, 0));
        } else {
            StatusBarView s = createTranslucentStatusbarView(activity, alpha);
            decorview.addView(s);
        }
        if (needoffsetView != null) {
            ViewGroup.MarginLayoutParams layoutparams = (ViewGroup.MarginLayoutParams) needoffsetView.getLayoutParams();
            layoutparams.setMargins(0, getStatusbarHeight(activity), 0, 0);
        }
    }

    /**
     * 为该视图的activity所属的窗口满屏
     *
     * @param activity
     */
    public static void setFullScreen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.e(TAG,"5.0");
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //设置透明的状态栏 这样才能让ContentView向上
            Log.e(TAG,"4.4");
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 获取状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusbarHeight(Context context) {
        //状态栏的高度在  包为:android   类型为: dimen  然后键的名字为status_bar_height   identifier  n. 标识符
        int resourseId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelOffset(resourseId);
    }

    /**
     * 获取通知栏的高度  利用反射
     *int a=StatusBarUtils.getStatusbarHeight(this);
     Log.e(TAG,"利用反射获取的状态栏的高度:"+a);
     a=50
     通过单元测试
     * @param context
     * @return
     */
    public int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;

        try {
            //  Class<?>  class=Class.forName("com.android.internal.R$dimen");  命名冲突 不能用class  用作变量名字

            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object obj=clazz.newInstance();
            Field field=clazz.getField("status_bar_height");

            int temp=Integer.parseInt(field.get(obj).toString());

            statusBarHeight=context.getResources().getDimensionPixelSize(temp);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * must be know the actual meaning
     *
     * @param color
     * @param alpha alpha
     * @return
     */
    private static int caculateStatusColor(int color, int alpha) {
        float a = 1 - alpha / 255;
        int red = color >> 16 & 0xff;
        int green = color >> 8 & 0xff;
        int blue = color & 0xff;

        red = (int) (red * a + 0.5);
        green = (int) (green * a + 0.5);
        blue = (int) (blue * a + 0.5);

        return 0xff << 24 | red << 16 | green << 8 | blue;

    }

    /**
     * @param activity
     */
    public static void setRootView(Activity activity) {
        ViewGroup rootview = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        //rootview不会为状态栏留出状态空间
        ViewCompat.setFitsSystemWindows(rootview, true);
        rootview.setClipToPadding(true);

    }

    /**
     * 绘制一个和状态栏一样高的矩形
     *
     * @param activity
     * @param color
     * @param alpha
     * @return
     */
    public static StatusBarView createStatusBarView(Activity activity, int color, int alpha) {
        StatusBarView statusbarview = new StatusBarView(activity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusbarHeight(activity));
        statusbarview.setLayoutParams(params);
        statusbarview.setBackgroundColor(caculateStatusColor(color, alpha));
        return statusbarview;
    }

    public static class StatusBarView extends View {

        public StatusBarView(Context context) {
            super(context);
        }

        public StatusBarView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public StatusBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }
    }

    private static StatusBarView createTranslucentStatusbarView(Activity activity, int alpha) {
        StatusBarView statusview = new StatusBarView(activity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getStatusbarHeight(activity));
        statusview.setLayoutParams(params);
        statusview.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
        return statusview;
    }

    @SuppressLint("NewApi")
    public static void hideSystemUI(View v) {
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @SuppressLint("NewApi")
    public static void showSystemUI(View v) {
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
