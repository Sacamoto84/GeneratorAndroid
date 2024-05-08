package com.example.generator2.util;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ADD_VOICEMAIL;
import static android.Manifest.permission.ANSWER_PHONE_CALLS;
import static android.Manifest.permission.BODY_SENSORS;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.PROCESS_OUTGOING_CALLS;
import static android.Manifest.permission.READ_CALENDAR;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECEIVE_MMS;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.RECEIVE_WAP_PUSH;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.USE_SIP;
import static android.Manifest.permission.WRITE_CALENDAR;
import static android.Manifest.permission.WRITE_CALL_LOG;
import static android.Manifest.permission.WRITE_CONTACTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import static com.example.generator2.features.initialization.utils.ReadFileMod2048byteKt.readFileMod2048byte;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.snatik.storage.Storage;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class Utils {

    public static Context ContextMainActivity;

    public static String patchDocument = "";
    public static String patchCarrier = "";
    public static String patchMod = "";

    private static final Integer[] CHANNEL_COUNT_OPTIONS = {1, 2, 3, 4, 5, 6, 7, 8};
    private static final int REQUEST_SINGLE_PERMISSION = 102;

    public static String[] PERMISSIONS = {
            READ_CALENDAR, WRITE_CALENDAR, READ_CALL_LOG, WRITE_CALL_LOG, PROCESS_OUTGOING_CALLS, CAMERA, READ_CONTACTS, WRITE_CONTACTS,
            GET_ACCOUNTS, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, RECORD_AUDIO, READ_PHONE_STATE, READ_PHONE_NUMBERS, CALL_PHONE, ANSWER_PHONE_CALLS, ADD_VOICEMAIL, USE_SIP, BODY_SENSORS, SEND_SMS, RECEIVE_SMS, READ_SMS, RECEIVE_WAP_PUSH, RECEIVE_MMS, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE
    };

    public static boolean checkPermissions(Activity c, int[] pchk) {
        List<String> permissionlist = new ArrayList<>();

        for (int i : pchk) {
            if (ContextCompat.checkSelfPermission(c, PERMISSIONS[i - 1])
                    != PackageManager.PERMISSION_GRANTED) {
                permissionlist.add(PERMISSIONS[i - 1]);
            }
        }

        if (!permissionlist.isEmpty()) {
            ActivityCompat.requestPermissions(c, permissionlist.toArray(new String[permissionlist.size()]), 101);
            return false;
        }
        return true;
    }

    /**
     * Получить список файлов по пути
     *
     * @return Список String[] файлов в папке
     */
    public static String[] listFileInDir(String path) {
        Log.d("Utils", "listFileInDir(" + path + ")");
        Storage storage = new Storage(ContextMainActivity);
        List<File> files = storage.getFiles(path);
        String[] strFiles = new String[files.size()];
        int i = 0;
        for (File file : files) {
            Log.d("Utils", "Files = " + file.getName());
            strFiles[i++] = file.getName();
        }
        Arrays.sort(strFiles);
        return strFiles;
    }

    /**
     * Получить список файлов в папке Carrier
     *
     * @return Список String[] файлов в папке
     */
    public static String[] listFileInCarrier() {
        Timber.tag("Utils").d("listFileInCarrier()");
        String path = patchCarrier;
        return listFileInDir(path);
    }

    /**
     * Получить список файлов в папке Mod
     *
     * @return Список String[] файлов в папке
     */
    public static String[] listFileInMod() {
        Timber.tag("Utils").d("listFileInMod()");
        String path = patchMod;
        return listFileInDir(path);
    }


//    public static Bitmap CreateBitmapModulation(String path) {
//        Bitmap bitmap = Bitmap.createBitmap(1024, 512, Bitmap.Config.RGB_565);
//
//        byte[] array8 = readFileMod2048byte(path); //Получим массив 8 бит
//
//        if (array8.length != 2048) {
//            Timber.tag("!ERROR!").i("CreateBitmapModulation:readFileMod2048byte len:" + Integer.toString(array8.length));
//            bitmap.eraseColor(Color.RED); // Закрашиваем синим цветом
//            return bitmap;
//        }
//
//        bitmap.eraseColor(Color.BLACK); // Закрашиваем синим цветом
//
//        int i = 0;
//
//        int[] arrayU8 = new int[2048];
//
//        for (i = 0; i < 2048; i++)
//            arrayU8[i] = Byte.toUnsignedInt(array8[i]);
//
//        int[] arrayInt = new int[1024];
//
//
//        for (i = 0; i < 1024; i++) {
//            arrayInt[i] = ((arrayU8[i * 2 + 1]) * 256) + (arrayU8[i * 2]);
//        }
//
//        Paint mPaint = new Paint();
//
//        Canvas c = new Canvas();
//        c.setBitmap(bitmap);
//        mPaint.setStyle(Paint.Style.STROKE);
//
//        mPaint.setStrokeWidth(4);
//        mPaint.setColor(Color.DKGRAY);
//        c.drawLine(512, 0, 512, 512, mPaint);
//
//        mPaint.setColor(Color.GREEN);
//        mPaint.setStrokeWidth(10);
//
//
//        for (i = 0; i < 512; i++) {
//            c.drawLine(i, 32 + (float) (4095 - arrayInt[i * 2]) / 18, i, (float) (arrayInt[i * 2]) / 18 + 256, mPaint);
//        }
//
//        for (i = 0; i < 512; i++) {
//            c.drawLine(i + 512, 32 + (float) (4095 - arrayInt[i * 2]) / 18, i + 512, (float) (arrayInt[i * 2]) / 18 + 256, mPaint);
//        }
//
//        mPaint.setStrokeWidth(2);
//        //mPaint.setColor(Color.BLUE);
//        //mPaint.setPathEffect(new DashPathEffect(new float[] { 20F, 30F, 40F, 50F}, 0));
//        Path pathLine = new Path();
//        pathLine.moveTo(0, 256);
//        pathLine.lineTo(0, 256);
//        pathLine.lineTo(1023, 255);
//        //c.drawPath(pathLine, mPaint);
//        c.drawLine(0, 256, 1023, 256, mPaint);
//
//        return bitmap;
//    }



    public static float ConvertValueToP(int min, int step, double value) {
        return (float) (value - min) / step;
    }

    //Конвертируем частоту в позицию
    public static int ConvertAM_FM_FloatValueToP(double value) {
        if (value < 10.0)
            return (int) (value * 10.0);
        else
            return (int) (value + 90);
    }

    //Конвертируем позицию в частоту
    public static float ConvertAM_FM_FloatPToValue(int position) {
        float res = 0;

        if (position < 100)
            res = position / 10.0F;
        else
            res = 10 + (position - 100);

        return res;
    }


}
