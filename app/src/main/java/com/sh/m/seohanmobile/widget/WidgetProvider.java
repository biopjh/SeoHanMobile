package com.sh.m.seohanmobile.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.sh.m.seohanmobile.MainActivity;
import com.sh.m.seohanmobile.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WidgetProvider extends AppWidgetProvider {
    /**
     * 브로드캐스트를 수신할때, Override된 콜백 메소드가 호출되기 직전에 호출됨
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    /**
     * 위젯을 갱신할때 호출됨
     *
     * 주의 : Configure Activity를 정의했을때는 위젯 등록시 처음 한번은 호출이 되지 않습니다
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        /**
         * 등록된 모든 위젯 id를 가져옵니다
         *
         * 일괄 업데이트가 가능합니다
         */
        appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(
                context, getClass()));

        for (int i = 0; i < appWidgetIds.length; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    /**
     * 위젯이 처음 생성될때 호출됨
     *
     * 동일한 위젯이 생성되도 최초 생성때만 호출됨
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    /**
     * 위젯의 마지막 인스턴스가 제거될때 호출됨
     *
     * onEnabled()에서 정의한 리소스 정리할때
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    /**
     * 위젯이 사용자에 의해 제거될때 호출됨
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * 위젯의 상태(크기)가 변경될때마다 호출되는 메소드
     */
    @SuppressLint({ "NewApi" })
    @Override
    public void onAppWidgetOptionsChanged(Context context,
                                          AppWidgetManager appWidgetManager, int appWidgetId,
                                          Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
                newOptions);
        /**
         * 현재 시간 정보를 가져오기 위한 Calendar
         */
        Calendar mCalendar = Calendar.getInstance();
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                Locale.KOREA);

        int minWidth = newOptions
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int maxWidth = newOptions
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int minHeight = newOptions
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int maxHeight = newOptions
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

        RemoteViews updateViews = null;

        Log.d("minWidth", "" + minWidth);
        Log.d("maxWidth", "" + maxWidth);
        Log.d("minHeight", "" + minHeight);
        Log.d("maxHeight", "" + maxHeight);
        Log.d(" ", " ");

        if (maxWidth >= 456) {
            updateViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout4x);
        } else {
            updateViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
        }

        updateViews.setTextViewText(R.id.mText,
                mFormat.format(mCalendar.getTime()));

        /**
         * 레이아웃을 클릭하면 앱 실행
         */
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName(context, MainActivity.class));
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0, intent,0);
        updateViews.setOnClickPendingIntent(R.id.mLayout,pendingIntent);
        Log.d("test Widget : ", "updateAppWidget135");

        /**
         * 레이아웃을 클릭하면 홈페이지 이동
         */
/*        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://itmir.tistory.com/"));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, 0);
        updateViews.setOnClickPendingIntent(R.id.mLayout, pendingIntent);*/

        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
    }

    public static void updateAppWidget(Context context,
                                       AppWidgetManager appWidgetManager, int appWidgetId) {
        /**
         * 현재 시간 정보를 가져오기 위한 Calendar
         */
        Calendar mCalendar = Calendar.getInstance();
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                Locale.KOREA);

        /**
         * RemoteViews를 이용해 Text설정
         */
        RemoteViews updateViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_layout);

        updateViews.setTextViewText(R.id.mText,
                mFormat.format(mCalendar.getTime()));

        /**
         * 레이아웃을 클릭하면 앱 실행
         */
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName(context, MainActivity.class));
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0, intent,0);
        updateViews.setOnClickPendingIntent(R.id.mLayout,pendingIntent);
        Log.d("test Widget : ", "updateAppWidget165");
        /**
         * 레이아웃을 클릭하면 홈페이지 이동
         */
/*
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://itmir.tistory.com/"));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, 0);
        updateViews.setOnClickPendingIntent(R.id.mLayout, pendingIntent);
*/

        /**
         * 위젯 업데이트
         */
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
    }

}