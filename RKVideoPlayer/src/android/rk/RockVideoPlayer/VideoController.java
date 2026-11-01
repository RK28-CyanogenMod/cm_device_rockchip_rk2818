package android.rk.RockVideoPlayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.rk.RockVideoPlayer.DBUtils;
import android.rk.RockVideoPlayer.RepeatingImageButton;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.internal.policy.PolicyManager;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

/* loaded from: classes.dex */
public class VideoController extends FrameLayout implements View.OnClickListener {
    private static final boolean DEBUG = true;
    private static final int FADE_OUT = 1;
    private static final int SHOW_FAST = 3;
    private static final int SHOW_PROGRESS = 2;
    private static final String TAG = "VideoController";
    private static ArrayList<ChoiceItem> choiceItems = null;
    private static final int sDefaultTimeout = 4000;
    private int AboutCount;
    private AlertDialog AboutDialog;
    private final int HIDE_ABOUTVIEW;
    private int OldScreenBrightMode;
    private int ScreenBrightness;
    private int ScreenBrightnessMode;
    private int ScreenHeight;
    private int ScreenSizeMode;
    private int ScreenWidth;
    private final int TIME_LONG;
    private final int TIME_MIDDLE;
    private final int TIME_SHORT;
    private final int VOLUMEMINUS;
    private final int VOLUMEPLUS;
    private View b;
    private ImageView mAboutButton;
    private DialogInterface.OnClickListener mAboutDialogClickListener;
    private View.OnClickListener mAboutListener;
    private View mAnchor;
    private ImageView mBookmarkButton;
    private View.OnClickListener mBookmarkListener;
    private int mBrightMode;
    private int mBrightness;
    private String[] mCols;
    private Context mContext;
    private TextView mCurrentTime;
    private View mDecor;
    private boolean mDragging;
    private boolean mEnable;
    private TextView mEndTime;
    private ImageView mFfwdButton;
    private RepeatingImageButton.RepeatListener mFfwdListener;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private boolean mFromXml;
    private Handler mHandler;
    private boolean mHdmiEnable;
    private long mLastSeekEventTime;
    private boolean mListenersSet;
    private ImageView mNextButton;
    private ImageView mPauseButton;
    private View.OnClickListener mPauseListener;
    private boolean mPauseStatus;
    private VideoPlayerControl mPlayer;
    private long mPosOverride;
    private ImageView mPreButton;
    private ProgressBar mProgress;
    private int mRepeatMode;
    private ImageView mReturnButton;
    private View.OnClickListener mReturnListener;
    private ImageView mRewButton;
    private RepeatingImageButton.RepeatListener mRewListener;
    private View mRoot;
    public ImageView mScreenBrightButton;
    private View.OnClickListener mScreenBrightListener;
    private ImageView mScreenModeButton;
    private View.OnClickListener mScreenModeListener;
    private SeekBar.OnSeekBarChangeListener mSeekListener;
    public long mSeekTime;
    private boolean mSeeking;
    private boolean mShowing;
    private long mStartSeekPos;
    private boolean mTVOutEnable;
    private View.OnTouchListener mTouchListener;
    private boolean mUseFastForward;
    private ImageView mVolumeMinus;
    private RepeatingImageButton.RepeatListener mVolumeMinusListener;
    private ImageView mVolumePlus;
    private RepeatingImageButton.RepeatListener mVolumePlusListener;
    private Window mWindow;
    private WindowManager mWindowManager;
    private final int sRepeatAll;
    private final int sRepeatOne;
    private final int sSingle;
    Handler volumeHandler;

    /* loaded from: classes.dex */
    public interface VideoPlayerControl {
        boolean Capture();

        void DisplayHdmi();

        void DisplayHelpView();

        void DisplayRepeat();

        void DisplayTvout();

        void DisplayVersionView();

        void Finish();

        void PlayFormBeginning();

        void SetScreenSizeClickMode(boolean z);

        void deleteBookmark();

        int getBrightMode();

        int getBufferPercentage();

        int getCurrentPosition();

        Uri getCurrentUri();

        int getDefaultHeight();

        int getDefaultWidth();

        int getDuration();

        int getRepeatMode();

        int getVolumeMode();

        void hideController();

        boolean isPlaying();

        boolean isSeekComplete();

        boolean isSeeking();

        void next();

        void pause();

        void prev();

        void seekTo(int i, boolean z);

        void setBookmark(int i);

        void setEnable(boolean z);

        int setScreenBright(int i);

        int setScreenMode(int i);

        void setScreenSize(int i, int i2);

        void setSeekComplete(boolean z);

        void start();
    }

    static /* synthetic */ int access$1508(VideoController x0) {
        int i = x0.ScreenBrightnessMode;
        x0.ScreenBrightnessMode = i + 1;
        return i;
    }

    static /* synthetic */ int access$1808(VideoController x0) {
        int i = x0.ScreenSizeMode;
        x0.ScreenSizeMode = i + 1;
        return i;
    }

    public void LOG(String msg) {
        Log.d(TAG, msg);
    }

    public VideoController(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mDragging = false;
        this.VOLUMEPLUS = 1;
        this.VOLUMEMINUS = -1;
        this.mStartSeekPos = 0L;
        this.mSeeking = false;
        this.mPosOverride = -1L;
        this.OldScreenBrightMode = 1;
        this.mEnable = true;
        this.mPauseStatus = false;
        this.sSingle = 2;
        this.sRepeatOne = 1;
        this.sRepeatAll = 0;
        this.mCols = new String[]{"_display_name", "duration", "mime_type", "_size", "_id", "bookmark"};
        this.HIDE_ABOUTVIEW = 4;
        this.TIME_SHORT = 5000;
        this.TIME_MIDDLE = 8000;
        this.TIME_LONG = 10000;
        this.AboutCount = 5;
        this.mTouchListener = new View.OnTouchListener() { // from class: android.rk.RockVideoPlayer.VideoController.1
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != 0 || !VideoController.this.mShowing) {
                    return false;
                }
                VideoController.this.hide();
                return false;
            }
        };
        this.mHandler = new Handler() { // from class: android.rk.RockVideoPlayer.VideoController.2
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        VideoController.this.hide();
                        return;
                    case 2:
                        int pos = VideoController.this.setProgress();
                        if (VideoController.this.mShowing && VideoController.this.mPlayer.isPlaying()) {
                            Message msg2 = obtainMessage(2);
                            sendMessageDelayed(msg2, 1000 - (pos % 1000));
                            return;
                        }
                        return;
                    case VideoController.SHOW_FAST /* 3 */:
                        VideoController.this.setProgress(VideoController.this.mSeekTime);
                        if (VideoController.this.mShowing && VideoController.this.mPlayer.isPlaying()) {
                            Message msg3 = obtainMessage(VideoController.SHOW_FAST);
                            sendMessageDelayed(msg3, 200L);
                            return;
                        }
                        return;
                    case 4:
                        VideoController.this.AboutDialog.dismiss();
                        if (!VideoController.this.mPlayer.isPlaying()) {
                            VideoController.this.mPlayer.start();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mPauseListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.doPauseResume();
                VideoController.this.show(VideoController.sDefaultTimeout);
            }
        };
        this.mSeekListener = new SeekBar.OnSeekBarChangeListener() { // from class: android.rk.RockVideoPlayer.VideoController.4
            long duration;

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar bar) {
                VideoController.this.LOG("onStartTrackingTouch() : Entered.");
                VideoController.this.setProgress();
                VideoController.this.show(VideoController.sDefaultTimeout);
                this.duration = VideoController.this.mPlayer.getDuration();
                VideoController.this.mPlayer.pause();
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar bar, int progress, boolean fromtouch) {
                VideoController.this.LOG("onProgressChanged() : Entered.");
                if (fromtouch) {
                    VideoController.this.mDragging = true;
                    this.duration = VideoController.this.mPlayer.getDuration();
                    long newposition = (this.duration * progress) / 1000;
                    VideoController.this.mPlayer.seekTo((int) newposition, false);
                    if (VideoController.this.mCurrentTime != null) {
                        VideoController.this.mCurrentTime.setText(VideoController.this.stringForTime((int) newposition));
                    }
                    if (VideoController.this.mEndTime != null) {
                        VideoController.this.mEndTime.setText(VideoController.this.stringForTime((int) this.duration));
                    }
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar bar) {
                VideoController.this.LOG("onStopTrackingTouch() : Entered.");
                VideoController.this.mDragging = false;
                int progress = VideoController.this.mProgress.getProgress();
                this.duration = VideoController.this.mPlayer.getDuration();
                long newposition = (this.duration * progress) / 1000;
                if (newposition >= this.duration) {
                    newposition = this.duration - 2000;
                }
                VideoController.this.mPlayer.seekTo((int) newposition, true);
                VideoController.this.mSeekTime = VideoController.this.mPlayer.getCurrentPosition();
                if (VideoController.this.mCurrentTime != null) {
                    VideoController.this.mCurrentTime.setText(VideoController.this.stringForTime((int) newposition));
                }
                if (VideoController.this.mEndTime != null) {
                    VideoController.this.mEndTime.setText(VideoController.this.stringForTime((int) this.duration));
                }
                VideoController.this.setProgress();
                if (!VideoController.this.mPauseStatus) {
                    VideoController.this.mPlayer.start();
                }
                VideoController.this.updatePausePlay();
                VideoController.this.show(VideoController.sDefaultTimeout);
            }
        };
        this.mRewListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.5
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                VideoController.this.scanBackward(repcnt, howlong);
                VideoController.this.showfast(VideoController.sDefaultTimeout);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
                if (!VideoController.this.mPauseStatus) {
                    VideoController.this.mPlayer.start();
                }
                VideoController.this.show(VideoController.sDefaultTimeout);
                VideoController.this.mPlayer.setEnable(true);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
                VideoController.this.mSeekTime = VideoController.this.mPlayer.getCurrentPosition();
            }
        };
        this.mFfwdListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.6
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                VideoController.this.scanForward(repcnt, howlong);
                VideoController.this.showfast(VideoController.sDefaultTimeout);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
                if (!VideoController.this.mPauseStatus) {
                    VideoController.this.mPlayer.start();
                }
                VideoController.this.mPlayer.setEnable(true);
                VideoController.this.show(VideoController.sDefaultTimeout);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
                VideoController.this.mSeekTime = VideoController.this.mPlayer.getCurrentPosition();
            }
        };
        this.mBookmarkListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.7
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.LOG("Button setBookmaik pressed");
                VideoController.this.mPlayer.setBookmark(VideoController.this.mPlayer.getCurrentPosition());
                VideoController.this.show(VideoController.sDefaultTimeout);
            }
        };
        this.mReturnListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.8
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.mPlayer.Finish();
            }
        };
        this.mVolumeMinusListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.9
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                Message msg = VideoController.this.volumeHandler.obtainMessage(1);
                VideoController.this.volumeHandler.sendMessage(msg);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
            }
        };
        this.mVolumePlusListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.10
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                Message msg = VideoController.this.volumeHandler.obtainMessage(-1);
                VideoController.this.volumeHandler.sendMessage(msg);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
            }
        };
        this.volumeHandler = new Handler() { // from class: android.rk.RockVideoPlayer.VideoController.11
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DBUtils.Def.VOLUMEMINUS /* -1 */:
                        VideoController.this.LOG("VolumePlus Clicked!");
                        DBUtils.volumeAdjust(VideoController.this.mContext, VideoController.this.mPlayer.getVolumeMode(), 1);
                        VideoController.this.show(VideoController.sDefaultTimeout);
                        return;
                    case 0:
                    default:
                        return;
                    case 1:
                        VideoController.this.LOG("VolumeMinus Clicked!");
                        DBUtils.volumeAdjust(VideoController.this.mContext, VideoController.this.mPlayer.getVolumeMode(), -1);
                        VideoController.this.show(VideoController.sDefaultTimeout);
                        return;
                }
            }
        };
        this.mScreenBrightListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.12
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.show(VideoController.sDefaultTimeout);
                if (VideoController.this.ScreenBrightnessMode == 4) {
                    VideoController.this.ScreenBrightnessMode = 0;
                } else {
                    VideoController.access$1508(VideoController.this);
                    VideoController.this.OldScreenBrightMode = VideoController.this.ScreenBrightnessMode;
                }
                VideoController.this.setScreenBright(VideoController.this.ScreenBrightnessMode);
            }
        };
        this.mScreenModeListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.13
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.show(VideoController.sDefaultTimeout);
                VideoController.this.LOG("Enter mScreenModeListener and ScreenSizeMode = " + VideoController.this.ScreenSizeMode);
                VideoController.this.mPlayer.SetScreenSizeClickMode(true);
                if (VideoController.this.ScreenSizeMode == VideoController.SHOW_FAST) {
                    VideoController.this.ScreenSizeMode = 0;
                } else {
                    VideoController.access$1808(VideoController.this);
                }
                VideoController.this.setScreen(VideoController.this.ScreenSizeMode);
            }
        };
        this.mAboutListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.14
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.LOG(">>>>>>>>>>>>>>>>start show aboutview");
                long starttime = System.currentTimeMillis();
                VideoController.this.hide();
                VideoController.this.mHandler.removeMessages(4);
                VideoController.this.showAboutDialog();
                long endtime = System.currentTimeMillis();
                VideoController.this.LOG(">>>>>>>>>>>>>>>>show aboutview:" + (endtime - starttime) + " ms");
            }
        };
        this.mAboutDialogClickListener = new DialogInterface.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.15
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int item) {
                switch (VideoController.this.AboutCount) {
                    case 5:
                        switch (item) {
                            case 0:
                                VideoController.this.mPlayer.PlayFormBeginning();
                                return;
                            case 1:
                                VideoController.this.mPlayer.deleteBookmark();
                                return;
                            case 2:
                                VideoController.this.mPlayer.DisplayRepeat();
                                return;
                            case VideoController.SHOW_FAST /* 3 */:
                                VideoController.this.mPlayer.DisplayHelpView();
                                return;
                            case 4:
                                VideoController.this.AboutDialog.dismiss();
                                return;
                            default:
                                return;
                        }
                    case 6:
                        switch (item) {
                            case 0:
                                VideoController.this.mPlayer.PlayFormBeginning();
                                return;
                            case 1:
                                VideoController.this.mPlayer.deleteBookmark();
                                return;
                            case 2:
                                VideoController.this.mPlayer.DisplayRepeat();
                                return;
                            case VideoController.SHOW_FAST /* 3 */:
                                if (VideoController.this.mTVOutEnable) {
                                    VideoController.this.LOG("mAboutDialogClickListener.onClick() : User select 'TV-Out' item.");
                                    VideoController.this.mPlayer.DisplayTvout();
                                    return;
                                } else if (VideoController.this.mHdmiEnable) {
                                    VideoController.this.LOG("mAboutDialogClickListener.onClick() : User select 'HDMI' item.");
                                    VideoController.this.mPlayer.DisplayHdmi();
                                    return;
                                } else {
                                    return;
                                }
                            case 4:
                                VideoController.this.mPlayer.DisplayHelpView();
                                return;
                            case 5:
                                VideoController.this.AboutDialog.dismiss();
                                return;
                            default:
                                return;
                        }
                    case 7:
                        switch (item) {
                            case 0:
                                VideoController.this.mPlayer.PlayFormBeginning();
                                return;
                            case 1:
                                VideoController.this.mPlayer.deleteBookmark();
                                return;
                            case 2:
                                VideoController.this.mPlayer.DisplayRepeat();
                                return;
                            case VideoController.SHOW_FAST /* 3 */:
                                if (VideoController.this.mTVOutEnable) {
                                    VideoController.this.mPlayer.DisplayTvout();
                                    return;
                                }
                                return;
                            case 4:
                                if (VideoController.this.mHdmiEnable) {
                                    VideoController.this.mPlayer.DisplayHdmi();
                                    return;
                                }
                                return;
                            case 5:
                                VideoController.this.mPlayer.DisplayHelpView();
                                return;
                            case 6:
                                VideoController.this.AboutDialog.dismiss();
                                return;
                            default:
                                return;
                        }
                    default:
                        return;
                }
            }
        };
        this.mRoot = this;
        this.mContext = context;
        this.mUseFastForward = true;
        this.mFromXml = true;
    }

    @Override // android.view.View
    public void onFinishInflate() {
        if (this.mRoot != null) {
            initControllerView(this.mRoot);
        }
    }

    public VideoController(Context context, boolean useFastForward) {
        super(context);
        this.mDragging = false;
        this.VOLUMEPLUS = 1;
        this.VOLUMEMINUS = -1;
        this.mStartSeekPos = 0L;
        this.mSeeking = false;
        this.mPosOverride = -1L;
        this.OldScreenBrightMode = 1;
        this.mEnable = true;
        this.mPauseStatus = false;
        this.sSingle = 2;
        this.sRepeatOne = 1;
        this.sRepeatAll = 0;
        this.mCols = new String[]{"_display_name", "duration", "mime_type", "_size", "_id", "bookmark"};
        this.HIDE_ABOUTVIEW = 4;
        this.TIME_SHORT = 5000;
        this.TIME_MIDDLE = 8000;
        this.TIME_LONG = 10000;
        this.AboutCount = 5;
        this.mTouchListener = new View.OnTouchListener() { // from class: android.rk.RockVideoPlayer.VideoController.1
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != 0 || !VideoController.this.mShowing) {
                    return false;
                }
                VideoController.this.hide();
                return false;
            }
        };
        this.mHandler = new Handler() { // from class: android.rk.RockVideoPlayer.VideoController.2
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        VideoController.this.hide();
                        return;
                    case 2:
                        int pos = VideoController.this.setProgress();
                        if (VideoController.this.mShowing && VideoController.this.mPlayer.isPlaying()) {
                            Message msg2 = obtainMessage(2);
                            sendMessageDelayed(msg2, 1000 - (pos % 1000));
                            return;
                        }
                        return;
                    case VideoController.SHOW_FAST /* 3 */:
                        VideoController.this.setProgress(VideoController.this.mSeekTime);
                        if (VideoController.this.mShowing && VideoController.this.mPlayer.isPlaying()) {
                            Message msg3 = obtainMessage(VideoController.SHOW_FAST);
                            sendMessageDelayed(msg3, 200L);
                            return;
                        }
                        return;
                    case 4:
                        VideoController.this.AboutDialog.dismiss();
                        if (!VideoController.this.mPlayer.isPlaying()) {
                            VideoController.this.mPlayer.start();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mPauseListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.doPauseResume();
                VideoController.this.show(VideoController.sDefaultTimeout);
            }
        };
        this.mSeekListener = new SeekBar.OnSeekBarChangeListener() { // from class: android.rk.RockVideoPlayer.VideoController.4
            long duration;

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar bar) {
                VideoController.this.LOG("onStartTrackingTouch() : Entered.");
                VideoController.this.setProgress();
                VideoController.this.show(VideoController.sDefaultTimeout);
                this.duration = VideoController.this.mPlayer.getDuration();
                VideoController.this.mPlayer.pause();
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar bar, int progress, boolean fromtouch) {
                VideoController.this.LOG("onProgressChanged() : Entered.");
                if (fromtouch) {
                    VideoController.this.mDragging = true;
                    this.duration = VideoController.this.mPlayer.getDuration();
                    long newposition = (this.duration * progress) / 1000;
                    VideoController.this.mPlayer.seekTo((int) newposition, false);
                    if (VideoController.this.mCurrentTime != null) {
                        VideoController.this.mCurrentTime.setText(VideoController.this.stringForTime((int) newposition));
                    }
                    if (VideoController.this.mEndTime != null) {
                        VideoController.this.mEndTime.setText(VideoController.this.stringForTime((int) this.duration));
                    }
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar bar) {
                VideoController.this.LOG("onStopTrackingTouch() : Entered.");
                VideoController.this.mDragging = false;
                int progress = VideoController.this.mProgress.getProgress();
                this.duration = VideoController.this.mPlayer.getDuration();
                long newposition = (this.duration * progress) / 1000;
                if (newposition >= this.duration) {
                    newposition = this.duration - 2000;
                }
                VideoController.this.mPlayer.seekTo((int) newposition, true);
                VideoController.this.mSeekTime = VideoController.this.mPlayer.getCurrentPosition();
                if (VideoController.this.mCurrentTime != null) {
                    VideoController.this.mCurrentTime.setText(VideoController.this.stringForTime((int) newposition));
                }
                if (VideoController.this.mEndTime != null) {
                    VideoController.this.mEndTime.setText(VideoController.this.stringForTime((int) this.duration));
                }
                VideoController.this.setProgress();
                if (!VideoController.this.mPauseStatus) {
                    VideoController.this.mPlayer.start();
                }
                VideoController.this.updatePausePlay();
                VideoController.this.show(VideoController.sDefaultTimeout);
            }
        };
        this.mRewListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.5
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                VideoController.this.scanBackward(repcnt, howlong);
                VideoController.this.showfast(VideoController.sDefaultTimeout);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
                if (!VideoController.this.mPauseStatus) {
                    VideoController.this.mPlayer.start();
                }
                VideoController.this.show(VideoController.sDefaultTimeout);
                VideoController.this.mPlayer.setEnable(true);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
                VideoController.this.mSeekTime = VideoController.this.mPlayer.getCurrentPosition();
            }
        };
        this.mFfwdListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.6
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                VideoController.this.scanForward(repcnt, howlong);
                VideoController.this.showfast(VideoController.sDefaultTimeout);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
                if (!VideoController.this.mPauseStatus) {
                    VideoController.this.mPlayer.start();
                }
                VideoController.this.mPlayer.setEnable(true);
                VideoController.this.show(VideoController.sDefaultTimeout);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
                VideoController.this.mSeekTime = VideoController.this.mPlayer.getCurrentPosition();
            }
        };
        this.mBookmarkListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.7
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.LOG("Button setBookmaik pressed");
                VideoController.this.mPlayer.setBookmark(VideoController.this.mPlayer.getCurrentPosition());
                VideoController.this.show(VideoController.sDefaultTimeout);
            }
        };
        this.mReturnListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.8
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.mPlayer.Finish();
            }
        };
        this.mVolumeMinusListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.9
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                Message msg = VideoController.this.volumeHandler.obtainMessage(1);
                VideoController.this.volumeHandler.sendMessage(msg);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
            }
        };
        this.mVolumePlusListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.10
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                Message msg = VideoController.this.volumeHandler.obtainMessage(-1);
                VideoController.this.volumeHandler.sendMessage(msg);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
            }
        };
        this.volumeHandler = new Handler() { // from class: android.rk.RockVideoPlayer.VideoController.11
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DBUtils.Def.VOLUMEMINUS /* -1 */:
                        VideoController.this.LOG("VolumePlus Clicked!");
                        DBUtils.volumeAdjust(VideoController.this.mContext, VideoController.this.mPlayer.getVolumeMode(), 1);
                        VideoController.this.show(VideoController.sDefaultTimeout);
                        return;
                    case 0:
                    default:
                        return;
                    case 1:
                        VideoController.this.LOG("VolumeMinus Clicked!");
                        DBUtils.volumeAdjust(VideoController.this.mContext, VideoController.this.mPlayer.getVolumeMode(), -1);
                        VideoController.this.show(VideoController.sDefaultTimeout);
                        return;
                }
            }
        };
        this.mScreenBrightListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.12
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.show(VideoController.sDefaultTimeout);
                if (VideoController.this.ScreenBrightnessMode == 4) {
                    VideoController.this.ScreenBrightnessMode = 0;
                } else {
                    VideoController.access$1508(VideoController.this);
                    VideoController.this.OldScreenBrightMode = VideoController.this.ScreenBrightnessMode;
                }
                VideoController.this.setScreenBright(VideoController.this.ScreenBrightnessMode);
            }
        };
        this.mScreenModeListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.13
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.show(VideoController.sDefaultTimeout);
                VideoController.this.LOG("Enter mScreenModeListener and ScreenSizeMode = " + VideoController.this.ScreenSizeMode);
                VideoController.this.mPlayer.SetScreenSizeClickMode(true);
                if (VideoController.this.ScreenSizeMode == VideoController.SHOW_FAST) {
                    VideoController.this.ScreenSizeMode = 0;
                } else {
                    VideoController.access$1808(VideoController.this);
                }
                VideoController.this.setScreen(VideoController.this.ScreenSizeMode);
            }
        };
        this.mAboutListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.14
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.LOG(">>>>>>>>>>>>>>>>start show aboutview");
                long starttime = System.currentTimeMillis();
                VideoController.this.hide();
                VideoController.this.mHandler.removeMessages(4);
                VideoController.this.showAboutDialog();
                long endtime = System.currentTimeMillis();
                VideoController.this.LOG(">>>>>>>>>>>>>>>>show aboutview:" + (endtime - starttime) + " ms");
            }
        };
        this.mAboutDialogClickListener = new DialogInterface.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.15
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int item) {
                switch (VideoController.this.AboutCount) {
                    case 5:
                        switch (item) {
                            case 0:
                                VideoController.this.mPlayer.PlayFormBeginning();
                                return;
                            case 1:
                                VideoController.this.mPlayer.deleteBookmark();
                                return;
                            case 2:
                                VideoController.this.mPlayer.DisplayRepeat();
                                return;
                            case VideoController.SHOW_FAST /* 3 */:
                                VideoController.this.mPlayer.DisplayHelpView();
                                return;
                            case 4:
                                VideoController.this.AboutDialog.dismiss();
                                return;
                            default:
                                return;
                        }
                    case 6:
                        switch (item) {
                            case 0:
                                VideoController.this.mPlayer.PlayFormBeginning();
                                return;
                            case 1:
                                VideoController.this.mPlayer.deleteBookmark();
                                return;
                            case 2:
                                VideoController.this.mPlayer.DisplayRepeat();
                                return;
                            case VideoController.SHOW_FAST /* 3 */:
                                if (VideoController.this.mTVOutEnable) {
                                    VideoController.this.LOG("mAboutDialogClickListener.onClick() : User select 'TV-Out' item.");
                                    VideoController.this.mPlayer.DisplayTvout();
                                    return;
                                } else if (VideoController.this.mHdmiEnable) {
                                    VideoController.this.LOG("mAboutDialogClickListener.onClick() : User select 'HDMI' item.");
                                    VideoController.this.mPlayer.DisplayHdmi();
                                    return;
                                } else {
                                    return;
                                }
                            case 4:
                                VideoController.this.mPlayer.DisplayHelpView();
                                return;
                            case 5:
                                VideoController.this.AboutDialog.dismiss();
                                return;
                            default:
                                return;
                        }
                    case 7:
                        switch (item) {
                            case 0:
                                VideoController.this.mPlayer.PlayFormBeginning();
                                return;
                            case 1:
                                VideoController.this.mPlayer.deleteBookmark();
                                return;
                            case 2:
                                VideoController.this.mPlayer.DisplayRepeat();
                                return;
                            case VideoController.SHOW_FAST /* 3 */:
                                if (VideoController.this.mTVOutEnable) {
                                    VideoController.this.mPlayer.DisplayTvout();
                                    return;
                                }
                                return;
                            case 4:
                                if (VideoController.this.mHdmiEnable) {
                                    VideoController.this.mPlayer.DisplayHdmi();
                                    return;
                                }
                                return;
                            case 5:
                                VideoController.this.mPlayer.DisplayHelpView();
                                return;
                            case 6:
                                VideoController.this.AboutDialog.dismiss();
                                return;
                            default:
                                return;
                        }
                    default:
                        return;
                }
            }
        };
        this.mContext = context;
        this.mUseFastForward = useFastForward;
        initFloatingWindow();
        addChoices();
    }

    public VideoController(Context context) {
        super(context);
        this.mDragging = false;
        this.VOLUMEPLUS = 1;
        this.VOLUMEMINUS = -1;
        this.mStartSeekPos = 0L;
        this.mSeeking = false;
        this.mPosOverride = -1L;
        this.OldScreenBrightMode = 1;
        this.mEnable = true;
        this.mPauseStatus = false;
        this.sSingle = 2;
        this.sRepeatOne = 1;
        this.sRepeatAll = 0;
        this.mCols = new String[]{"_display_name", "duration", "mime_type", "_size", "_id", "bookmark"};
        this.HIDE_ABOUTVIEW = 4;
        this.TIME_SHORT = 5000;
        this.TIME_MIDDLE = 8000;
        this.TIME_LONG = 10000;
        this.AboutCount = 5;
        this.mTouchListener = new View.OnTouchListener() { // from class: android.rk.RockVideoPlayer.VideoController.1
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != 0 || !VideoController.this.mShowing) {
                    return false;
                }
                VideoController.this.hide();
                return false;
            }
        };
        this.mHandler = new Handler() { // from class: android.rk.RockVideoPlayer.VideoController.2
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        VideoController.this.hide();
                        return;
                    case 2:
                        int pos = VideoController.this.setProgress();
                        if (VideoController.this.mShowing && VideoController.this.mPlayer.isPlaying()) {
                            Message msg2 = obtainMessage(2);
                            sendMessageDelayed(msg2, 1000 - (pos % 1000));
                            return;
                        }
                        return;
                    case VideoController.SHOW_FAST /* 3 */:
                        VideoController.this.setProgress(VideoController.this.mSeekTime);
                        if (VideoController.this.mShowing && VideoController.this.mPlayer.isPlaying()) {
                            Message msg3 = obtainMessage(VideoController.SHOW_FAST);
                            sendMessageDelayed(msg3, 200L);
                            return;
                        }
                        return;
                    case 4:
                        VideoController.this.AboutDialog.dismiss();
                        if (!VideoController.this.mPlayer.isPlaying()) {
                            VideoController.this.mPlayer.start();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mPauseListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.doPauseResume();
                VideoController.this.show(VideoController.sDefaultTimeout);
            }
        };
        this.mSeekListener = new SeekBar.OnSeekBarChangeListener() { // from class: android.rk.RockVideoPlayer.VideoController.4
            long duration;

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar bar) {
                VideoController.this.LOG("onStartTrackingTouch() : Entered.");
                VideoController.this.setProgress();
                VideoController.this.show(VideoController.sDefaultTimeout);
                this.duration = VideoController.this.mPlayer.getDuration();
                VideoController.this.mPlayer.pause();
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar bar, int progress, boolean fromtouch) {
                VideoController.this.LOG("onProgressChanged() : Entered.");
                if (fromtouch) {
                    VideoController.this.mDragging = true;
                    this.duration = VideoController.this.mPlayer.getDuration();
                    long newposition = (this.duration * progress) / 1000;
                    VideoController.this.mPlayer.seekTo((int) newposition, false);
                    if (VideoController.this.mCurrentTime != null) {
                        VideoController.this.mCurrentTime.setText(VideoController.this.stringForTime((int) newposition));
                    }
                    if (VideoController.this.mEndTime != null) {
                        VideoController.this.mEndTime.setText(VideoController.this.stringForTime((int) this.duration));
                    }
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar bar) {
                VideoController.this.LOG("onStopTrackingTouch() : Entered.");
                VideoController.this.mDragging = false;
                int progress = VideoController.this.mProgress.getProgress();
                this.duration = VideoController.this.mPlayer.getDuration();
                long newposition = (this.duration * progress) / 1000;
                if (newposition >= this.duration) {
                    newposition = this.duration - 2000;
                }
                VideoController.this.mPlayer.seekTo((int) newposition, true);
                VideoController.this.mSeekTime = VideoController.this.mPlayer.getCurrentPosition();
                if (VideoController.this.mCurrentTime != null) {
                    VideoController.this.mCurrentTime.setText(VideoController.this.stringForTime((int) newposition));
                }
                if (VideoController.this.mEndTime != null) {
                    VideoController.this.mEndTime.setText(VideoController.this.stringForTime((int) this.duration));
                }
                VideoController.this.setProgress();
                if (!VideoController.this.mPauseStatus) {
                    VideoController.this.mPlayer.start();
                }
                VideoController.this.updatePausePlay();
                VideoController.this.show(VideoController.sDefaultTimeout);
            }
        };
        this.mRewListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.5
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                VideoController.this.scanBackward(repcnt, howlong);
                VideoController.this.showfast(VideoController.sDefaultTimeout);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
                if (!VideoController.this.mPauseStatus) {
                    VideoController.this.mPlayer.start();
                }
                VideoController.this.show(VideoController.sDefaultTimeout);
                VideoController.this.mPlayer.setEnable(true);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
                VideoController.this.mSeekTime = VideoController.this.mPlayer.getCurrentPosition();
            }
        };
        this.mFfwdListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.6
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                VideoController.this.scanForward(repcnt, howlong);
                VideoController.this.showfast(VideoController.sDefaultTimeout);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
                if (!VideoController.this.mPauseStatus) {
                    VideoController.this.mPlayer.start();
                }
                VideoController.this.mPlayer.setEnable(true);
                VideoController.this.show(VideoController.sDefaultTimeout);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
                VideoController.this.mSeekTime = VideoController.this.mPlayer.getCurrentPosition();
            }
        };
        this.mBookmarkListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.7
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.LOG("Button setBookmaik pressed");
                VideoController.this.mPlayer.setBookmark(VideoController.this.mPlayer.getCurrentPosition());
                VideoController.this.show(VideoController.sDefaultTimeout);
            }
        };
        this.mReturnListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.8
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.mPlayer.Finish();
            }
        };
        this.mVolumeMinusListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.9
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                Message msg = VideoController.this.volumeHandler.obtainMessage(1);
                VideoController.this.volumeHandler.sendMessage(msg);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
            }
        };
        this.mVolumePlusListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.10
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                Message msg = VideoController.this.volumeHandler.obtainMessage(-1);
                VideoController.this.volumeHandler.sendMessage(msg);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
            }
        };
        this.volumeHandler = new Handler() { // from class: android.rk.RockVideoPlayer.VideoController.11
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DBUtils.Def.VOLUMEMINUS /* -1 */:
                        VideoController.this.LOG("VolumePlus Clicked!");
                        DBUtils.volumeAdjust(VideoController.this.mContext, VideoController.this.mPlayer.getVolumeMode(), 1);
                        VideoController.this.show(VideoController.sDefaultTimeout);
                        return;
                    case 0:
                    default:
                        return;
                    case 1:
                        VideoController.this.LOG("VolumeMinus Clicked!");
                        DBUtils.volumeAdjust(VideoController.this.mContext, VideoController.this.mPlayer.getVolumeMode(), -1);
                        VideoController.this.show(VideoController.sDefaultTimeout);
                        return;
                }
            }
        };
        this.mScreenBrightListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.12
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.show(VideoController.sDefaultTimeout);
                if (VideoController.this.ScreenBrightnessMode == 4) {
                    VideoController.this.ScreenBrightnessMode = 0;
                } else {
                    VideoController.access$1508(VideoController.this);
                    VideoController.this.OldScreenBrightMode = VideoController.this.ScreenBrightnessMode;
                }
                VideoController.this.setScreenBright(VideoController.this.ScreenBrightnessMode);
            }
        };
        this.mScreenModeListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.13
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.show(VideoController.sDefaultTimeout);
                VideoController.this.LOG("Enter mScreenModeListener and ScreenSizeMode = " + VideoController.this.ScreenSizeMode);
                VideoController.this.mPlayer.SetScreenSizeClickMode(true);
                if (VideoController.this.ScreenSizeMode == VideoController.SHOW_FAST) {
                    VideoController.this.ScreenSizeMode = 0;
                } else {
                    VideoController.access$1808(VideoController.this);
                }
                VideoController.this.setScreen(VideoController.this.ScreenSizeMode);
            }
        };
        this.mAboutListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.14
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.LOG(">>>>>>>>>>>>>>>>start show aboutview");
                long starttime = System.currentTimeMillis();
                VideoController.this.hide();
                VideoController.this.mHandler.removeMessages(4);
                VideoController.this.showAboutDialog();
                long endtime = System.currentTimeMillis();
                VideoController.this.LOG(">>>>>>>>>>>>>>>>show aboutview:" + (endtime - starttime) + " ms");
            }
        };
        this.mAboutDialogClickListener = new DialogInterface.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.15
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int item) {
                switch (VideoController.this.AboutCount) {
                    case 5:
                        switch (item) {
                            case 0:
                                VideoController.this.mPlayer.PlayFormBeginning();
                                return;
                            case 1:
                                VideoController.this.mPlayer.deleteBookmark();
                                return;
                            case 2:
                                VideoController.this.mPlayer.DisplayRepeat();
                                return;
                            case VideoController.SHOW_FAST /* 3 */:
                                VideoController.this.mPlayer.DisplayHelpView();
                                return;
                            case 4:
                                VideoController.this.AboutDialog.dismiss();
                                return;
                            default:
                                return;
                        }
                    case 6:
                        switch (item) {
                            case 0:
                                VideoController.this.mPlayer.PlayFormBeginning();
                                return;
                            case 1:
                                VideoController.this.mPlayer.deleteBookmark();
                                return;
                            case 2:
                                VideoController.this.mPlayer.DisplayRepeat();
                                return;
                            case VideoController.SHOW_FAST /* 3 */:
                                if (VideoController.this.mTVOutEnable) {
                                    VideoController.this.LOG("mAboutDialogClickListener.onClick() : User select 'TV-Out' item.");
                                    VideoController.this.mPlayer.DisplayTvout();
                                    return;
                                } else if (VideoController.this.mHdmiEnable) {
                                    VideoController.this.LOG("mAboutDialogClickListener.onClick() : User select 'HDMI' item.");
                                    VideoController.this.mPlayer.DisplayHdmi();
                                    return;
                                } else {
                                    return;
                                }
                            case 4:
                                VideoController.this.mPlayer.DisplayHelpView();
                                return;
                            case 5:
                                VideoController.this.AboutDialog.dismiss();
                                return;
                            default:
                                return;
                        }
                    case 7:
                        switch (item) {
                            case 0:
                                VideoController.this.mPlayer.PlayFormBeginning();
                                return;
                            case 1:
                                VideoController.this.mPlayer.deleteBookmark();
                                return;
                            case 2:
                                VideoController.this.mPlayer.DisplayRepeat();
                                return;
                            case VideoController.SHOW_FAST /* 3 */:
                                if (VideoController.this.mTVOutEnable) {
                                    VideoController.this.mPlayer.DisplayTvout();
                                    return;
                                }
                                return;
                            case 4:
                                if (VideoController.this.mHdmiEnable) {
                                    VideoController.this.mPlayer.DisplayHdmi();
                                    return;
                                }
                                return;
                            case 5:
                                VideoController.this.mPlayer.DisplayHelpView();
                                return;
                            case 6:
                                VideoController.this.AboutDialog.dismiss();
                                return;
                            default:
                                return;
                        }
                    default:
                        return;
                }
            }
        };
        this.mContext = context;
        this.mUseFastForward = true;
        initFloatingWindow();
        addChoices();
    }

    public VideoController(Context context, int brightness) {
        super(context);
        this.mDragging = false;
        this.VOLUMEPLUS = 1;
        this.VOLUMEMINUS = -1;
        this.mStartSeekPos = 0L;
        this.mSeeking = false;
        this.mPosOverride = -1L;
        this.OldScreenBrightMode = 1;
        this.mEnable = true;
        this.mPauseStatus = false;
        this.sSingle = 2;
        this.sRepeatOne = 1;
        this.sRepeatAll = 0;
        this.mCols = new String[]{"_display_name", "duration", "mime_type", "_size", "_id", "bookmark"};
        this.HIDE_ABOUTVIEW = 4;
        this.TIME_SHORT = 5000;
        this.TIME_MIDDLE = 8000;
        this.TIME_LONG = 10000;
        this.AboutCount = 5;
        this.mTouchListener = new View.OnTouchListener() { // from class: android.rk.RockVideoPlayer.VideoController.1
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != 0 || !VideoController.this.mShowing) {
                    return false;
                }
                VideoController.this.hide();
                return false;
            }
        };
        this.mHandler = new Handler() { // from class: android.rk.RockVideoPlayer.VideoController.2
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        VideoController.this.hide();
                        return;
                    case 2:
                        int pos = VideoController.this.setProgress();
                        if (VideoController.this.mShowing && VideoController.this.mPlayer.isPlaying()) {
                            Message msg2 = obtainMessage(2);
                            sendMessageDelayed(msg2, 1000 - (pos % 1000));
                            return;
                        }
                        return;
                    case VideoController.SHOW_FAST /* 3 */:
                        VideoController.this.setProgress(VideoController.this.mSeekTime);
                        if (VideoController.this.mShowing && VideoController.this.mPlayer.isPlaying()) {
                            Message msg3 = obtainMessage(VideoController.SHOW_FAST);
                            sendMessageDelayed(msg3, 200L);
                            return;
                        }
                        return;
                    case 4:
                        VideoController.this.AboutDialog.dismiss();
                        if (!VideoController.this.mPlayer.isPlaying()) {
                            VideoController.this.mPlayer.start();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mPauseListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.doPauseResume();
                VideoController.this.show(VideoController.sDefaultTimeout);
            }
        };
        this.mSeekListener = new SeekBar.OnSeekBarChangeListener() { // from class: android.rk.RockVideoPlayer.VideoController.4
            long duration;

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar bar) {
                VideoController.this.LOG("onStartTrackingTouch() : Entered.");
                VideoController.this.setProgress();
                VideoController.this.show(VideoController.sDefaultTimeout);
                this.duration = VideoController.this.mPlayer.getDuration();
                VideoController.this.mPlayer.pause();
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar bar, int progress, boolean fromtouch) {
                VideoController.this.LOG("onProgressChanged() : Entered.");
                if (fromtouch) {
                    VideoController.this.mDragging = true;
                    this.duration = VideoController.this.mPlayer.getDuration();
                    long newposition = (this.duration * progress) / 1000;
                    VideoController.this.mPlayer.seekTo((int) newposition, false);
                    if (VideoController.this.mCurrentTime != null) {
                        VideoController.this.mCurrentTime.setText(VideoController.this.stringForTime((int) newposition));
                    }
                    if (VideoController.this.mEndTime != null) {
                        VideoController.this.mEndTime.setText(VideoController.this.stringForTime((int) this.duration));
                    }
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar bar) {
                VideoController.this.LOG("onStopTrackingTouch() : Entered.");
                VideoController.this.mDragging = false;
                int progress = VideoController.this.mProgress.getProgress();
                this.duration = VideoController.this.mPlayer.getDuration();
                long newposition = (this.duration * progress) / 1000;
                if (newposition >= this.duration) {
                    newposition = this.duration - 2000;
                }
                VideoController.this.mPlayer.seekTo((int) newposition, true);
                VideoController.this.mSeekTime = VideoController.this.mPlayer.getCurrentPosition();
                if (VideoController.this.mCurrentTime != null) {
                    VideoController.this.mCurrentTime.setText(VideoController.this.stringForTime((int) newposition));
                }
                if (VideoController.this.mEndTime != null) {
                    VideoController.this.mEndTime.setText(VideoController.this.stringForTime((int) this.duration));
                }
                VideoController.this.setProgress();
                if (!VideoController.this.mPauseStatus) {
                    VideoController.this.mPlayer.start();
                }
                VideoController.this.updatePausePlay();
                VideoController.this.show(VideoController.sDefaultTimeout);
            }
        };
        this.mRewListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.5
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                VideoController.this.scanBackward(repcnt, howlong);
                VideoController.this.showfast(VideoController.sDefaultTimeout);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
                if (!VideoController.this.mPauseStatus) {
                    VideoController.this.mPlayer.start();
                }
                VideoController.this.show(VideoController.sDefaultTimeout);
                VideoController.this.mPlayer.setEnable(true);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
                VideoController.this.mSeekTime = VideoController.this.mPlayer.getCurrentPosition();
            }
        };
        this.mFfwdListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.6
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                VideoController.this.scanForward(repcnt, howlong);
                VideoController.this.showfast(VideoController.sDefaultTimeout);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
                if (!VideoController.this.mPauseStatus) {
                    VideoController.this.mPlayer.start();
                }
                VideoController.this.mPlayer.setEnable(true);
                VideoController.this.show(VideoController.sDefaultTimeout);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
                VideoController.this.mSeekTime = VideoController.this.mPlayer.getCurrentPosition();
            }
        };
        this.mBookmarkListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.7
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.LOG("Button setBookmaik pressed");
                VideoController.this.mPlayer.setBookmark(VideoController.this.mPlayer.getCurrentPosition());
                VideoController.this.show(VideoController.sDefaultTimeout);
            }
        };
        this.mReturnListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.8
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.mPlayer.Finish();
            }
        };
        this.mVolumeMinusListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.9
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                Message msg = VideoController.this.volumeHandler.obtainMessage(1);
                VideoController.this.volumeHandler.sendMessage(msg);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
            }
        };
        this.mVolumePlusListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.10
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                Message msg = VideoController.this.volumeHandler.obtainMessage(-1);
                VideoController.this.volumeHandler.sendMessage(msg);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
            }
        };
        this.volumeHandler = new Handler() { // from class: android.rk.RockVideoPlayer.VideoController.11
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DBUtils.Def.VOLUMEMINUS /* -1 */:
                        VideoController.this.LOG("VolumePlus Clicked!");
                        DBUtils.volumeAdjust(VideoController.this.mContext, VideoController.this.mPlayer.getVolumeMode(), 1);
                        VideoController.this.show(VideoController.sDefaultTimeout);
                        return;
                    case 0:
                    default:
                        return;
                    case 1:
                        VideoController.this.LOG("VolumeMinus Clicked!");
                        DBUtils.volumeAdjust(VideoController.this.mContext, VideoController.this.mPlayer.getVolumeMode(), -1);
                        VideoController.this.show(VideoController.sDefaultTimeout);
                        return;
                }
            }
        };
        this.mScreenBrightListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.12
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.show(VideoController.sDefaultTimeout);
                if (VideoController.this.ScreenBrightnessMode == 4) {
                    VideoController.this.ScreenBrightnessMode = 0;
                } else {
                    VideoController.access$1508(VideoController.this);
                    VideoController.this.OldScreenBrightMode = VideoController.this.ScreenBrightnessMode;
                }
                VideoController.this.setScreenBright(VideoController.this.ScreenBrightnessMode);
            }
        };
        this.mScreenModeListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.13
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.show(VideoController.sDefaultTimeout);
                VideoController.this.LOG("Enter mScreenModeListener and ScreenSizeMode = " + VideoController.this.ScreenSizeMode);
                VideoController.this.mPlayer.SetScreenSizeClickMode(true);
                if (VideoController.this.ScreenSizeMode == VideoController.SHOW_FAST) {
                    VideoController.this.ScreenSizeMode = 0;
                } else {
                    VideoController.access$1808(VideoController.this);
                }
                VideoController.this.setScreen(VideoController.this.ScreenSizeMode);
            }
        };
        this.mAboutListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.14
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.LOG(">>>>>>>>>>>>>>>>start show aboutview");
                long starttime = System.currentTimeMillis();
                VideoController.this.hide();
                VideoController.this.mHandler.removeMessages(4);
                VideoController.this.showAboutDialog();
                long endtime = System.currentTimeMillis();
                VideoController.this.LOG(">>>>>>>>>>>>>>>>show aboutview:" + (endtime - starttime) + " ms");
            }
        };
        this.mAboutDialogClickListener = new DialogInterface.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.15
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int item) {
                switch (VideoController.this.AboutCount) {
                    case 5:
                        switch (item) {
                            case 0:
                                VideoController.this.mPlayer.PlayFormBeginning();
                                return;
                            case 1:
                                VideoController.this.mPlayer.deleteBookmark();
                                return;
                            case 2:
                                VideoController.this.mPlayer.DisplayRepeat();
                                return;
                            case VideoController.SHOW_FAST /* 3 */:
                                VideoController.this.mPlayer.DisplayHelpView();
                                return;
                            case 4:
                                VideoController.this.AboutDialog.dismiss();
                                return;
                            default:
                                return;
                        }
                    case 6:
                        switch (item) {
                            case 0:
                                VideoController.this.mPlayer.PlayFormBeginning();
                                return;
                            case 1:
                                VideoController.this.mPlayer.deleteBookmark();
                                return;
                            case 2:
                                VideoController.this.mPlayer.DisplayRepeat();
                                return;
                            case VideoController.SHOW_FAST /* 3 */:
                                if (VideoController.this.mTVOutEnable) {
                                    VideoController.this.LOG("mAboutDialogClickListener.onClick() : User select 'TV-Out' item.");
                                    VideoController.this.mPlayer.DisplayTvout();
                                    return;
                                } else if (VideoController.this.mHdmiEnable) {
                                    VideoController.this.LOG("mAboutDialogClickListener.onClick() : User select 'HDMI' item.");
                                    VideoController.this.mPlayer.DisplayHdmi();
                                    return;
                                } else {
                                    return;
                                }
                            case 4:
                                VideoController.this.mPlayer.DisplayHelpView();
                                return;
                            case 5:
                                VideoController.this.AboutDialog.dismiss();
                                return;
                            default:
                                return;
                        }
                    case 7:
                        switch (item) {
                            case 0:
                                VideoController.this.mPlayer.PlayFormBeginning();
                                return;
                            case 1:
                                VideoController.this.mPlayer.deleteBookmark();
                                return;
                            case 2:
                                VideoController.this.mPlayer.DisplayRepeat();
                                return;
                            case VideoController.SHOW_FAST /* 3 */:
                                if (VideoController.this.mTVOutEnable) {
                                    VideoController.this.mPlayer.DisplayTvout();
                                    return;
                                }
                                return;
                            case 4:
                                if (VideoController.this.mHdmiEnable) {
                                    VideoController.this.mPlayer.DisplayHdmi();
                                    return;
                                }
                                return;
                            case 5:
                                VideoController.this.mPlayer.DisplayHelpView();
                                return;
                            case 6:
                                VideoController.this.AboutDialog.dismiss();
                                return;
                            default:
                                return;
                        }
                    default:
                        return;
                }
            }
        };
        this.mContext = context;
        this.mUseFastForward = true;
        initFloatingWindow();
        this.mBrightness = brightness;
        addChoices();
    }

    public VideoController(Context context, int brightness, boolean tvout, boolean hdmi) {
        super(context);
        this.mDragging = false;
        this.VOLUMEPLUS = 1;
        this.VOLUMEMINUS = -1;
        this.mStartSeekPos = 0L;
        this.mSeeking = false;
        this.mPosOverride = -1L;
        this.OldScreenBrightMode = 1;
        this.mEnable = true;
        this.mPauseStatus = false;
        this.sSingle = 2;
        this.sRepeatOne = 1;
        this.sRepeatAll = 0;
        this.mCols = new String[]{"_display_name", "duration", "mime_type", "_size", "_id", "bookmark"};
        this.HIDE_ABOUTVIEW = 4;
        this.TIME_SHORT = 5000;
        this.TIME_MIDDLE = 8000;
        this.TIME_LONG = 10000;
        this.AboutCount = 5;
        this.mTouchListener = new View.OnTouchListener() { // from class: android.rk.RockVideoPlayer.VideoController.1
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != 0 || !VideoController.this.mShowing) {
                    return false;
                }
                VideoController.this.hide();
                return false;
            }
        };
        this.mHandler = new Handler() { // from class: android.rk.RockVideoPlayer.VideoController.2
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        VideoController.this.hide();
                        return;
                    case 2:
                        int pos = VideoController.this.setProgress();
                        if (VideoController.this.mShowing && VideoController.this.mPlayer.isPlaying()) {
                            Message msg2 = obtainMessage(2);
                            sendMessageDelayed(msg2, 1000 - (pos % 1000));
                            return;
                        }
                        return;
                    case VideoController.SHOW_FAST /* 3 */:
                        VideoController.this.setProgress(VideoController.this.mSeekTime);
                        if (VideoController.this.mShowing && VideoController.this.mPlayer.isPlaying()) {
                            Message msg3 = obtainMessage(VideoController.SHOW_FAST);
                            sendMessageDelayed(msg3, 200L);
                            return;
                        }
                        return;
                    case 4:
                        VideoController.this.AboutDialog.dismiss();
                        if (!VideoController.this.mPlayer.isPlaying()) {
                            VideoController.this.mPlayer.start();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mPauseListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.doPauseResume();
                VideoController.this.show(VideoController.sDefaultTimeout);
            }
        };
        this.mSeekListener = new SeekBar.OnSeekBarChangeListener() { // from class: android.rk.RockVideoPlayer.VideoController.4
            long duration;

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar bar) {
                VideoController.this.LOG("onStartTrackingTouch() : Entered.");
                VideoController.this.setProgress();
                VideoController.this.show(VideoController.sDefaultTimeout);
                this.duration = VideoController.this.mPlayer.getDuration();
                VideoController.this.mPlayer.pause();
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar bar, int progress, boolean fromtouch) {
                VideoController.this.LOG("onProgressChanged() : Entered.");
                if (fromtouch) {
                    VideoController.this.mDragging = true;
                    this.duration = VideoController.this.mPlayer.getDuration();
                    long newposition = (this.duration * progress) / 1000;
                    VideoController.this.mPlayer.seekTo((int) newposition, false);
                    if (VideoController.this.mCurrentTime != null) {
                        VideoController.this.mCurrentTime.setText(VideoController.this.stringForTime((int) newposition));
                    }
                    if (VideoController.this.mEndTime != null) {
                        VideoController.this.mEndTime.setText(VideoController.this.stringForTime((int) this.duration));
                    }
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar bar) {
                VideoController.this.LOG("onStopTrackingTouch() : Entered.");
                VideoController.this.mDragging = false;
                int progress = VideoController.this.mProgress.getProgress();
                this.duration = VideoController.this.mPlayer.getDuration();
                long newposition = (this.duration * progress) / 1000;
                if (newposition >= this.duration) {
                    newposition = this.duration - 2000;
                }
                VideoController.this.mPlayer.seekTo((int) newposition, true);
                VideoController.this.mSeekTime = VideoController.this.mPlayer.getCurrentPosition();
                if (VideoController.this.mCurrentTime != null) {
                    VideoController.this.mCurrentTime.setText(VideoController.this.stringForTime((int) newposition));
                }
                if (VideoController.this.mEndTime != null) {
                    VideoController.this.mEndTime.setText(VideoController.this.stringForTime((int) this.duration));
                }
                VideoController.this.setProgress();
                if (!VideoController.this.mPauseStatus) {
                    VideoController.this.mPlayer.start();
                }
                VideoController.this.updatePausePlay();
                VideoController.this.show(VideoController.sDefaultTimeout);
            }
        };
        this.mRewListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.5
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                VideoController.this.scanBackward(repcnt, howlong);
                VideoController.this.showfast(VideoController.sDefaultTimeout);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
                if (!VideoController.this.mPauseStatus) {
                    VideoController.this.mPlayer.start();
                }
                VideoController.this.show(VideoController.sDefaultTimeout);
                VideoController.this.mPlayer.setEnable(true);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
                VideoController.this.mSeekTime = VideoController.this.mPlayer.getCurrentPosition();
            }
        };
        this.mFfwdListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.6
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                VideoController.this.scanForward(repcnt, howlong);
                VideoController.this.showfast(VideoController.sDefaultTimeout);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
                if (!VideoController.this.mPauseStatus) {
                    VideoController.this.mPlayer.start();
                }
                VideoController.this.mPlayer.setEnable(true);
                VideoController.this.show(VideoController.sDefaultTimeout);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
                VideoController.this.mSeekTime = VideoController.this.mPlayer.getCurrentPosition();
            }
        };
        this.mBookmarkListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.7
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.LOG("Button setBookmaik pressed");
                VideoController.this.mPlayer.setBookmark(VideoController.this.mPlayer.getCurrentPosition());
                VideoController.this.show(VideoController.sDefaultTimeout);
            }
        };
        this.mReturnListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.8
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.mPlayer.Finish();
            }
        };
        this.mVolumeMinusListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.9
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                Message msg = VideoController.this.volumeHandler.obtainMessage(1);
                VideoController.this.volumeHandler.sendMessage(msg);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
            }
        };
        this.mVolumePlusListener = new RepeatingImageButton.RepeatListener() { // from class: android.rk.RockVideoPlayer.VideoController.10
            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void onRepeat(View v, long howlong, int repcnt) {
                Message msg = VideoController.this.volumeHandler.obtainMessage(-1);
                VideoController.this.volumeHandler.sendMessage(msg);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void isClick(boolean click) {
                VideoController.this.mPlayer.setEnable(click);
                VideoController.this.setShortClickButtonMode(click);
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doAfterLongclick() {
            }

            @Override // android.rk.RockVideoPlayer.RepeatingImageButton.RepeatListener
            public void doBeginLongclick() {
            }
        };
        this.volumeHandler = new Handler() { // from class: android.rk.RockVideoPlayer.VideoController.11
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DBUtils.Def.VOLUMEMINUS /* -1 */:
                        VideoController.this.LOG("VolumePlus Clicked!");
                        DBUtils.volumeAdjust(VideoController.this.mContext, VideoController.this.mPlayer.getVolumeMode(), 1);
                        VideoController.this.show(VideoController.sDefaultTimeout);
                        return;
                    case 0:
                    default:
                        return;
                    case 1:
                        VideoController.this.LOG("VolumeMinus Clicked!");
                        DBUtils.volumeAdjust(VideoController.this.mContext, VideoController.this.mPlayer.getVolumeMode(), -1);
                        VideoController.this.show(VideoController.sDefaultTimeout);
                        return;
                }
            }
        };
        this.mScreenBrightListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.12
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.show(VideoController.sDefaultTimeout);
                if (VideoController.this.ScreenBrightnessMode == 4) {
                    VideoController.this.ScreenBrightnessMode = 0;
                } else {
                    VideoController.access$1508(VideoController.this);
                    VideoController.this.OldScreenBrightMode = VideoController.this.ScreenBrightnessMode;
                }
                VideoController.this.setScreenBright(VideoController.this.ScreenBrightnessMode);
            }
        };
        this.mScreenModeListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.13
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.show(VideoController.sDefaultTimeout);
                VideoController.this.LOG("Enter mScreenModeListener and ScreenSizeMode = " + VideoController.this.ScreenSizeMode);
                VideoController.this.mPlayer.SetScreenSizeClickMode(true);
                if (VideoController.this.ScreenSizeMode == VideoController.SHOW_FAST) {
                    VideoController.this.ScreenSizeMode = 0;
                } else {
                    VideoController.access$1808(VideoController.this);
                }
                VideoController.this.setScreen(VideoController.this.ScreenSizeMode);
            }
        };
        this.mAboutListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.14
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoController.this.LOG(">>>>>>>>>>>>>>>>start show aboutview");
                long starttime = System.currentTimeMillis();
                VideoController.this.hide();
                VideoController.this.mHandler.removeMessages(4);
                VideoController.this.showAboutDialog();
                long endtime = System.currentTimeMillis();
                VideoController.this.LOG(">>>>>>>>>>>>>>>>show aboutview:" + (endtime - starttime) + " ms");
            }
        };
        this.mAboutDialogClickListener = new DialogInterface.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoController.15
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int item) {
                switch (VideoController.this.AboutCount) {
                    case 5:
                        switch (item) {
                            case 0:
                                VideoController.this.mPlayer.PlayFormBeginning();
                                return;
                            case 1:
                                VideoController.this.mPlayer.deleteBookmark();
                                return;
                            case 2:
                                VideoController.this.mPlayer.DisplayRepeat();
                                return;
                            case VideoController.SHOW_FAST /* 3 */:
                                VideoController.this.mPlayer.DisplayHelpView();
                                return;
                            case 4:
                                VideoController.this.AboutDialog.dismiss();
                                return;
                            default:
                                return;
                        }
                    case 6:
                        switch (item) {
                            case 0:
                                VideoController.this.mPlayer.PlayFormBeginning();
                                return;
                            case 1:
                                VideoController.this.mPlayer.deleteBookmark();
                                return;
                            case 2:
                                VideoController.this.mPlayer.DisplayRepeat();
                                return;
                            case VideoController.SHOW_FAST /* 3 */:
                                if (VideoController.this.mTVOutEnable) {
                                    VideoController.this.LOG("mAboutDialogClickListener.onClick() : User select 'TV-Out' item.");
                                    VideoController.this.mPlayer.DisplayTvout();
                                    return;
                                } else if (VideoController.this.mHdmiEnable) {
                                    VideoController.this.LOG("mAboutDialogClickListener.onClick() : User select 'HDMI' item.");
                                    VideoController.this.mPlayer.DisplayHdmi();
                                    return;
                                } else {
                                    return;
                                }
                            case 4:
                                VideoController.this.mPlayer.DisplayHelpView();
                                return;
                            case 5:
                                VideoController.this.AboutDialog.dismiss();
                                return;
                            default:
                                return;
                        }
                    case 7:
                        switch (item) {
                            case 0:
                                VideoController.this.mPlayer.PlayFormBeginning();
                                return;
                            case 1:
                                VideoController.this.mPlayer.deleteBookmark();
                                return;
                            case 2:
                                VideoController.this.mPlayer.DisplayRepeat();
                                return;
                            case VideoController.SHOW_FAST /* 3 */:
                                if (VideoController.this.mTVOutEnable) {
                                    VideoController.this.mPlayer.DisplayTvout();
                                    return;
                                }
                                return;
                            case 4:
                                if (VideoController.this.mHdmiEnable) {
                                    VideoController.this.mPlayer.DisplayHdmi();
                                    return;
                                }
                                return;
                            case 5:
                                VideoController.this.mPlayer.DisplayHelpView();
                                return;
                            case 6:
                                VideoController.this.AboutDialog.dismiss();
                                return;
                            default:
                                return;
                        }
                    default:
                        return;
                }
            }
        };
        LOG(">>>>>>>>>>>>>Enter VideoController() tvout/hdmi =<<<<<<<<<<<<<<" + tvout + "/" + hdmi);
        this.mContext = context;
        this.mUseFastForward = true;
        initFloatingWindow();
        this.mBrightness = brightness;
        this.mTVOutEnable = tvout;
        this.mHdmiEnable = hdmi;
        addChoices();
    }

    private void initFloatingWindow() {
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mWindow = PolicyManager.makeNewWindow(this.mContext);
        this.mWindow.setWindowManager(this.mWindowManager, null, null);
        this.mWindow.requestFeature(1);
        this.mDecor = this.mWindow.getDecorView();
        this.mDecor.setOnTouchListener(this.mTouchListener);
        this.mWindow.setContentView(this);
        this.mWindow.setVolumeControlStream(SHOW_FAST);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(262144);
        requestFocus();
    }

    public void setMediaPlayer(VideoPlayerControl player) {
        this.mPlayer = player;
        updatePausePlay();
    }

    public void setAnchorView(View view) {
        this.mAnchor = view;
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(-1, -1);
        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.mRoot = inflate.inflate(R.layout.media_controller, (ViewGroup) null);
        initControllerView(this.mRoot);
        return this.mRoot;
    }

    private void initControllerView(View v) {
        LOG("Enter initControllerView");
        this.b = v.findViewById(R.id.menubar_button_pre);
        this.b.setOnClickListener(this);
        this.b = v.findViewById(R.id.menubar_button_next);
        this.b.setOnClickListener(this);
        this.b = v.findViewById(R.id.menubar_button_volumeminus);
        this.b.setOnClickListener(this);
        this.b = v.findViewById(R.id.menubar_button_volumeplus);
        this.b.setOnClickListener(this);
        this.mPreButton = (ImageView) v.findViewById(R.id.menubar_button_pre);
        if (this.mPreButton != null) {
            this.mPreButton.requestFocus();
            ((RepeatingImageButton) this.mPreButton).setRepeatListener(this.mRewListener, 260L);
        }
        this.mNextButton = (ImageView) v.findViewById(R.id.menubar_button_next);
        if (this.mNextButton != null) {
            this.mNextButton.requestFocus();
            ((RepeatingImageButton) this.mNextButton).setRepeatListener(this.mFfwdListener, 260L);
        }
        this.mPauseButton = (ImageView) v.findViewById(R.id.menubar_button_play);
        if (this.mPauseButton != null) {
            this.mPauseButton.requestFocus();
            this.mPauseButton.setOnClickListener(this.mPauseListener);
        }
        this.mBookmarkButton = (ImageView) v.findViewById(R.id.menubar_button_bookmark);
        if (this.mBookmarkButton != null) {
            this.mBookmarkButton.requestFocus();
            this.mBookmarkButton.setOnClickListener(this.mBookmarkListener);
        }
        this.mReturnButton = (ImageView) v.findViewById(R.id.menubar_button_return);
        if (this.mReturnButton != null) {
            this.mReturnButton.requestFocus();
            this.mReturnButton.setOnClickListener(this.mReturnListener);
        }
        this.mScreenBrightButton = (ImageView) v.findViewById(R.id.menubar_button_bright);
        if (this.mScreenBrightButton != null) {
            this.mScreenBrightButton.requestFocus();
            this.mScreenBrightButton.setOnClickListener(this.mScreenBrightListener);
        }
        this.mBrightness = DBUtils.getBacklight(this.mContext, this.mPlayer.getBrightMode());
        LOG("VideoController : mBrightness = " + DBUtils.getBacklight(this.mContext, this.mPlayer.getBrightMode()));
        setSrcByMode(CheckAndsetBrihtness(this.mBrightness));
        DBUtils.setBacklight(this.mContext, this.mPlayer.getBrightMode(), this.mBrightness);
        this.ScreenBrightnessMode = CheckAndsetBrihtness(this.mBrightness);
        this.mScreenModeButton = (ImageView) v.findViewById(R.id.menubar_button_screenmode);
        if (this.mScreenModeButton != null) {
            this.mScreenModeButton.requestFocus();
            this.mScreenModeButton.setOnClickListener(this.mScreenModeListener);
        }
        this.mAboutButton = (ImageView) v.findViewById(R.id.menubar_button_settings);
        if (this.mAboutButton != null) {
            this.mAboutButton.requestFocus();
            this.mAboutButton.setOnClickListener(this.mAboutListener);
        }
        this.mProgress = (ProgressBar) v.findViewById(R.id.mediacontroller_progress);
        if (this.mProgress != null) {
            if (this.mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) this.mProgress;
                seeker.setOnSeekBarChangeListener(this.mSeekListener);
            }
            this.mProgress.setMax(1000);
        }
        this.mVolumeMinus = (ImageView) v.findViewById(R.id.menubar_button_volumeminus);
        if (this.mVolumeMinus != null) {
            this.mVolumeMinus.requestFocus();
            ((RepeatingImageButton) this.mVolumeMinus).setRepeatListener(this.mVolumeMinusListener, 260L);
        }
        this.mVolumePlus = (ImageView) v.findViewById(R.id.menubar_button_volumeplus);
        if (this.mVolumePlus != null) {
            this.mVolumePlus.requestFocus();
            ((RepeatingImageButton) this.mVolumePlus).setRepeatListener(this.mVolumePlusListener, 260L);
        }
        this.mEndTime = (TextView) v.findViewById(R.id.time_total);
        this.mCurrentTime = (TextView) v.findViewById(R.id.time_current);
        this.mFormatBuilder = new StringBuilder();
        this.mFormatter = new Formatter(this.mFormatBuilder, Locale.getDefault());
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        LOG("Enter onClick()and v = " + v);
        switch (v.getId()) {
            case R.id.menubar_button_volumeminus /* 2131099685 */:
                show();
                DBUtils.volumeAdjust(this.mContext, this.mPlayer.getVolumeMode(), -1);
                return;
            case R.id.menubar_button_volumeplus /* 2131099689 */:
                show();
                DBUtils.volumeAdjust(this.mContext, this.mPlayer.getVolumeMode(), 1);
                return;
            case R.id.menubar_button_pre /* 2131099693 */:
                if (this.mEnable) {
                    hide();
                    this.mPlayer.prev();
                    return;
                }
                return;
            case R.id.menubar_button_next /* 2131099695 */:
                if (this.mEnable) {
                    hide();
                    this.mPlayer.next();
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void show() {
        show(sDefaultTimeout);
    }

    public void show(int timeout) {
        if (!this.mShowing && this.mAnchor != null) {
            setProgress();
            int[] anchorpos = new int[2];
            this.mAnchor.getLocationOnScreen(anchorpos);
            WindowManager.LayoutParams p = new WindowManager.LayoutParams();
            p.gravity = 48;
            p.width = this.mAnchor.getWidth();
            p.height = -2;
            p.x = 0;
            p.y = (anchorpos[1] + this.mAnchor.getHeight()) - p.height;
            p.format = -3;
            p.type = 1000;
            p.flags |= 131072;
            p.token = null;
            p.windowAnimations = 0;
            this.mWindowManager.addView(this.mDecor, p);
            this.mShowing = true;
        }
        updatePausePlay();
        this.mHandler.sendEmptyMessage(2);
        Message msg = this.mHandler.obtainMessage(1);
        if (timeout != 0) {
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(SHOW_FAST);
            this.mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    public void showfast(int timeout) {
        if (!this.mShowing && this.mAnchor != null) {
            setProgress(this.mSeekTime);
            int[] anchorpos = new int[2];
            this.mAnchor.getLocationOnScreen(anchorpos);
            WindowManager.LayoutParams p = new WindowManager.LayoutParams();
            p.gravity = 48;
            p.width = this.mAnchor.getWidth();
            p.height = -2;
            p.x = 0;
            p.y = (anchorpos[1] + this.mAnchor.getHeight()) - p.height;
            p.format = -3;
            p.type = 1000;
            p.flags |= 131072;
            p.token = null;
            p.windowAnimations = 0;
            this.mWindowManager.addView(this.mDecor, p);
            this.mShowing = true;
        }
        updatePausePlay();
        this.mHandler.sendEmptyMessage(SHOW_FAST);
        Message msg = this.mHandler.obtainMessage(1);
        if (timeout != 0) {
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
            this.mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public void hide() {
        if (this.mAnchor != null && this.mShowing) {
            try {
                this.mHandler.removeMessages(2);
                this.mHandler.removeMessages(SHOW_FAST);
                this.mWindowManager.removeView(this.mDecor);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "already removed");
            }
            this.mShowing = false;
        }
    }

    public String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        this.mFormatBuilder.setLength(0);
        return this.mFormatter.format("%02d:%02d:%02d", Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)).toString();
    }

    public int setProgress() {
        LOG("setProgress() : Entered : mPlayer = " + this.mPlayer + ", mDragging = " + this.mDragging);
        if (this.mPlayer == null) {
            return 0;
        }
        LOG("setProgress() : is MediaPlayer seeking ? : " + (this.mPlayer.isSeeking() ? "yes." : "no."));
        if (this.mPlayer.isSeeking()) {
            return 0;
        }
        int position = this.mPlayer.getCurrentPosition();
        int duration = this.mPlayer.getDuration();
        if (this.mProgress != null) {
            if (duration > 0) {
                long pos = (1000 * position) / duration;
                LOG("setProgress() : to set mProgress progress to '" + pos + "'.");
                this.mProgress.setProgress((int) pos);
            }
            int percent = this.mPlayer.getBufferPercentage();
            this.mProgress.setSecondaryProgress(percent * 10);
        }
        if (this.mEndTime != null) {
            this.mEndTime.setText(stringForTime(duration));
        }
        if (this.mCurrentTime != null) {
            this.mCurrentTime.setText(stringForTime(position));
        }
        return position;
    }

    public int setProgress(long time) {
        LOG("setProgress(long) : Entered : time = " + time);
        if (this.mPlayer == null || this.mDragging) {
            return 0;
        }
        int position = (int) time;
        int duration = this.mPlayer.getDuration();
        if (this.mProgress != null) {
            if (duration > 0) {
                long pos = (1000 * position) / duration;
                LOG("setProgress(long) : to set mProgress progress to '" + pos + "'.");
                this.mProgress.setProgress((int) pos);
            }
            int percent = this.mPlayer.getBufferPercentage();
            this.mProgress.setSecondaryProgress(percent * 10);
        }
        if (this.mEndTime != null) {
            this.mEndTime.setText(stringForTime(duration));
        }
        if (this.mCurrentTime != null) {
            this.mCurrentTime.setText(stringForTime(position));
        }
        return position;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        show(sDefaultTimeout);
        return true;
    }

    @Override // android.view.View
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getRepeatCount() == 0 && event.getAction() == 0 && (keyCode == 79 || keyCode == 85 || keyCode == 62)) {
            doPauseResume();
            show(sDefaultTimeout);
            return true;
        } else if (keyCode == 86) {
            if (this.mPlayer.isPlaying()) {
                this.mPlayer.pause();
                updatePausePlay();
            }
            return true;
        } else if (keyCode == 25) {
            DBUtils.volumeAdjust(this.mContext, this.mPlayer.getVolumeMode(), -1);
            return true;
        } else if (keyCode == 24) {
            DBUtils.volumeAdjust(this.mContext, this.mPlayer.getVolumeMode(), 1);
            return true;
        } else if (keyCode == 4 || keyCode == 82) {
            hide();
            return false;
        } else if (keyCode == SHOW_FAST) {
            LOG(">>>>>>>>>BUTTON_HOME CLICKED <<<<<<<<<");
            DBUtils.setbackBacklight(this.mContext, this.mPlayer.getBrightMode());
            DBUtils.setbackVolume(this.mContext, this.mPlayer.getBrightMode());
            return false;
        } else {
            show(sDefaultTimeout);
            return super.dispatchKeyEvent(event);
        }
    }

    public void updatePausePlay() {
        ImageView button;
        if (this.mRoot != null && (button = (ImageView) this.mRoot.findViewById(R.id.menubar_button_play)) != null) {
            if (this.mPlayer.isPlaying()) {
                button.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                button.setImageResource(android.R.drawable.ic_media_play);
            }
        }
    }

    public void doPauseResume() {
        if (this.mPlayer.isPlaying()) {
            this.mPlayer.pause();
        } else {
            this.mPlayer.start();
        }
        updatePausePlay();
    }

    private void showTime() {
        if (this.mPlayer != null) {
            long duration = this.mPlayer.getDuration();
            long nowtime = this.mPlayer.getCurrentPosition();
            if (this.mCurrentTime != null) {
                this.mCurrentTime.setText(stringForTime((int) nowtime));
            }
            if (this.mEndTime != null) {
                this.mEndTime.setText(stringForTime((int) duration));
            }
        }
    }

    public void scanBackward(int repcnt, long delta) {
        long delta2;
        LOG("Enter scanBackward()");
        if (this.mPlayer != null) {
            if (repcnt == 0) {
                this.mStartSeekPos = this.mPlayer.getCurrentPosition();
                this.mLastSeekEventTime = 0L;
                this.mSeeking = false;
                return;
            }
            this.mSeeking = true;
            if (delta < 3000) {
                delta2 = delta * 25;
            } else {
                delta2 = 75000 + ((delta - 3000) * 40);
            }
            long newpos = this.mStartSeekPos - delta2;
            if (newpos < 0) {
                this.mPlayer.pause();
                newpos = 0;
            }
            if (delta2 - this.mLastSeekEventTime > 250 || repcnt < 0) {
                if (this.mPlayer.isSeekComplete()) {
                    this.mPlayer.setSeekComplete(false);
                    this.mPlayer.seekTo((int) newpos, true);
                    this.mLastSeekEventTime = delta2;
                }
                this.mSeekTime = newpos;
            }
            if (repcnt >= 0) {
                this.mPosOverride = newpos;
            } else {
                this.mPosOverride = -1L;
            }
        }
    }

    public void scanForward(int repcnt, long delta) {
        long delta2;
        if (this.mPlayer != null) {
            if (repcnt == 0) {
                this.mStartSeekPos = this.mPlayer.getCurrentPosition();
                this.mLastSeekEventTime = 0L;
                this.mSeeking = false;
                return;
            }
            this.mSeeking = true;
            if (delta < 3000) {
                delta2 = delta * 25;
            } else {
                delta2 = 75000 + ((delta - 3000) * 40);
            }
            long newpos = this.mStartSeekPos + delta2;
            long duration = this.mPlayer.getDuration();
            if (newpos >= duration) {
                this.mPlayer.pause();
                newpos = duration - 2000;
            }
            if (delta2 - this.mLastSeekEventTime > 250 || repcnt < 0) {
                if (this.mPlayer.isSeekComplete()) {
                    this.mPlayer.setSeekComplete(false);
                    this.mPlayer.seekTo((int) newpos, true);
                    this.mLastSeekEventTime = delta2;
                }
                this.mSeekTime = newpos;
            }
            if (repcnt >= 0) {
                this.mPosOverride = newpos;
            } else {
                this.mPosOverride = -1L;
            }
        }
    }

    public void setScreenBright(int mode) {
        setScreenBrightness(mode);
        setSrcByMode(mode);
    }

    private void setScreenBrightness(int mode) {
        switch (mode) {
            case 0:
                DBUtils.setBacklight(this.mContext, this.mPlayer.getBrightMode(), 78);
                return;
            case 1:
                DBUtils.setBacklight(this.mContext, this.mPlayer.getBrightMode(), 102);
                return;
            case 2:
                DBUtils.setBacklight(this.mContext, this.mPlayer.getBrightMode(), 138);
                return;
            case SHOW_FAST /* 3 */:
                DBUtils.setBacklight(this.mContext, this.mPlayer.getBrightMode(), 188);
                return;
            case 4:
                DBUtils.setBacklight(this.mContext, this.mPlayer.getBrightMode(), 248);
                return;
            default:
                return;
        }
    }

    private int CheckAndsetBrihtness(int brightness) {
        if (this.mBrightness > 0 && this.mBrightness <= 78) {
            this.mScreenBrightButton.setImageResource(R.drawable.video_bright1);
            this.mBrightMode = 0;
        } else if (78 < this.mBrightness && this.mBrightness <= 102) {
            this.mBrightMode = 1;
            this.mScreenBrightButton.setImageResource(R.drawable.video_bright2);
        } else if (102 < this.mBrightness && this.mBrightness <= 138) {
            this.mBrightMode = 2;
            this.mScreenBrightButton.setImageResource(R.drawable.video_bright3);
        } else if (138 < this.mBrightness && this.mBrightness <= 188) {
            this.mBrightMode = SHOW_FAST;
            this.mScreenBrightButton.setImageResource(R.drawable.video_bright4);
        } else if (188 < this.mBrightness && this.mBrightness <= 255) {
            this.mBrightMode = 4;
            this.mScreenBrightButton.setImageResource(R.drawable.video_bright5);
        }
        return this.mBrightMode;
    }

    private void setSrcByMode(int mode) {
        switch (mode) {
            case 0:
                this.mScreenBrightButton.setImageResource(R.drawable.video_bright1);
                return;
            case 1:
                this.mScreenBrightButton.setImageResource(R.drawable.video_bright2);
                return;
            case 2:
                this.mScreenBrightButton.setImageResource(R.drawable.video_bright3);
                return;
            case SHOW_FAST /* 3 */:
                this.mScreenBrightButton.setImageResource(R.drawable.video_bright4);
                return;
            case 4:
                this.mScreenBrightButton.setImageResource(R.drawable.video_bright5);
                return;
            default:
                return;
        }
    }

    public void setScreen(int ScreenSizeMode) {
        setButtonSrcByMode(ScreenSizeMode);
        setScreensize(ScreenSizeMode);
    }

    private void setScreensize(int mode) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) this.mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int maxWidth = dm.widthPixels;
        int maxHeight = dm.heightPixels;
        LOG("maxWidth / maxHeight = " + maxWidth + "/ " + maxHeight + " mode:" + mode);
        switch (mode) {
            case 0:
                this.mPlayer.setScreenSize(this.mPlayer.getDefaultWidth(), this.mPlayer.getDefaultHeight());
                DBUtils.setScreenValue(this.mContext, 0);
                return;
            case 1:
                this.mPlayer.setScreenSize(maxWidth, (maxWidth / 16) * 9);
                DBUtils.setScreenValue(this.mContext, 1);
                return;
            case 2:
                this.mPlayer.setScreenSize((maxHeight / SHOW_FAST) * 4, maxHeight);
                DBUtils.setScreenValue(this.mContext, 2);
                return;
            case SHOW_FAST /* 3 */:
                if (this.mPlayer.getDefaultWidth() == 0 || this.mPlayer.getDefaultHeight() == 0) {
                    this.mPlayer.setScreenSize(maxWidth, maxHeight);
                    DBUtils.setScreenValue(this.mContext, SHOW_FAST);
                    return;
                }
                this.mPlayer.setScreenSize(maxWidth, maxHeight);
                DBUtils.setScreenValue(this.mContext, SHOW_FAST);
                return;
            default:
                return;
        }
    }

    public void setButtonSrcByMode(int mode) {
        switch (mode) {
            case 0:
                this.mScreenModeButton.setImageResource(R.drawable.video_screenmode_original);
                this.ScreenSizeMode = 0;
                return;
            case 1:
                this.mScreenModeButton.setImageResource(R.drawable.video_screenmode_169);
                this.ScreenSizeMode = 1;
                return;
            case 2:
                this.mScreenModeButton.setImageResource(R.drawable.video_screenmode_43);
                this.ScreenSizeMode = 2;
                return;
            case SHOW_FAST /* 3 */:
                this.mScreenModeButton.setImageResource(R.drawable.video_screenmode_full);
                this.ScreenSizeMode = SHOW_FAST;
                return;
            default:
                return;
        }
    }

    public void showAboutDialog() {
        this.AboutDialog = new AlertDialog.Builder(getContext()).setAdapter(new ChoiceListAdapter(getContext(), R.layout.choice_item, choiceItems), this.mAboutDialogClickListener).setTitle(R.string.resume_playing_title).show();
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

    private ArrayList addChoices() {
        Resources r = getContext().getResources();
        this.AboutCount = 5;
        ArrayList<String> items = new ArrayList<String>();
        String str1 = r.getString(R.string.resume_playing_restart);
        String str2 = r.getString(R.string.alert_deletebookmark);
        String str3 = r.getString(R.string.alert_repeatmode);
        String str4 = r.getString(R.string.alert_tvout);
        String str5 = r.getString(R.string.alert_hdmi);
        String str6 = r.getString(R.string.alert_help);
        String str8 = r.getString(R.string.alert_close);
        if (this.mTVOutEnable) {
            if (this.mHdmiEnable) {
                items.add(str1);
                items.add(str2);
                items.add(str3);
                items.add(str4);
                items.add(str5);
                items.add(str6);
                items.add(str8);
                this.AboutCount += 2;
            } else {
                items.add(str1);
                items.add(str2);
                items.add(str3);
                items.add(str4);
                items.add(str6);
                items.add(str8);
                this.AboutCount++;
            }
        } else if (this.mHdmiEnable) {
            items.add(str1);
            items.add(str2);
            items.add(str3);
            items.add(str5);
            items.add(str6);
            items.add(str8);
            this.AboutCount++;
        } else {
            items.add(str1);
            items.add(str2);
            items.add(str3);
            items.add(str6);
            items.add(str8);
        }
        choiceItems = new ArrayList<ChoiceItem>();
        for (int i = 0; i < this.AboutCount; i++) {
            ChoiceItem item = new ChoiceItem();
            item.content = items.get(i);
            choiceItems.add(item);
        }
        return choiceItems;
    }

    @Override // android.view.View
    public void setEnabled(boolean enabled) {
        if (this.mPauseButton != null) {
            this.mPauseButton.setEnabled(enabled);
        }
        if (this.mNextButton != null) {
            this.mNextButton.setEnabled(enabled);
        }
        if (this.mPreButton != null) {
            this.mPreButton.setEnabled(enabled);
        }
        if (this.mBookmarkButton != null) {
            this.mBookmarkButton.setEnabled(enabled);
        }
        if (this.mReturnButton != null) {
            this.mReturnButton.setEnabled(enabled);
        }
        if (this.mScreenBrightButton != null) {
            this.mScreenBrightButton.setEnabled(enabled);
        }
        if (this.mScreenModeButton != null) {
            this.mScreenModeButton.setEnabled(enabled);
        }
        if (this.mAboutButton != null) {
            this.mAboutButton.setEnabled(enabled);
        }
        if (this.mProgress != null) {
            this.mProgress.setEnabled(enabled);
        }
        if (this.mVolumeMinus != null) {
            this.mVolumeMinus.setEnabled(enabled);
        }
        if (this.mVolumePlus != null) {
            this.mVolumePlus.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }

    public void setDisableWhen3gp() {
        this.mPreButton.setEnabled(false);
        this.mNextButton.setEnabled(false);
        this.mProgress.setEnabled(false);
    }

    public void setShortClickButtonMode(boolean mode) {
        this.mEnable = mode;
    }
}
