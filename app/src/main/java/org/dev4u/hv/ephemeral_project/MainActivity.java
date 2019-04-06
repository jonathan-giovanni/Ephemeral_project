package org.dev4u.hv.ephemeral_project;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


import com.akexorcist.screenshotdetection.ScreenshotDetectionDelegate;
import com.github.piasy.rxscreenshotdetector.RxScreenshotDetector;
import com.trello.rxlifecycle2.components.RxActivity;

import java.util.List;
import java.util.logging.Logger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends RxActivity implements ScreenshotDetectionDelegate.ScreenshotDetectionListener {

    private final Logger log = Logger.getLogger(getClass().getName());
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 3009;

    private ScreenshotDetectionDelegate screenshotDetectionDelegate = new ScreenshotDetectionDelegate(this, this);


    private TextView lblText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        lblText = findViewById(R.id.lblText);

        log.info("iniciado activity");

        checkReadExternalStoragePermission();


    }

    @Override
    protected void onResume() {
        super.onResume();
        RxScreenshotDetector.start(this)
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(path -> lblText.setText("\nScreenshot: " + path),
                        Throwable::printStackTrace);

        if(isEmulator()){
            lblText.setText("Iniciado en emulador ");
        }else{
            lblText.setText("No se inicio en emulador ");
        }
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        screenshotDetectionDelegate.startScreenshotDetection();

    }

    @Override
    protected void onStop() {
        super.onStop();
        screenshotDetectionDelegate.stopScreenshotDetection();
    }



    //permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showReadExternalStoragePermissionDeniedMessage();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void checkReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestReadExternalStoragePermission();
        }
    }

    private void requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION);
    }

    private void showReadExternalStoragePermissionDeniedMessage() {
        Toast.makeText(this, "Read external storage permission has denied", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onScreenCaptured(String path) {

        Toast.makeText(this, "ruta "+path, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onScreenCapturedWithDeniedPermission() {
        Toast.makeText(this, "permiso denegado", Toast.LENGTH_SHORT).show();
    }




}
