package com.rockchip.tscalibration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/* loaded from: classes.dex */
public class TSCalibration extends Activity {
    private ImageView notification;
    public ProgressDialog pd;
    private ImageView touchPoint;
    private final String TAG = "TSCalibration";
    private AppToDriver mATD = new AppToDriver();
    private int currTouch = 0;
    private Point[] calibrateAD = new Point[5];
    private Point curTouchAdc = null;
    private final int TOUCH_TOP_LEFT = 0;
    private final int TOUCH_TOP_RIGHT = 1;
    private final int TOUCH_BOTTOM_LEFT = 2;
    private final int TOUCH_BOTTOM_RIGHT = 3;
    private final int TOUCH_CENTER = 4;
    private final int TOUCH_CHECK_END = 5;
    private String locale = null;
    Handler mOpenHandler = new Handler();
    Runnable mOpeningRun = new Runnable() { // from class: com.rockchip.tscalibration.TSCalibration.3
        @Override // java.lang.Runnable
        public void run() {
            TSCalibration.this.mOpenHandler.removeCallbacks(TSCalibration.this.mOpeningRun);
            TSCalibration.this.pd.dismiss();
            TSCalibration.this.onDestroy();
        }
    };

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
    }

    private void setupViews() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(1);
        hideSystemUi();
        setContentView(R.layout.main);
        for (int i = 0; i < 5; i++) {
            this.calibrateAD[i] = new Point(-1, -1);
        }
        Configuration configuration = getResources().getConfiguration();
        this.locale = configuration.locale.toString();
        this.touchPoint = (ImageView) findViewById(R.id.touch_point1);
        this.notification = (ImageView) findViewById(R.id.notification);
        this.touchPoint.setBackgroundDrawable(getResources().getDrawable(R.drawable.touch_point));
        if (this.locale.equals("zh_CN")) {
            this.notification.setBackgroundDrawable(getResources().getDrawable(R.drawable.notification_zh));
        } else {
            this.notification.setBackgroundDrawable(getResources().getDrawable(R.drawable.notification));
        }
    }

    /** Keep calibration usable on the tablet UI, whose system bar is at the bottom. */
    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUi();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        PackageManager pm = getPackageManager();
        ComponentName name = new ComponentName(this, TSCalibration.class);
        pm.setComponentEnabledSetting(name, 2, 0);
        System.exit(0);
        finish();
    }

    @Override // android.app.Activity
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == 0 && this.currTouch != 5) {
            this.curTouchAdc = this.mATD.getPointAdc();
            this.calibrateAD[this.currTouch].x = this.curTouchAdc.x;
            this.calibrateAD[this.currTouch].y = this.curTouchAdc.y;
            this.currTouch++;
            switch (this.currTouch) {
                case 1:
                    this.touchPoint.setVisibility(8);
                    this.touchPoint = (ImageView) findViewById(R.id.touch_point2);
                    this.touchPoint.setVisibility(0);
                    this.touchPoint.setBackgroundDrawable(getResources().getDrawable(R.drawable.touch_point));
                    break;
                case 2:
                    this.touchPoint.setVisibility(4);
                    this.touchPoint = (ImageView) findViewById(R.id.touch_point3);
                    this.touchPoint.setVisibility(0);
                    this.touchPoint.setBackgroundDrawable(getResources().getDrawable(R.drawable.touch_point));
                    break;
                case 3:
                    this.touchPoint.setVisibility(8);
                    this.touchPoint = (ImageView) findViewById(R.id.touch_point4);
                    this.touchPoint.setVisibility(0);
                    this.touchPoint.setBackgroundDrawable(getResources().getDrawable(R.drawable.touch_point));
                    break;
                case 4:
                    this.touchPoint.setVisibility(8);
                    if (!this.locale.equals("zh_CN")) {
                        this.notification.setBackgroundDrawable(getResources().getDrawable(R.drawable.notification_done));
                        break;
                    } else {
                        this.notification.setBackgroundDrawable(getResources().getDrawable(R.drawable.notification_done_zh));
                        break;
                    }
            }
            if (this.currTouch == 5) {
                this.mATD.setCalibrateAD(this.calibrateAD);
                if (this.mATD.doCheckCalibration()) {
                    saveToPreference();
                    this.pd = ProgressDialog.show(this, getString(R.string.adjust_touch_screen_finish), getString(R.string.adjust_touch_screen_finish_data), true, false);
                    this.mOpenHandler.postDelayed(this.mOpeningRun, 1000L);
                } else {
                    int queryForce = Integer.parseInt(SystemProperties.get("app.tscalibration.force", "0"));
                    if (queryForce == 1) {
                        this.touchPoint = (ImageView) findViewById(R.id.touch_point2);
                        this.touchPoint.setVisibility(8);
                        this.touchPoint = (ImageView) findViewById(R.id.touch_point1);
                        this.touchPoint.setVisibility(0);
                        this.touchPoint.setBackgroundDrawable(getResources().getDrawable(R.drawable.touch_point));
                        if (this.locale.equals("zh_CN")) {
                            this.notification.setBackgroundDrawable(getResources().getDrawable(R.drawable.notification_zh));
                        } else {
                            this.notification.setBackgroundDrawable(getResources().getDrawable(R.drawable.notification));
                        }
                        this.currTouch = 0;
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setIcon(R.drawable.alertdialog).setTitle(R.string.alert_dialog).setMessage(R.string.alert_dialog_message).setCancelable(false).setPositiveButton(getString(R.string.home_screen), new DialogInterface.OnClickListener() { // from class: com.rockchip.tscalibration.TSCalibration.2
                            @Override // android.content.DialogInterface.OnClickListener
                            public void onClick(DialogInterface dialog, int id) {
                                TSCalibration.this.onDestroy();
                                TSCalibration.this.finish();
                            }
                        }).setNegativeButton(getString(R.string.try_again), new DialogInterface.OnClickListener() { // from class: com.rockchip.tscalibration.TSCalibration.1
                            @Override // android.content.DialogInterface.OnClickListener
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                TSCalibration.this.touchPoint = (ImageView) TSCalibration.this.findViewById(R.id.touch_point2);
                                TSCalibration.this.touchPoint.setVisibility(8);
                                TSCalibration.this.touchPoint = (ImageView) TSCalibration.this.findViewById(R.id.touch_point1);
                                TSCalibration.this.touchPoint.setVisibility(0);
                                TSCalibration.this.touchPoint.setBackgroundDrawable(TSCalibration.this.getResources().getDrawable(R.drawable.touch_point));
                                if (TSCalibration.this.locale.equals("zh_CN")) {
                                    TSCalibration.this.notification.setBackgroundDrawable(TSCalibration.this.getResources().getDrawable(R.drawable.notification_zh));
                                } else {
                                    TSCalibration.this.notification.setBackgroundDrawable(TSCalibration.this.getResources().getDrawable(R.drawable.notification));
                                }
                                TSCalibration.this.currTouch = 0;
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            }
        }
        return true;
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == 1) {
            System.out.println("ORIENTATION_PORTRAIT");
        }
        if (newConfig.orientation == 2) {
            System.out.println("ORIENTATION_LANDSCAPE");
        }
        super.onConfigurationChanged(newConfig);
    }

    private void saveToPreference() {
        AppToDriver appToDriver = this.mATD;
        SharedPreferences prefCalibrate = getSharedPreferences(AppToDriver.TOUCH_CHECK_VALUE, 0);
        SharedPreferences.Editor ed = prefCalibrate.edit();
        for (int i = 0; i < 5; i++) {
            ed.putInt(this.mATD.strTouch[(i * 2) + 0], this.calibrateAD[i].x);
            ed.putInt(this.mATD.strTouch[(i * 2) + 1], this.calibrateAD[i].y);
        }
        ed.commit();
    }
}
