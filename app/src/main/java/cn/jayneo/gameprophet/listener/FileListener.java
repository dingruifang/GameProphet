package cn.jayneo.gameprophet.listener;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.FileObserver;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import cn.jayneo.gameprophet.service.FloatingService;

public class FileListener extends FileObserver {

    private Context context;
    private Intent intent;
    private int screenWidth;

    public FileListener(String path, Context context) {
        super(path);
        //this.screenWidth = screenWidth;
        this.context = context;
    }

    /**
     * 读取本地文件内容
     * @param file
     * @return
     */
    public static String getFileContent(File file) {
        String content = "";
        if (!file.isDirectory()) {  //检查此路径名的文件是否是一个目录(文件夹)
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader
                            = new InputStreamReader(instream, "UTF-8");
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line = "";
                    while ((line = buffreader.readLine()) != null) {
                        content += line;
                    }
                    instream.close();//关闭输入流
                }
            } catch (java.io.FileNotFoundException e) {
            } catch (IOException e) {
            }
        }
        return content;
    }

    private static void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);
        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent;
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

//生成文件

    private static File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

//生成文件夹

    private static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }


    public void move(){
        //坐标读取
        File file = new File("/sdcard/Prophet/Position.txt");
        String fileContent = FileListener.getFileContent(file);
        if (fileContent == null){
            return;
        }
        String[] concent = fileContent.split(";");
        //角色id读取
        file = new File("/sdcard/Prophet/ID.log");
        fileContent = FileListener.getFileContent(file);
        if (fileContent == null){
            return;
        }
        String[] hero = fileContent.split(",");
        //偏移值读取
        file = new File("/sdcard/Prophet/Offset.log");
        fileContent = FileListener.getFileContent(file);
        if (fileContent == null){
            return;
        }
        String[] offset = fileContent.split(",");
        if (concent.length == 2) {
            String concentXY = concent[0];
            String concentLife = concent[1];
            String[] xy = concentXY.split(",");
            String[] life = concentLife.split(",");
            intent = new Intent(context, FloatingService.class);
            intent.putExtra(FloatingService.ACTION, FloatingService.UPDATE);
            Bundle bundle = new Bundle();
            bundle.putSerializable("xy", xy);
            bundle.putSerializable("life", life);
            bundle.putSerializable("hero", hero);
            bundle.putSerializable("offset", offset);
            intent.putExtras(bundle);
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }

    @Override
    public void onEvent(int event, String path) {
        switch (event) {
            case FileObserver.MODIFY:
                move();
                break;
        }
    }
}
