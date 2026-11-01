package android.rk.RockVideoPlayer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.rk.RockVideoPlayer.DBUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/* loaded from: classes.dex */
public class VideoPlayActivity extends Activity implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, View.OnTouchListener, DBUtils.Def, View.OnKeyListener {
    private static final String CMDNAME = "command";
    private static final String CMDPAUSE = "pause";
    private static final int CMD__ENABEL_TVOUT = 1;
    private static final boolean DEBUG = true;
    private static final int FULLSCREEN_FLAG = 3;
    private static final int HIDEPROGRESSVIEW = 19;
    private static final int MENU_ITEM__OUTPUT_TO_HDMI_576p = 1;
    private static final int MENU_ITEM__OUTPUT_TO_HDMI_720p = 2;
    private static final int MENU_ITEM__OUTPUT_TO_NTSC = 12;
    private static final int MENU_ITEM__OUTPUT_TO_PAL = 11;
    private static final int MENU_ITEM__OUTPUT_TO_YBR_480 = 13;
    private static final int MENU_ITEM__OUTPUT_TO_YBR_576 = 14;
    private static final int MENU_ITEM__OUTPUT_TO_YBR_720 = 15;
    private static final int NOT_FULLSCREEN_FLAG = 4;
    private static final long OUTPUT_OPS_DELAY_MILlIS = 600;
    private static final String SERVICECMD = "android.rk.RockAudioPlayer.musicservicecommand";
    private static final int SETVIDEOCONN = 20;
    private static final int SETVIDEOURI = 21;
    private static final String TAG = "VideoPlayActivity";
    AlertDialog HdmiAlert;
    AlertDialog TVOutAlert;
    private View Volume_view;
    private AudioManager audioMa;
    private ArrayList<ChoiceItem> choiceItems;
    private View mBlackView;
    public int mBrightMode;
    public int mBrightness;
    private boolean mDragging;
    private boolean mFinishOnCompletion;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private Object mHdmiMgr;
    private View mHelpView;
    private Runnable mInitAsync;
    public ProgressDialog mLoadDialog;
    private int mOriginalBrightness;
    private String mPath;
    PowerManager mPowerManager;
    private View mProgressView;
    private BroadcastReceiver mReceiver;
    public long mSeekTime;
    public int mSystemBacklight;
    private int mSystemVolume;
    private int mTVOutMode;
    public TextView mTotalTime;
    private Object mTvOutMgr;
    private Uri mUri;
    private VideoDisplayView mVideoDisplayView;
    public int mVolumeMode;
    PowerManager.WakeLock mWakeLock;
    private ProgressBar myProgress;
    int screenOn;
    int timeoutmode;
    private int mPositionWhenPaused = -1;
    private int mPositionWhenStop = -1;
    private boolean mWasPlayingWhenPaused = false;
    private int volume = 0;
    private final int VOLUMEPLUS = 1;
    private final int VOLUMEMINUS = -1;
    private final int SHOWTIME = 2000;
    private int mOldBrightness = 0;
    public final int mSysBright = 1;
    public final int mUserBright = 2;
    private int mOldVolume = 0;
    private boolean isScreenOff = false;
    private final int HIDE_HELPVIEW = 1;
    private final int TIME_SHORT = 5000;
    private final int TIME_MIDDLE = 8000;
    private final int TIME_LONG = 10000;
    private boolean mPauseStatus = false;
    private final int sDefaultTimeout = 4000;
    private long mStartSeekPos = 0;
    private long mEndSeekPos = 0;
    private boolean mOutputToTvOut = false;
    private final int sTVOUT_Cvbs_PAL = 0;
    private final int sTVOUT_Cvbs_NTSC = 1;
    private final int sTVOUT_Ypbpr480 = 2;
    private final int sTVOUT_Ypbpr576 = FULLSCREEN_FLAG;
    private final int sTVOUT_Ypbpr720 = NOT_FULLSCREEN_FLAG;
    private final int DIALOG_ITEM_ID__CLOSE = 5;
    private boolean mOutputToHdmi = false;
    private boolean mTVOutEnable = false;
    private boolean mHdmiEnable = false;
    private View contentView = null;
    private VideoPlayActivity mActivity = this;
    private MediaPlayer.OnErrorListener mErrorListener = this;
    private MediaPlayer.OnCompletionListener mCompletionListener = this;
    private View.OnTouchListener mTouchListener = this;
    private View.OnKeyListener mKeyListener = this;
    private boolean isActivityRunning = true;
    private KeyguardManager mKeyguardManager = null;
    private KeyguardManager.KeyguardLock mKeyguardLock = null;
    Handler mHandler = new Handler() { // from class: android.rk.RockVideoPlayer.VideoPlayActivity.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    VideoPlayActivity.this.mHelpView.setVisibility(8);
                    VideoPlayActivity.this.mVideoDisplayView.setEnable(true);
                    return;
                case 2:
                default:
                    return;
                case VideoPlayActivity.FULLSCREEN_FLAG /* 3 */:
                    VideoPlayActivity.this.updateFullscreenStatus(true);
                    return;
                case VideoPlayActivity.NOT_FULLSCREEN_FLAG /* 4 */:
                    VideoPlayActivity.this.updateFullscreenStatus(false);
                    return;
            }
        }
    };
    Runnable mPlayingChecker = new Runnable() { // from class: android.rk.RockVideoPlayer.VideoPlayActivity.2
        @Override // java.lang.Runnable
        public void run() {
            if (VideoPlayActivity.this.isActivityRunning) {
                VideoPlayActivity.this.LOG("Downlaod percent = " + VideoPlayActivity.this.mVideoDisplayView.getBufferPercentage());
                if (VideoPlayActivity.this.mVideoDisplayView.isDismiss()) {
                    VideoPlayActivity.this.LOG("+++++++++++++++++++++++++++++++++++++++++i am run A");
                    VideoPlayActivity.this.mHandler.sendEmptyMessage(VideoPlayActivity.FULLSCREEN_FLAG);
                    VideoPlayActivity.this.mProgressView.setVisibility(8);
                } else if (VideoPlayActivity.this.mVideoDisplayView.getBufferPercentage() > 0) {
                    VideoPlayActivity.this.LOG("+++++++++++++++++++++++++++++++++++++++++i am run B");
                    VideoPlayActivity.this.mProgressView.setVisibility(8);
                    VideoPlayActivity.this.mHandler.postDelayed(VideoPlayActivity.this.mLoadingChecker, 100L);
                } else {
                    VideoPlayActivity.this.mHandler.sendEmptyMessage(VideoPlayActivity.NOT_FULLSCREEN_FLAG);
                    VideoPlayActivity.this.LOG("+++++++++++++++++++++++++++++++++++++++++i am run C");
                    VideoPlayActivity.this.mHandler.postDelayed(VideoPlayActivity.this.mPlayingChecker, 250L);
                }
            }
        }
    };
    Runnable mLoadingChecker = new Runnable() { // from class: android.rk.RockVideoPlayer.VideoPlayActivity.3
        @Override // java.lang.Runnable
        public void run() {
            if (VideoPlayActivity.this.isActivityRunning) {
                System.gc();
                if (!VideoPlayActivity.this.mVideoDisplayView.mIsPrepared) {
                    StringBuilder sb = new StringBuilder();
                    String loadtitle = VideoPlayActivity.this.mContext.getResources().getString(R.string.loadtitle);
                    int precent = VideoPlayActivity.this.mVideoDisplayView.getBufferPercentage();
                    sb.append(precent);
                    sb.append("% ");
                    sb.append(loadtitle);
                    VideoPlayActivity.this.mHandler.sendEmptyMessage(VideoPlayActivity.NOT_FULLSCREEN_FLAG);
                    VideoPlayActivity.this.mHandler.postDelayed(VideoPlayActivity.this.mLoadingChecker, 250L);
                    return;
                }
                VideoPlayActivity.this.mHandler.sendEmptyMessage(VideoPlayActivity.FULLSCREEN_FLAG);
            }
        }
    };
    Handler viewHandler = new Handler() { // from class: android.rk.RockVideoPlayer.VideoPlayActivity.5
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VideoPlayActivity.HIDEPROGRESSVIEW /* 19 */:
                    VideoPlayActivity.this.mProgressView.setVisibility(8);
                    return;
                case VideoPlayActivity.SETVIDEOCONN /* 20 */:
                    VideoPlayActivity.this.mVideoDisplayView.setMediaController(new VideoController(VideoPlayActivity.this.mContext, VideoPlayActivity.this.mBrightness, VideoPlayActivity.this.mTVOutEnable, VideoPlayActivity.this.mHdmiEnable));
                    return;
                case VideoPlayActivity.SETVIDEOURI /* 21 */:
                    VideoPlayActivity.this.mVideoDisplayView.setVideoURI(VideoPlayActivity.this.mUri);
                    return;
                default:
                    return;
            }
        }
    };
    private BroadcastReceiver mScreencloseReceiver = new BroadcastReceiver() { // from class: android.rk.RockVideoPlayer.VideoPlayActivity.6
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_OFF")) {
                VideoPlayActivity.this.LOG("-------------------Screen closed!!!!--------------------");
                if (VideoPlayActivity.this.mTvOutMgr != null) {
                    VideoPlayActivity.this.disableTvOut();
                }
                if (VideoPlayActivity.this.mHdmiMgr != null) {
                    VideoPlayActivity.this.disableHdmiOutput();
                }
                VideoPlayActivity.this.mVideoDisplayView.pause();
            }
        }
    };
    private View.OnClickListener mCloseListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoPlayActivity.8
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            VideoPlayActivity.this.mHelpView.setVisibility(8);
            VideoPlayActivity.this.mVideoDisplayView.setEnable(true);
        }
    };
    private final BroadcastReceiver mHdmiBroadcastReceiver = new BroadcastReceiver() { // from class: android.rk.RockVideoPlayer.VideoPlayActivity.9
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.hardware.HdmiManager.WIFI_STATE_CHANGED")) {
                int state = intent.getIntExtra("hdmi_state", 0);
                VideoPlayActivity.this.LOG("mHdmiBroadcastReceiver.onReceive() : Received broadcast : HDMI state changed to " + Integer.toString(state));
                if (state == 0) {
                    VideoPlayActivity.this.closeOptionsMenu();
                    if (VideoPlayActivity.this.mOutputToHdmi) {
                        VideoPlayActivity.this.mOutputToHdmi = false;
                    }
                }
            }
        }
    };
    private Handler mOutputHandler = new Handler() { // from class: android.rk.RockVideoPlayer.VideoPlayActivity.10
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    VideoPlayActivity.this.LOG("mOutputHandler.handleMessage() : To enable TvOut.");
                    VideoPlayActivity.this.enableTVOut(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    };
    private Context mContext = this;

    public void LOG(String msg) {
        Log.d(TAG, msg);
    }

    public void updateFullscreenStatus(boolean bUseFullscreen) {
        LOG("***************************************");
        LOG("updateFullscreenStatus:" + bUseFullscreen);
        LOG("***************************************");
        if (bUseFullscreen) {
            getWindow().addFlags(1024);
            getWindow().clearFlags(2048);
        } else {
            getWindow().addFlags(2048);
            getWindow().clearFlags(1024);
        }
        this.contentView.requestLayout();
    }

    private void acquireWakeLock() {
        if (this.mWakeLock != null) {
            try {
                if (!this.mWakeLock.isHeld()) {
                    this.mWakeLock.acquire();
                } else {
                    LOG(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>error: wake lock is held!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void releaseWakeLock() {
        if (this.mWakeLock != null) {
            try {
                if (this.mWakeLock.isHeld()) {
                    LOG(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>now release wake lock!");
                    this.mWakeLock.release();
                    this.mWakeLock.setReferenceCounted(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void KeyguardUnLock() {
        if (this.mKeyguardLock != null) {
            try {
                this.mKeyguardLock.disableKeyguard();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void KeyguardLock() {
        if (this.mKeyguardLock != null) {
            try {
                this.mKeyguardLock.reenableKeyguard();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG("onCreate() : Entered.");
        try {
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
            LOG(">>>>>>>>>>>>>>>>>>>>>>>>>>>>now to new a wake lock");
            this.mWakeLock = this.mPowerManager.newWakeLock(10, TAG);
            LOG(">>>>>>>>>>>>>>>>>>>>>>>>>>>>now get a new wake lock");
        } catch (Exception e) {
            e.printStackTrace();
        }
        acquireWakeLock();
        Window win = getWindow();
        win.setFlags(1024, 1024);
        requestWindowFeature(1);
        setContentView(R.layout.play_display_land);
        try {
            this.mKeyguardManager = (KeyguardManager) getSystemService("keyguard");
            this.mKeyguardLock = this.mKeyguardManager.newKeyguardLock("SCService");
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        KeyguardUnLock();
        this.mInitAsync = new Thread() { // from class: android.rk.RockVideoPlayer.VideoPlayActivity.4
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                int orientation;
                VideoPlayActivity.this.contentView = VideoPlayActivity.this.findViewById(R.id.root_layout);
                VideoPlayActivity.this.mVideoDisplayView = (VideoDisplayView) VideoPlayActivity.this.findViewById(R.id.surface_view);
                VideoPlayActivity.this.mVideoDisplayView.setBackgroundColor(R.color.black);
                VideoPlayActivity.this.mBlackView = VideoPlayActivity.this.mActivity.findViewById(R.id.black_view);
                VideoPlayActivity.this.Volume_view = VideoPlayActivity.this.mActivity.findViewById(R.id.layout_volume);
                VideoPlayActivity.this.myProgress = (ProgressBar) VideoPlayActivity.this.findViewById(R.id.myProgress);
                VideoPlayActivity.this.mProgressView = VideoPlayActivity.this.findViewById(R.id.progress_indicator);
                VideoPlayActivity.this.isActivityRunning = true;
                VideoPlayActivity.this.mBrightMode = 1;
                Intent intent = VideoPlayActivity.this.getIntent();
                VideoPlayActivity.this.mUri = intent.getData();
                String scheme = VideoPlayActivity.this.mUri.getScheme();
                VideoPlayActivity.this.mPath = DBUtils.getLocalPath(VideoPlayActivity.this, VideoPlayActivity.this.mUri);
                VideoPlayActivity.this.LOG("run() : mPath = " + VideoPlayActivity.this.mPath);
                DBUtils.setOriBacklight(VideoPlayActivity.this.mContext, VideoPlayActivity.this.mBrightMode);
                VideoPlayActivity.this.mBrightness = DBUtils.getBacklight(VideoPlayActivity.this.mContext, VideoPlayActivity.this.mBrightMode);
                VideoPlayActivity.this.LOG(">>>>>>>mBrightness = <<<<<<<" + VideoPlayActivity.this.mBrightness);
                VideoPlayActivity.this.mVolumeMode = 1;
                DBUtils.setOriVolume(VideoPlayActivity.this.mContext, VideoPlayActivity.this.mVolumeMode);
                VideoPlayActivity.this.setVolumeControlStream(VideoPlayActivity.FULLSCREEN_FLAG);
                VideoPlayActivity.this.mFinishOnCompletion = intent.getBooleanExtra("android.intent.extra.finishOnCompletion", true);
                String netType = intent.getType();
                if (intent.hasExtra("android.intent.extra.screenOrientation") && (orientation = intent.getIntExtra("android.intent.extra.screenOrientation", -1)) != VideoPlayActivity.this.getRequestedOrientation()) {
                    VideoPlayActivity.this.setRequestedOrientation(orientation);
                }
                if (!"http".equalsIgnoreCase(scheme) && !"rtsp".equalsIgnoreCase(scheme)) {
                    VideoPlayActivity.this.LOG("+++++++++++++++wow wow restart browser");
                    VideoPlayActivity.this.viewHandler.sendEmptyMessage(VideoPlayActivity.HIDEPROGRESSVIEW);
                    ActivityManager tasksManager = (ActivityManager) VideoPlayActivity.this.getSystemService("activity");
                    List<ActivityManager.RunningServiceInfo> myServiceList = tasksManager.getRunningServices(50);
                    int i = 0;
                    while (true) {
                        if (i >= myServiceList.size()) {
                            break;
                        }
                        ActivityManager.RunningServiceInfo myRS = myServiceList.get(i);
                        if (myRS.service.equals("com.android.browser")) {
                            tasksManager.restartPackage("com.android.browser");
                            break;
                        }
                        i++;
                    }
                } else if (netType == null) {
                    VideoPlayActivity.this.mHandler.postDelayed(VideoPlayActivity.this.mPlayingChecker, 250L);
                } else if (netType.equalsIgnoreCase("video/mp4") || netType.equalsIgnoreCase("video/flv") || netType.equalsIgnoreCase("video/x-flv")) {
                    VideoPlayActivity.this.mHandler.postDelayed(VideoPlayActivity.this.mPlayingChecker, 250L);
                } else if (netType.equalsIgnoreCase("video/3gp") || netType.equalsIgnoreCase("video/3gpp") || netType.equalsIgnoreCase("video/3gpp2")) {
                    VideoPlayActivity.this.mVideoDisplayView.setDisableWhen3gp(true);
                    VideoPlayActivity.this.mHandler.postDelayed(VideoPlayActivity.this.mPlayingChecker, 250L);
                }
                VideoPlayActivity.this.checkTvout();
                VideoPlayActivity.this.checkHdmi();
                VideoPlayActivity.this.mVideoDisplayView.setOnErrorListener(VideoPlayActivity.this.mErrorListener);
                VideoPlayActivity.this.mVideoDisplayView.setOnCompletionListener(VideoPlayActivity.this.mCompletionListener);
                VideoPlayActivity.this.viewHandler.sendEmptyMessage(VideoPlayActivity.SETVIDEOCONN);
                VideoPlayActivity.this.mVideoDisplayView.requestFocus();
                VideoPlayActivity.this.mVideoDisplayView.setActivity(VideoPlayActivity.this.mActivity);
                VideoPlayActivity.this.LOG("run() : To pause 'android.rk.RockAudioPlayer.AudioPlaybackService'.");
                Intent i2 = new Intent(VideoPlayActivity.SERVICECMD);
                i2.putExtra(VideoPlayActivity.CMDNAME, VideoPlayActivity.CMDPAUSE);
                VideoPlayActivity.this.sendBroadcast(i2);
                VideoPlayActivity.this.LOG("Intent i sended");
                Intent j = new Intent("com.android.music.musicservicecommand");
                j.putExtra(VideoPlayActivity.CMDNAME, VideoPlayActivity.CMDPAUSE);
                VideoPlayActivity.this.sendBroadcast(j);
                VideoPlayActivity.this.LOG("Intent j sended");
                VideoPlayActivity.this.requestToStopFmPlay();
                VideoPlayActivity.this.mFormatBuilder = new StringBuilder();
                VideoPlayActivity.this.mFormatter = new Formatter(VideoPlayActivity.this.mFormatBuilder, Locale.getDefault());
                VideoPlayActivity.this.mVideoDisplayView.setOnTouchListener(VideoPlayActivity.this.mTouchListener);
                VideoPlayActivity.this.mVideoDisplayView.setOnKeyListener(VideoPlayActivity.this.mKeyListener);
                VideoPlayActivity.this.viewHandler.sendEmptyMessage(VideoPlayActivity.SETVIDEOURI);
            }
        };
        this.mHandler.postDelayed(this.mInitAsync, 300L);
    }

    public void checkTvout() {
        this.mTvOutMgr = null;
        this.mTVOutEnable = false;
        LOG("TVOUT:mTVOutEnable=" + this.mTVOutEnable + " mTvOutMgr=" + this.mTvOutMgr);
    }

    public void checkHdmi() {
        this.mHdmiMgr = null;
        this.mHdmiEnable = false;
        this.mOutputToHdmi = false;
        LOG("checkHdmi() : HDMI facility is NOT supported.");
    }

    private int setProgress() {
        LOG("Enter setProvress()");
        if (this.mVideoDisplayView == null || this.mDragging) {
            return 0;
        }
        int position = this.mVideoDisplayView.getCurrentPosition();
        int duration = this.mVideoDisplayView.getDuration();
        if (this.mTotalTime != null) {
            this.mTotalTime.setText(stringForTime(duration));
        }
        return position;
    }

    public String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        this.mFormatBuilder.setLength(0);
        return this.mFormatter.format("%02d:%02d:%02d", Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)).toString();
    }

    private static boolean uriSupportsBookmarks(Uri uri) {
        String scheme = uri.getScheme();
        String authority = uri.getAuthority();
        return "content".equalsIgnoreCase(scheme) && "media".equalsIgnoreCase(authority);
    }

    public Integer getBookmark(Uri uri) {
        if (!uriSupportsBookmarks(uri)) {
            return null;
        }
        Integer bookmark = DBUtils.getBookmark(this, uri);
        return bookmark;
    }

    private int getCursorInteger(Cursor cursor, int index) {
        try {
            return cursor.getInt(index);
        } catch (SQLiteException e) {
            return 0;
        } catch (NumberFormatException e2) {
            return 0;
        }
    }

    public void setBookmark(int bookmark) {
        if (uriSupportsBookmarks(this.mUri)) {
            ContentValues values = new ContentValues();
            values.put("bookmark", Integer.toString(bookmark));
            try {
                getContentResolver().update(this.mUri, values, null, null);
                LOG("setBookmark successed");
            } catch (SQLiteException e) {
            } catch (SecurityException e2) {
            } catch (UnsupportedOperationException e3) {
            }
        }
    }

    @Override // android.app.Activity
    public void onPause() {
        super.onPause();
        LOG("Enter onPause()");
        if (this.mVideoDisplayView != null) {
            this.mWasPlayingWhenPaused = this.mVideoDisplayView.isPlaying();
            this.mVideoDisplayView.pause();
        }
        if (this.mHdmiMgr != null) {
            LOG("onPause() : To disable HDMI output for activity being paused.");
            disableHdmiOutput();
        }
        if (this.mTvOutMgr != null) {
            disableTvOut();
        }
        unregisterReceiver(this.mScreencloseReceiver);
        unregisterReceiver(this.mReceiver);
        releaseWakeLock();
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        LOG("Enter VideoPlayActivity onResume()");
        this.isActivityRunning = true;
        DBUtils.setOriBacklight(this.mContext, this.mBrightMode);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MEDIA_BAD_REMOVAL");
        filter.addAction("android.intent.action.MEDIA_EJECT");
        LOG("run() : To listen to ACTION_MEDIA_BAD_REMOVAL and ACTION_MEDIA_EJECT");
        filter.addDataScheme("file");
        this.mReceiver = new BroadcastReceiver() { // from class: android.rk.RockVideoPlayer.VideoPlayActivity.7
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                VideoPlayActivity.this.LOG("mReceiver.onReceive() : action = " + action);
                if (action.equals("android.intent.action.MEDIA_EJECT") || action.equals("android.intent.action.MEDIA_BAD_REMOVAL")) {
                    String path = intent.getData().getPath();
                    Log.w(VideoPlayActivity.TAG, "mReceiver.onReceive() : path = " + path + " ,mPath = " + VideoPlayActivity.this.mPath);
                    if (VideoPlayActivity.this.mPath != null && VideoPlayActivity.this.mPath.startsWith(path)) {
                        Log.w(VideoPlayActivity.TAG, "mReceiver.onReceive() : Source volume was EJECTED or BAD_REMOVED, to finish this activity.");
                        VideoPlayActivity.this.mVideoDisplayView.Finish();
                    }
                }
            }
        };
        registerReceiver(this.mReceiver, new IntentFilter(filter));
        registerReceiver(this.mScreencloseReceiver, new IntentFilter("android.intent.action.SCREEN_OFF"));
        acquireWakeLock();
        if (this.mWasPlayingWhenPaused) {
            this.mVideoDisplayView.start();
            this.mWasPlayingWhenPaused = false;
        }
    }

    public void SaveVideoMark() {
        if (DBUtils.getBookmark(this, this.mUri) != null || DBUtils.getBookmark(this, this.mUri).intValue() == 0) {
            DBUtils.setBookmark(this, DBUtils.getCurrentCursor(this, this.mUri), this.mVideoDisplayView.getCurrentPosition());
        }
    }

    @Override // android.app.Activity
    public void onStop() {
        LOG("Enter onStop");
        super.onStop();
        initialStatus();
        if (this.mHdmiMgr != null) {
            LOG("onStop() : To disable HDMI output for activity being stopped.");
            disableHdmiOutput();
            this.mHdmiMgr = null;
        }
        DBUtils.setScreenValue(this.mContext, 0);
        if (this.mTvOutMgr != null) {
            disableTvOut();
            this.mTvOutMgr = null;
        }
        KeyguardLock();
        releaseWakeLock();
    }

    @Override // android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        LOG("Enter VideoPlayActivity OnDestroy");
        this.mHandler.removeCallbacks(this.mInitAsync);
        this.mInitAsync = null;
        this.isActivityRunning = false;
        if (this.mHdmiMgr != null) {
            unregisterReceiver(this.mHdmiBroadcastReceiver);
            LOG("onDestroy() : To disable HDMI facility for activity being destroyed.");
        }
        if (this.mTvOutMgr != null) {
            disableTvOut();
        }
        initialStatus();
    }

    public void initialStatus() {
        DBUtils.setbackBacklight(this, this.mBrightMode);
        DBUtils.setVolume(this, this.mVolumeMode);
        Settings.System.putInt(getContentResolver(), "stay_on_while_plugged_in", this.screenOn);
    }

    @Override // android.media.MediaPlayer.OnErrorListener
    public boolean onError(MediaPlayer player, int arg1, int arg2) {
        this.mHandler.removeCallbacksAndMessages(null);
        this.mProgressView.setVisibility(8);
        if (this.mHdmiMgr != null) {
            disableHdmiOutput();
        }
        if (this.mTvOutMgr == null) {
            return false;
        }
        disableTvOut();
        return false;
    }

    @Override // android.media.MediaPlayer.OnCompletionListener
    public void onCompletion(MediaPlayer mp) {
        if (this.mFinishOnCompletion) {
            finish();
        }
        if (this.mHdmiMgr != null) {
            disableHdmiOutput();
        }
        if (this.mTvOutMgr != null) {
            disableTvOut();
        }
    }

    public void DisplayVersionView() {
        View view = inflateView(R.layout.layout_version);
        Toast toast = new Toast(this);
        toast.setView(view);
        toast.setDuration(1);
        toast.show();
    }

    public void DisplayHelpView() {
        this.mHelpView = findViewById(R.id.layout_help);
        this.mHelpView.setOnClickListener(this.mCloseListener);
        TextView tv_helptitle = (TextView) this.mHelpView.findViewById(R.id.help_title);
        tv_helptitle.setText(getText(R.string.text_helptitle));
        TextView tv_helpcolse = (TextView) this.mHelpView.findViewById(R.id.help_close);
        tv_helpcolse.setText(R.string.text_helpcolse);
        tv_helpcolse.setTextSize(20.0f);
        TextView tv_bookmarkhelp = (TextView) this.mHelpView.findViewById(R.id.bookmarkhelp_text);
        tv_bookmarkhelp.setText(R.string.text_bookmarkhelp);
        TextView tv_prehelp = (TextView) this.mHelpView.findViewById(R.id.prehelp_text);
        tv_prehelp.setText(R.string.text_prehelp);
        TextView tv_pausehelp = (TextView) this.mHelpView.findViewById(R.id.pausehelp_text);
        tv_pausehelp.setText(R.string.text_pausehelp);
        TextView tv_nexthelp = (TextView) this.mHelpView.findViewById(R.id.nexthelp_text);
        tv_nexthelp.setText(R.string.text_nexthelp);
        TextView tv_brighthelp = (TextView) this.mHelpView.findViewById(R.id.brighthelp_text);
        tv_brighthelp.setText(R.string.text_brighthelp);
        TextView tv_screensizehelp = (TextView) this.mHelpView.findViewById(R.id.screensizehelp_text);
        tv_screensizehelp.setText(R.string.text_screensizehelp);
        TextView tv_returnhelp = (TextView) this.mHelpView.findViewById(R.id.returnhelp_text);
        tv_returnhelp.setText(R.string.text_returnhelp);
        TextView tv_othershelp = (TextView) this.mHelpView.findViewById(R.id.othershelp_text);
        tv_othershelp.setText(R.string.text_othershelp);
        TextView textView = (TextView) this.mHelpView.findViewById(R.id.help_more);
        tv_othershelp.setText(R.string.text_help_more);
        this.mHandler.removeMessages(1);
        this.mHelpView.setVisibility(0);
        this.mVideoDisplayView.setEnable(false);
    }

    private View inflateView(int resource) {
        LayoutInflater vi = (LayoutInflater) getSystemService("layout_inflater");
        return vi.inflate(resource, (ViewGroup) null);
    }

    public void Finish() {
        this.mVideoDisplayView.StopPlay();
        finish();
    }

    public void setUri(Uri uri) {
        this.mUri = uri;
    }

    public void dismissLoading() {
        this.mProgressView.setVisibility(8);
    }

    public void disableHdmiOutput() {
        if (this.mOutputToHdmi) {
            this.mOutputToHdmi = false;
        }
    }

    public void enableHdmiOutput(int resolution) {
        if (!this.mOutputToHdmi) {
            hideMediaControls();
            this.mOutputToHdmi = true;
        }
    }

    private void hideMediaControls() {
        this.mVideoDisplayView.hideMediaControls();
    }

    /* loaded from: classes.dex */
    public class ChoiceListAdapter extends ArrayAdapter {
        private final LayoutInflater mInflater;
        int resource;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public ChoiceListAdapter(Context context, int ResourceId, ArrayList<ChoiceItem> items) {
            super(context, ResourceId, items);
            this.resource = ResourceId;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout ChoiceView;
            ChoiceItem info = (ChoiceItem) getItem(position);
            if (convertView == null) {
                ChoiceView = new LinearLayout(getContext());
                this.mInflater.inflate(this.resource, (ViewGroup) ChoiceView, true);
            } else {
                ChoiceView = (LinearLayout) convertView;
            }
            ViewHolder vh = new ViewHolder();
            vh.choiceText = (TextView) ChoiceView.findViewById(R.id.choice_content);
            ChoiceView.setTag(vh);
            vh.choiceText.setText(info.content);
            return ChoiceView;
        }
    }

    /* loaded from: classes.dex */
    public class ChoiceItem {
        String content;

        ChoiceItem() {
        }
    }

    /* loaded from: classes.dex */
    class ViewHolder {
        TextView choiceText;

        ViewHolder() {
        }
    }

    public void enableTVOutDelay(int mode, long delayMillis) {
        Message msg = Message.obtain();
        msg.what = 1;
        msg.arg1 = mode;
        this.mOutputHandler.sendMessageDelayed(msg, delayMillis);
    }

    public void DisplayTvout() {
        this.TVOutAlert = new AlertDialog.Builder(this).setAdapter(new ChoiceListAdapter(this, R.layout.choice_item, addTVOutChoices()), new DialogInterface.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoPlayActivity.11
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int item) {
                VideoPlayActivity.this.LOG("TVOutAlert.onClick() : User clicked item '" + item + "'.");
                switch (item) {
                    case 0:
                        VideoPlayActivity.this.mTVOutMode = 2;
                        VideoPlayActivity.this.enableTVOutDelay(VideoPlayActivity.this.mTVOutMode, VideoPlayActivity.OUTPUT_OPS_DELAY_MILlIS);
                        VideoPlayActivity.this.TVOutAlert.dismiss();
                        return;
                    case 1:
                        VideoPlayActivity.this.mTVOutMode = 1;
                        VideoPlayActivity.this.enableTVOutDelay(VideoPlayActivity.this.mTVOutMode, VideoPlayActivity.OUTPUT_OPS_DELAY_MILlIS);
                        VideoPlayActivity.this.TVOutAlert.dismiss();
                        return;
                    case 2:
                        VideoPlayActivity.this.mTVOutMode = VideoPlayActivity.FULLSCREEN_FLAG;
                        VideoPlayActivity.this.enableTVOutDelay(VideoPlayActivity.this.mTVOutMode, VideoPlayActivity.OUTPUT_OPS_DELAY_MILlIS);
                        VideoPlayActivity.this.TVOutAlert.dismiss();
                        return;
                    case VideoPlayActivity.FULLSCREEN_FLAG /* 3 */:
                        VideoPlayActivity.this.mTVOutMode = VideoPlayActivity.NOT_FULLSCREEN_FLAG;
                        VideoPlayActivity.this.enableTVOutDelay(VideoPlayActivity.this.mTVOutMode, VideoPlayActivity.OUTPUT_OPS_DELAY_MILlIS);
                        VideoPlayActivity.this.TVOutAlert.dismiss();
                        return;
                    case VideoPlayActivity.NOT_FULLSCREEN_FLAG /* 4 */:
                        VideoPlayActivity.this.mTVOutMode = 5;
                        VideoPlayActivity.this.enableTVOutDelay(VideoPlayActivity.this.mTVOutMode, VideoPlayActivity.OUTPUT_OPS_DELAY_MILlIS);
                        VideoPlayActivity.this.TVOutAlert.dismiss();
                        return;
                    case 5:
                        VideoPlayActivity.this.TVOutAlert.dismiss();
                        return;
                    default:
                        VideoPlayActivity.this.TVOutAlert.dismiss();
                        return;
                }
            }
        }).setTitle(R.string.selection_tvout_title).show();
    }

    private ArrayList addTVOutChoices() {
        Resources r = getResources();
        String str1 = r.getString(R.string.selection_tvout_Cvbs_PAL);
        String str2 = r.getString(R.string.selection_tvout_Cvbs_NTSC);
        String str3 = r.getString(R.string.selection_tvout_Ypbpr480);
        String str4 = r.getString(R.string.selection_tvout_Ypbpr576);
        String str5 = r.getString(R.string.selection_tvout_Ypbpr720);
        String str6 = r.getString(R.string.alert_close);
        CharSequence[] items = {str1, str2, str3, str4, str5, str6};
        this.choiceItems = new ArrayList<ChoiceItem>();
        for (CharSequence charSequence : items) {
            ChoiceItem item = new ChoiceItem();
            item.content = (String) charSequence;
            this.choiceItems.add(item);
        }
        return this.choiceItems;
    }

    public void enableTVOut(int mode) {
        LOG("++++++++++++++==enable tvout now");
        this.mOutputToTvOut = true;
        this.mVideoDisplayView.mTVOutEnable = true;
    }

    public void DisplayHdmi() {
        this.HdmiAlert = new AlertDialog.Builder(this).setAdapter(new ChoiceListAdapter(this, R.layout.choice_item, addHdmiChoices()), new DialogInterface.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoPlayActivity.12
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        VideoPlayActivity.this.HdmiAlert.dismiss();
                        VideoPlayActivity.this.LOG("User set to output to HDMI with 720p resolution.");
                        VideoPlayActivity.this.LOG("Pause the video when hdmi start");
                        VideoPlayActivity.this.mVideoDisplayView.pause();
                        VideoPlayActivity.this.enableHdmiOutput(0);
                        VideoPlayActivity.this.mVideoDisplayView.start();
                        VideoPlayActivity.this.LOG("Start the video after hdmi start");
                        return;
                    case 1:
                    default:
                        VideoPlayActivity.this.HdmiAlert.dismiss();
                        return;
                    case 2:
                        VideoPlayActivity.this.HdmiAlert.dismiss();
                        return;
                }
            }
        }).setTitle(R.string.selection_hdmi_title).show();
    }

    private ArrayList addHdmiChoices() {
        Resources r = getResources();
        String hdmioutto = r.getString(R.string.output_to_hdmi);
        String p720 = r.getString(R.string.hdmi_720p);
        String str2 = hdmioutto + p720;
        String str3 = r.getString(R.string.alert_close);
        CharSequence[] items = {str2, str3};
        this.choiceItems = new ArrayList<ChoiceItem>();
        for (CharSequence charSequence : items) {
            ChoiceItem item = new ChoiceItem();
            item.content = (String) charSequence;
            this.choiceItems.add(item);
        }
        return this.choiceItems;
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View v, MotionEvent event) {
        if (v == this.mVideoDisplayView && this.mOutputToTvOut) {
            LOG("onTouch() : User touched device screen, to disable TvOut output.");
            disableTvOut();
            return true;
        } else if (v != this.mVideoDisplayView || !this.mOutputToHdmi) {
            return false;
        } else {
            LOG("onKey() : User press a key. to disable Hdmi output.");
            disableHdmiOutput();
            this.mVideoDisplayView.mTVOutEnable = false;
            return true;
        }
    }

    @Override // android.view.View.OnKeyListener
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        LOG("onKey() : Entered, keyCode = " + keyCode);
        if (v == this.mVideoDisplayView && this.mOutputToTvOut) {
            LOG("onKey() : User press a key. to disable TvOut output.");
            disableTvOut();
            this.mVideoDisplayView.mTVOutEnable = false;
            return true;
        } else if (v != this.mVideoDisplayView || !this.mOutputToHdmi) {
            return false;
        } else {
            LOG("onKey() : User press a key. to disable Hdmi output.");
            disableHdmiOutput();
            this.mVideoDisplayView.mTVOutEnable = false;
            return true;
        }
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == 25) {
            DBUtils.volumeAdjust(this, this.mVolumeMode, -1);
            return true;
        } else if (keyCode != 6) {
            return super.dispatchKeyEvent(event);
        } else {
            if (!this.mOutputToTvOut) {
                return false;
            }
            disableTvOut();
            this.mVideoDisplayView.mTVOutEnable = false;
            return true;
        }
    }

    public void disableTvOut() {
        this.mOutputToTvOut = false;
        this.mVideoDisplayView.mTVOutEnable = false;
    }

    public void TopBar_next(int count, int delay) {
        int delay2;
        this.mVideoDisplayView.pause();
        this.mStartSeekPos = this.mVideoDisplayView.getDuration();
        if (count < 6) {
            delay2 = delay * 25;
        } else {
            delay2 = 75000 + ((delay - 3000) * 40);
        }
        long newpos = this.mStartSeekPos + delay2;
        long duration = this.mVideoDisplayView.getDuration();
        if (newpos >= duration) {
            newpos = duration - 2000;
        }
        if (this.mVideoDisplayView.isSeekComplete()) {
            this.mVideoDisplayView.setSeekComplete(false);
            this.mVideoDisplayView.seekTo((int) newpos, true);
        }
        if (!this.mPauseStatus) {
            this.mVideoDisplayView.start();
        }
    }

    public void TopBar_prev(int count, int delay) {
        int delay2;
        this.mVideoDisplayView.pause();
        this.mStartSeekPos = this.mVideoDisplayView.getDuration();
        if (count < 6) {
            delay2 = delay * 25;
        } else {
            delay2 = 75000 + ((delay - 3000) * 40);
        }
        long newpos = this.mStartSeekPos - delay2;
        if (newpos < 0) {
            newpos = 0;
        }
        if (this.mVideoDisplayView.isSeekComplete()) {
            this.mVideoDisplayView.setSeekComplete(false);
            this.mVideoDisplayView.seekTo((int) newpos, true);
        }
        if (!this.mPauseStatus) {
            this.mVideoDisplayView.start();
        }
    }

    public void TopBar_return() {
        this.mVideoDisplayView.pause();
        disableTvOut();
        this.mVideoDisplayView.start();
    }

    public void TopBar_pause() {
        doPauseResume();
    }

    private void doPauseResume() {
        if (this.mVideoDisplayView.isPlaying()) {
            this.mVideoDisplayView.pause();
            this.mPauseStatus = true;
            return;
        }
        this.mVideoDisplayView.start();
        this.mPauseStatus = false;
    }

    public void TopBar_volume(int mode) {
        DBUtils.volumeAdjust(this.mContext, this.mVolumeMode, mode);
    }

    public int getBrightMode() {
        return this.mBrightMode;
    }

    public void requestToStopFmPlay() {
        Intent j = new Intent("com.rk.FmRadio.fmservicecommand");
        j.putExtra(CMDNAME, "stop");
        sendBroadcast(j);
    }
}
