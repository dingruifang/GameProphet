package cn.jayneo.gameprophet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;

import cn.jayneo.gameprophet.listener.FileListener;
import cn.jayneo.gameprophet.service.FloatingService;
import cn.jayneo.gameprophet.utils.ZipUtils;

public class MainActivity extends AppCompatActivity {

    int screenWidth = 1080;
    FileListener fileListener;
    Intent intent;
    Switch mSwitch1;
    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        fileListener = new FileListener("/sdcard/Prophet", this);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }
        mSwitch1 = (Switch) findViewById(R.id.switch1);
        mSwitch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    start();
                }else {
                    stop();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults[1] == 0){
                //开启新线程解压图片
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File imgsDir = new File("/sdcard/Prophet/imgs/");
                        if (!imgsDir.exists()){
                            try {
                                ZipUtils.UnZipAssetsFolder(MainActivity.this,"imgs.zip", "/sdcard/Prophet/imgs");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            } else if (grantResults[1] == -1){
                Toast.makeText(this, "无文件读写权限将无法正常使用!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "战术目镜已启动~", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "战术目镜已启动~请再次点击按钮!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 开启位置检测
     */
    public void start(){
        if (FloatingService.isStarted) {
            return;
        }
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "嘿!我需要权限,老伙计~", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 1);
        } else {
            File imgsDir = new File("/sdcard/Prophet/imgs/");
            if (!imgsDir.exists()){
                try {
                    ZipUtils.UnZipAssetsFolder(MainActivity.this,"imgs.zip", "/sdcard/Prophet/imgs");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            intent = new Intent(this, FloatingService.class);
            intent.putExtra(FloatingService.ACTION, FloatingService.SHOW);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            fileListener.startWatching();
            Toast.makeText(this, "战术目镜已启动!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 关闭位置检测
     */
    public void stop(){
        intent = new Intent(this, FloatingService.class);
        intent.putExtra(FloatingService.ACTION, FloatingService.HIDE);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        fileListener.stopWatching();
        stopService(intent);
        Toast.makeText(this, "已关闭", Toast.LENGTH_SHORT).show();
    }

}
