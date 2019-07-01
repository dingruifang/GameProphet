package cn.jayneo.gameprophet.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import cn.jayneo.gameprophet.view.FloatingView;

public class FloatingService extends Service {
    public static boolean isStarted = false;
    public static final String ACTION = "action";
    public static final String SHOW = "show";
    public static final String HIDE = "hide";
    public static final String UPDATE = "update";
    private final static int GRAY_SERVICE_ID = 1001;
    private FloatingView mFloatingView;
    private FloatingView mFloatingView2;
    private FloatingView mFloatingView3;
    private FloatingView mFloatingView4;
    private FloatingView mFloatingView5;

    public IBinder mBinder = new FloatingService.LocalBinder();

    public class LocalBinder extends Binder {
        // 在Binder中定义一个自定义的接口用于数据交互
        // 这里直接把当前的服务传回给宿主
        public FloatingService getService() {
            return FloatingService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        mFloatingView = new FloatingView(this);
        mFloatingView2 = new FloatingView(this);
        mFloatingView3 = new FloatingView(this);
        mFloatingView4 = new FloatingView(this);
        mFloatingView5 = new FloatingView(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //设置service为前台服务，提高优先级
        if (Build.VERSION.SDK_INT < 18) {
            //Android4.3以下 ，此方法能有效隐藏Notification上的图标
            startForeground(GRAY_SERVICE_ID, new Notification());
        } else if(Build.VERSION.SDK_INT>18 && Build.VERSION.SDK_INT<25){
            //Android4.3 - Android7.0，此方法能有效隐藏Notification上的图标
            Intent innerIntent = new Intent(this, GrayInnerService.class);
            startService(innerIntent);
            startForeground(GRAY_SERVICE_ID, new Notification());
        }else if (Build.VERSION.SDK_INT<26){
            //Android7.1 google修复了此漏洞，暂无解决方法（现状：Android7.1以上app启动后通知栏会出现一条"正在运行"的通知消息）
            startForeground(GRAY_SERVICE_ID, new Notification());
        }else{
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel(GRAY_SERVICE_ID+"", "GameProphet", NotificationManager.IMPORTANCE_NONE));
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, GRAY_SERVICE_ID+"");
            startForeground(1 ,builder.build());
        }
        if (intent != null) {
            String action = intent.getStringExtra(ACTION);
            if (SHOW.equals(action)) {
                mFloatingView.show();
                mFloatingView2.show();
                mFloatingView3.show();
                mFloatingView4.show();
                mFloatingView5.show();
            } else if (HIDE.equals(action)) {
                mFloatingView.hide();
                mFloatingView = null;
                mFloatingView2.hide();
                mFloatingView2 = null;
                mFloatingView3.hide();
                mFloatingView3 = null;
                mFloatingView4.hide();
                mFloatingView4 = null;
                mFloatingView5.hide();
                mFloatingView5 = null;
            } else if (UPDATE.equals(action)) {
                String[] xys = intent.getStringArrayExtra("xy");
                String[] life = intent.getStringArrayExtra("life");
                String[] heroes = intent.getStringArrayExtra("hero");
                String[] offset = intent.getStringArrayExtra("offset");
                setXY(xys,offset);
                setLife(life);
                setId(heroes);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void setXY(String[] xys, String[] offset) {
        int offsetX = 0;
        int offsetY = 0;
        if (offset.length == 2){
            offsetX = Integer.parseInt(offset[0]);
            offsetY = Integer.parseInt(offset[1]);
        }
        int[] xy = new int[xys.length];
        String str = "";
        for (int i = 0; i < xys.length; i++) {
            if (xys[i].indexOf(".") != -1){
                str = xys[i].substring(0, xys[i].indexOf("."));
            }else{
                str = xys[i];
            }
            xy[i] = Integer.parseInt(str);
        }
        mFloatingView.setXY(xy[0] + offsetX, xy[1] + offsetY);
        mFloatingView2.setXY(xy[2] + offsetX, xy[3] + offsetY);
        mFloatingView3.setXY(xy[4] +offsetX, xy[5] + offsetY);
        mFloatingView4.setXY(xy[6] + offsetX, xy[7] + offsetY);
        mFloatingView5.setXY(xy[8] + offsetX, xy[9] + offsetY);
    }

    public void setLife(String[] lifes){
        mFloatingView.setLife(lifes[0]);
        mFloatingView2.setLife(lifes[1]);
        mFloatingView3.setLife(lifes[2]);
        mFloatingView4.setLife(lifes[3]);
        mFloatingView5.setLife(lifes[4]);
    }

    public void setId(String[] ids) {
        mFloatingView.setId(ids[0], this);
        mFloatingView2.setId(ids[1], this);
        mFloatingView3.setId(ids[2], this);
        mFloatingView4.setId(ids[3], this);
        mFloatingView5.setId(ids[4], this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStarted = false;
    }
    public static class GrayInnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}



