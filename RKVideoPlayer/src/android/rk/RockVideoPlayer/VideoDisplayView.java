package android.rk.RockVideoPlayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.rk.RockVideoPlayer.DBUtils;
import android.rk.RockVideoPlayer.VideoController;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class VideoDisplayView extends SurfaceView implements VideoController.VideoPlayerControl {
    private static final boolean DEBUG = true;
    private static final String PREFS_NAME = "android.rk.RockVideoPlayer";
    private static final String TAG = "VideoDisplayView";
    AlertDialog BookmarkCheckDialog;
    AlertDialog RepeatDialog;
    private ArrayList<ChoiceItem> choiceItems;
    private VideoPlayActivity mActivity;
    public int mBrightMode;
    private int mBrightness;
    private Context mContext;
    private int mCurrentBufferPercentage;
    private int mDuration;
    public boolean mIsPrepared;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnErrorListener mOnErrorListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private boolean mPauseState;
    private Integer mPlayTime;
    private int mRepeatMode;
    private int mSeekWhenPrepared;
    private boolean mStartWhenPrepared;
    private int mSurfaceHeight;
    private int mSurfaceWidth;
    public boolean mTVOutEnable;
    private Uri mUri;
    private VideoController mVideoController;
    private int mVideoHeight;
    private int mVideoWidth;
    private static int sMaxWidth = -1;
    private static int sMaxHeight = -1;
    private SurfaceHolder mSurfaceHolder = null;
    private MediaPlayer mMediaPlayer = null;
    private boolean isNet3gp = false;
    private boolean mEnable = true;
    public boolean mSeekComplete = true;
    private int mAsyncSeekCount = 0;
    private final int sSingle = 2;
    private final int sRepeatOne = 1;
    private final int sRepeatAll = 0;
    private boolean mSetScreebSizeClicked = false;
    private final int HIDE_REPEATVIEW = 1;
    private final int BookmarkCheckDialog_FADE_OUT = 2;
    private final int TIME_SHOWCHECK = 500;
    private final int TIME_SHORT = 5000;
    private final int TIME_MIDDLE = 8000;
    private final int TIME_LONG = 10000;
    private boolean mVideoAvailable = false;
    private boolean mVideoUnavailable = false;
    private int mUnavailableCount = 0;
    private int screenSizeMode = 0;
    private boolean mCheckBookmark = false;
    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.5
        @Override // android.media.MediaPlayer.OnVideoSizeChangedListener
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            VideoDisplayView.this.LOG("Enter onVideoSizeChanged() and width = " + width + " height = " + height + " screenSizeMode=" + VideoDisplayView.this.screenSizeMode + " sMaxWidth=" + VideoDisplayView.sMaxWidth + " sMaxHeight=" + VideoDisplayView.sMaxHeight);
            if ((VideoDisplayView.this.screenSizeMode != 0 || (mp.getVideoWidth() <= VideoDisplayView.sMaxWidth && mp.getVideoHeight() <= VideoDisplayView.sMaxHeight)) && VideoDisplayView.this.screenSizeMode != 3) {
                VideoDisplayView.this.mVideoWidth = mp.getVideoWidth();
                VideoDisplayView.this.mVideoHeight = mp.getVideoHeight();
            } else {
                VideoDisplayView.this.mVideoWidth = mp.getVideoWidth();
                VideoDisplayView.this.mVideoHeight = mp.getVideoHeight();
            }
            if (VideoDisplayView.this.mVideoWidth != 0 && VideoDisplayView.this.mVideoHeight != 0) {
                if (VideoDisplayView.this.mVideoWidth > VideoDisplayView.sMaxWidth) {
                    VideoDisplayView.this.mVideoWidth = VideoDisplayView.sMaxWidth;
                }
                if (VideoDisplayView.this.mVideoHeight > VideoDisplayView.sMaxHeight) {
                    VideoDisplayView.this.mVideoHeight = VideoDisplayView.sMaxHeight;
                }
                VideoDisplayView.this.getHolder().setFixedSize(VideoDisplayView.this.mVideoWidth, VideoDisplayView.this.mVideoHeight);
                VideoDisplayView.this.LOG("onVideoSizeChanged(): setFixedSize() EXE");
            }
        }
    };
    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.6
        @Override // android.media.MediaPlayer.OnPreparedListener
        public void onPrepared(MediaPlayer mp) {
            Log.i(VideoDisplayView.TAG, "mPreparedListener.onPrepared() : Entered. ");
            VideoDisplayView.this.mIsPrepared = true;
            VideoDisplayView.this.mActivity.dismissLoading();
            View parentView = (View) VideoDisplayView.this.getParent();
            parentView.setOnClickListener(VideoDisplayView.this.fullscreenListener);
            VideoDisplayView.this.LOG("mOnPreparedListener = " + VideoDisplayView.this.mOnPreparedListener);
            if (VideoDisplayView.this.mOnPreparedListener != null) {
                VideoDisplayView.this.mOnPreparedListener.onPrepared(VideoDisplayView.this.mMediaPlayer);
            }
            VideoDisplayView.this.LOG("mVideoController = " + VideoDisplayView.this.mVideoController);
            if (VideoDisplayView.this.mVideoController != null) {
                VideoDisplayView.this.mVideoController.setEnabled(true);
                if (VideoDisplayView.this.isNet3gp) {
                    VideoDisplayView.this.mVideoController.setDisableWhen3gp();
                }
            }
            VideoDisplayView.this.LOG("MediaPlayer mp= " + mp);
            VideoDisplayView.this.mVideoWidth = mp.getVideoWidth();
            VideoDisplayView.this.mVideoHeight = mp.getVideoHeight();
            VideoDisplayView.this.screenSizeMode = DBUtils.getScreenValue(VideoDisplayView.this.mContext);
            VideoDisplayView.this.mVideoController.setButtonSrcByMode(VideoDisplayView.this.screenSizeMode);
            VideoDisplayView.this.setScreenMode(VideoDisplayView.this.screenSizeMode);
            VideoDisplayView.this.LOG("mVideoWidth = " + VideoDisplayView.this.mVideoWidth + " mVideoHeight = " + VideoDisplayView.this.mVideoHeight);
            VideoDisplayView.this.LOG("mSeekWhenPrepared = " + VideoDisplayView.this.mSeekWhenPrepared + " mStartWhenPrepared = " + VideoDisplayView.this.mStartWhenPrepared);
            if (VideoDisplayView.this.mVideoWidth == 0 || VideoDisplayView.this.mVideoHeight == 0) {
                if (VideoDisplayView.this.mSeekWhenPrepared != 0) {
                    VideoDisplayView.this.LOG("CASE 3 EXE");
                    VideoDisplayView.this.mMediaPlayer.start();
                    VideoDisplayView.this.mVideoAvailable = true;
                    VideoDisplayView.this.mHandler.postDelayed(VideoDisplayView.this.checkBookmarkRunnable, 500L);
                    VideoDisplayView.this.mCheckBookmark = true;
                    VideoDisplayView.this.mSeekWhenPrepared = 0;
                }
                if (VideoDisplayView.this.mStartWhenPrepared) {
                    VideoDisplayView.this.LOG("CASE 4 EXE");
                    VideoDisplayView.this.mMediaPlayer.start();
                    VideoDisplayView.this.mVideoAvailable = true;
                    VideoDisplayView.this.mHandler.postDelayed(VideoDisplayView.this.checkBookmarkRunnable, 500L);
                    VideoDisplayView.this.mCheckBookmark = true;
                    VideoDisplayView.this.mStartWhenPrepared = false;
                    return;
                }
                VideoDisplayView.this.LOG("CASE 6 EXE");
                VideoDisplayView.this.mCheckBookmark = true;
                VideoDisplayView.this.mVideoAvailable = true;
                VideoDisplayView.this.mMediaPlayer.start();
                VideoDisplayView.this.mStartWhenPrepared = false;
                VideoDisplayView.this.mSeekWhenPrepared = 0;
                VideoDisplayView.this.mHandler.postDelayed(VideoDisplayView.this.checkBookmarkRunnable, 500L);
                return;
            }
            Log.i("@@@@", "mSurfaceWidth and mSurfaceHeight size: " + VideoDisplayView.this.mSurfaceWidth + "/" + VideoDisplayView.this.mSurfaceHeight);
            if (VideoDisplayView.this.mSurfaceWidth == VideoDisplayView.this.mVideoWidth && VideoDisplayView.this.mSurfaceHeight == VideoDisplayView.this.mVideoHeight) {
                if (VideoDisplayView.this.mSeekWhenPrepared != 0) {
                    VideoDisplayView.this.LOG("CASE 1 EXE");
                    VideoDisplayView.this.mMediaPlayer.start();
                    VideoDisplayView.this.mVideoAvailable = true;
                    VideoDisplayView.this.mHandler.postDelayed(VideoDisplayView.this.checkBookmarkRunnable, 500L);
                    VideoDisplayView.this.mCheckBookmark = true;
                    VideoDisplayView.this.mSeekWhenPrepared = 0;
                }
                if (VideoDisplayView.this.mStartWhenPrepared) {
                    VideoDisplayView.this.LOG("CASE 2 EXE");
                    VideoDisplayView.this.mMediaPlayer.start();
                    VideoDisplayView.this.mVideoAvailable = true;
                    VideoDisplayView.this.mHandler.postDelayed(VideoDisplayView.this.checkBookmarkRunnable, 500L);
                    VideoDisplayView.this.mCheckBookmark = true;
                    VideoDisplayView.this.mStartWhenPrepared = false;
                }
                if (VideoDisplayView.this.mSeekWhenPrepared != 0 || VideoDisplayView.this.mStartWhenPrepared) {
                    if (VideoDisplayView.this.isPlaying() || VideoDisplayView.this.mSeekWhenPrepared != 0 || VideoDisplayView.this.getCurrentPosition() > 0) {
                    }
                    return;
                }
                VideoDisplayView.this.LOG("CASE 7 EXE");
                VideoDisplayView.this.mMediaPlayer.start();
                VideoDisplayView.this.mVideoAvailable = true;
                VideoDisplayView.this.mHandler.postDelayed(VideoDisplayView.this.checkBookmarkRunnable, 500L);
                VideoDisplayView.this.mCheckBookmark = true;
                return;
            }
            VideoDisplayView.this.mMediaPlayer.start();
            VideoDisplayView.this.mVideoAvailable = true;
            VideoDisplayView.this.mHandler.postDelayed(VideoDisplayView.this.checkBookmarkRunnable, 500L);
            VideoDisplayView.this.mCheckBookmark = true;
            VideoDisplayView.this.mSeekWhenPrepared = 0;
        }
    };
    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.7
        @Override // android.media.MediaPlayer.OnCompletionListener
        public void onCompletion(MediaPlayer mp) {
            if (VideoDisplayView.this.mVideoController != null) {
                VideoDisplayView.this.mVideoController.hide();
            }
            VideoDisplayView.this.afterVideoComplete();
        }
    };
    private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.8
        @Override // android.media.MediaPlayer.OnSeekCompleteListener
        public void onSeekComplete(MediaPlayer mp) {
            VideoDisplayView.this.LOG("mOnSeekCompleteListener.onSeekComplete() : mSeekComplete = " + VideoDisplayView.this.mSeekComplete);
            VideoDisplayView.access$2310(VideoDisplayView.this);
            VideoDisplayView.this.mSeekComplete = true;
        }
    };
    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.9
        @Override // android.media.MediaPlayer.OnErrorListener
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            int messageId;
            VideoDisplayView.this.LOG("Video Display View Error: " + framework_err + "," + impl_err);
            if (VideoDisplayView.this.mVideoController != null) {
                VideoDisplayView.this.mVideoController.hide();
            }
            VideoDisplayView.this.LOG("+++++++++++++++++++++++++mVideoController == null");
            if (VideoDisplayView.this.mMediaPlayer != null) {
                VideoDisplayView.this.mMediaPlayer.setScreenOnWhilePlaying(false);
            }
            VideoDisplayView.this.LOG("+++++++++++++++++++++++++mMediaPlayer == null");
            if (VideoDisplayView.this.mOnErrorListener == null || !VideoDisplayView.this.mOnErrorListener.onError(VideoDisplayView.this.mMediaPlayer, framework_err, impl_err)) {
                VideoDisplayView.this.LOG("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                if (VideoDisplayView.this.getWindowToken() != null) {
                    VideoDisplayView.this.LOG("++++++++++++++++++++++++++++++++++++getWindowToken() != null");
                    VideoDisplayView.this.mContext.getResources();
                    if (framework_err == 200) {
                        messageId = R.string.VideoView_error_text_invalid_progressive_playback;
                    } else if (impl_err == -11) {
                        messageId = R.string.VideoView_error_networkfail;
                    } else {
                        messageId = R.string.VideoView_error_text_unknown;
                    }
                    if (VideoDisplayView.this.mRepeatMode == 0) {
                        String tUriString = VideoDisplayView.this.mUri.toString();
                        if (tUriString.indexOf("sdcard") != -1) {
                            VideoDisplayView.this.mVideoUnavailable = true;
                            VideoDisplayView.access$2708(VideoDisplayView.this);
                            Toast.makeText(VideoDisplayView.this.mContext, messageId, 1).show();
                            if (VideoDisplayView.this.mVideoAvailable) {
                                VideoDisplayView.this.next();
                            } else if (VideoDisplayView.this.mVideoUnavailable) {
                                if (VideoDisplayView.this.mUnavailableCount >= DBUtils.getVideoCount(VideoDisplayView.this.mContext)) {
                                    VideoDisplayView.this.mActivity.finish();
                                } else {
                                    VideoDisplayView.this.next();
                                }
                            }
                        } else {
                            new AlertDialog.Builder(VideoDisplayView.this.mContext).setTitle(R.string.VideoView_error_title).setMessage(messageId).setPositiveButton(R.string.VideoView_error_button, new DialogInterface.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.9.1
                                @Override // android.content.DialogInterface.OnClickListener
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if (VideoDisplayView.this.mOnCompletionListener != null) {
                                        VideoDisplayView.this.mOnCompletionListener.onCompletion(VideoDisplayView.this.mMediaPlayer);
                                    }
                                    VideoDisplayView.this.mActivity.Finish();
                                }
                            }).setCancelable(true).show();
                        }
                    } else {
                        new AlertDialog.Builder(VideoDisplayView.this.mContext).setTitle(R.string.VideoView_error_title).setMessage(messageId).setPositiveButton(R.string.VideoView_error_button, new DialogInterface.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.9.2
                            @Override // android.content.DialogInterface.OnClickListener
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (VideoDisplayView.this.mOnCompletionListener != null) {
                                    VideoDisplayView.this.mOnCompletionListener.onCompletion(VideoDisplayView.this.mMediaPlayer);
                                }
                                VideoDisplayView.this.mActivity.Finish();
                            }
                        }).setCancelable(true).show();
                    }
                }
                return true;
            }
            VideoDisplayView.this.LOG(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.use on error listener");
            return true;
        }
    };
    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.10
        @Override // android.media.MediaPlayer.OnBufferingUpdateListener
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            VideoDisplayView.this.mCurrentBufferPercentage = percent;
        }
    };
    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.11
        @Override // android.view.SurfaceHolder.Callback
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            VideoDisplayView.this.LOG("mSHCallback.surfaceChanged() : Enter : format = " + format + "; w = " + w + "; h" + h);
            VideoDisplayView.this.mSurfaceWidth = w;
            VideoDisplayView.this.mSurfaceHeight = h;
            if (VideoDisplayView.this.mMediaPlayer == null || VideoDisplayView.this.mMediaPlayer.getCurrentPosition() > 0) {
            }
            if (VideoDisplayView.this.mMediaPlayer != null && VideoDisplayView.this.mIsPrepared && VideoDisplayView.this.mVideoWidth == w && VideoDisplayView.this.mVideoHeight == h) {
                VideoDisplayView.this.LOG("CASE 5 EXE");
                if (!VideoDisplayView.this.mSetScreebSizeClicked) {
                    VideoDisplayView.this.mMediaPlayer.start();
                    if (!VideoDisplayView.this.mCheckBookmark) {
                        VideoDisplayView.this.mHandler.postDelayed(VideoDisplayView.this.checkBookmarkRunnable, 500L);
                    }
                    VideoDisplayView.this.mSeekWhenPrepared = 0;
                }
            }
        }

        @Override // android.view.SurfaceHolder.Callback
        public void surfaceCreated(SurfaceHolder holder) {
            VideoDisplayView.this.LOG("mSHCallback.surfaceCreated() : Enter.");
            VideoDisplayView.this.mSurfaceHolder = holder;
            VideoDisplayView.this.openVideo();
        }

        @Override // android.view.SurfaceHolder.Callback
        public void surfaceDestroyed(SurfaceHolder holder) {
            VideoDisplayView.this.mSurfaceHolder = null;
            if (VideoDisplayView.this.mVideoController != null) {
                VideoDisplayView.this.mVideoController.hide();
            }
            if (VideoDisplayView.this.mMediaPlayer != null) {
                VideoDisplayView.this.mMediaPlayer.reset();
                VideoDisplayView.this.mMediaPlayer.release();
                VideoDisplayView.this.mMediaPlayer = null;
            }
            if (VideoDisplayView.this.mActivity != null) {
                VideoDisplayView.this.mActivity.Finish();
            }
        }
    };
    Handler mHandler = new Handler() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.12
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    VideoDisplayView.this.RepeatDialog.dismiss();
                    return;
                case DBUtils.Def.MODE_USER /* 2 */:
                    VideoDisplayView.this.BookmarkCheckDialog.dismiss();
                    return;
                default:
                    return;
            }
        }
    };
    Runnable checkBookmarkRunnable = new Runnable() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.13
        @Override // java.lang.Runnable
        public void run() {
            VideoDisplayView.this.CheckIfHaveBookmark();
        }
    };
    View.OnClickListener fullscreenListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.14
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            VideoDisplayView.this.toggleMediaControlsVisiblity();
        }
    };
    private View.OnClickListener mBookmarkCheckListener = new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.15
        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play_begin /* 2131099649 */:
                    VideoDisplayView.this.BookmarkCheckDialog.dismiss();
                    return;
                case R.id.play_bookmark /* 2131099650 */:
                    VideoDisplayView.this.mMediaPlayer.seekTo(VideoDisplayView.this.mPlayTime.intValue());
                    VideoDisplayView.this.BookmarkCheckDialog.dismiss();
                    return;
                default:
                    return;
            }
        }
    };
    private View mBookmarkCheckView;

    static /* synthetic */ int access$2310(VideoDisplayView x0) {
        int i = x0.mAsyncSeekCount;
        x0.mAsyncSeekCount = i - 1;
        return i;
    }

    static /* synthetic */ int access$2708(VideoDisplayView x0) {
        int i = x0.mUnavailableCount;
        x0.mUnavailableCount = i + 1;
        return i;
    }

    public void LOG(String msg) {
        Log.d(TAG, msg);
    }

    /* JADX WARN: Type inference failed for: r0v19, types: [android.rk.RockVideoPlayer.VideoDisplayView$1] */
    public VideoDisplayView(Context context) {
        super(context);
        LOG("Enter VideoDisplayView");
        this.mContext = context;
        this.mBookmarkCheckView = inflatView(R.layout.bookmark_check);
        new Thread() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.1
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                VideoDisplayView.this.mBookmarkCheckView.setOnClickListener(VideoDisplayView.this.mBookmarkCheckListener);
                Button play_begin = (Button) VideoDisplayView.this.mBookmarkCheckView.findViewById(R.id.play_begin);
                Button play_bookmark = (Button) VideoDisplayView.this.mBookmarkCheckView.findViewById(R.id.play_bookmark);
                play_begin.setOnClickListener(VideoDisplayView.this.mBookmarkCheckListener);
                play_bookmark.setOnClickListener(VideoDisplayView.this.mBookmarkCheckListener);
                VideoDisplayView.this.mRepeatMode = VideoDisplayView.this.GetRepeatMode();
                VideoDisplayView.this.initVideoDisplayView();
            }
        }.start();
    }

    /* JADX WARN: Type inference failed for: r0v19, types: [android.rk.RockVideoPlayer.VideoDisplayView$2] */
    public VideoDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        LOG("Enter VideoDisplayView");
        this.mContext = context;
        this.mBookmarkCheckView = inflatView(R.layout.bookmark_check);
        new Thread() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.2
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                Button play_begin = (Button) VideoDisplayView.this.mBookmarkCheckView.findViewById(R.id.play_begin);
                Button play_bookmark = (Button) VideoDisplayView.this.mBookmarkCheckView.findViewById(R.id.play_bookmark);
                play_begin.setOnClickListener(VideoDisplayView.this.mBookmarkCheckListener);
                play_bookmark.setOnClickListener(VideoDisplayView.this.mBookmarkCheckListener);
                VideoDisplayView.this.mRepeatMode = VideoDisplayView.this.GetRepeatMode();
                VideoDisplayView.this.initVideoDisplayView();
            }
        }.start();
    }

    /* JADX WARN: Type inference failed for: r0v19, types: [android.rk.RockVideoPlayer.VideoDisplayView$3] */
    public VideoDisplayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LOG("Enter VideoDisplayView");
        this.mContext = context;
        this.mBookmarkCheckView = inflatView(R.layout.bookmark_check);
        new Thread() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.3
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                VideoDisplayView.this.mBookmarkCheckView.setOnClickListener(VideoDisplayView.this.mBookmarkCheckListener);
                Button play_begin = (Button) VideoDisplayView.this.mBookmarkCheckView.findViewById(R.id.play_begin);
                Button play_bookmark = (Button) VideoDisplayView.this.mBookmarkCheckView.findViewById(R.id.play_bookmark);
                play_begin.setOnClickListener(VideoDisplayView.this.mBookmarkCheckListener);
                play_bookmark.setOnClickListener(VideoDisplayView.this.mBookmarkCheckListener);
                VideoDisplayView.this.mRepeatMode = VideoDisplayView.this.GetRepeatMode();
                VideoDisplayView.this.initVideoDisplayView();
            }
        }.start();
    }

    @Override // android.view.SurfaceView, android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i("@@@@@@@@@@", "setting size: " + this.mVideoWidth + 'x' + this.mVideoHeight);
        setMeasuredDimension(this.mVideoWidth, this.mVideoHeight);
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case Integer.MIN_VALUE:
                int result = Math.min(desiredSize, specSize);
                return result;
            case 0:
                return desiredSize;
            case 1073741824:
                return specSize;
            default:
                return desiredSize;
        }
    }

    public void initVideoDisplayView() {
        LOG("Enter initVideoDisplayView()");
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) this.mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        this.mVideoHeight = dm.heightPixels;
        this.mVideoWidth = dm.widthPixels;
        sMaxWidth = dm.widthPixels;
        sMaxHeight = dm.heightPixels;
        getHolder().addCallback(this.mSHCallback);
        getHolder().setType(3);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        LOG("initVideoDisplayView() Ended");
        setOnClickListener(new View.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.4
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                VideoDisplayView.this.LOG("VideoDisplayView:onClick");
                VideoDisplayView.this.toggleMediaControlsVisiblity();
            }
        });
    }

    public void setVideoPath(String path) {
        LOG("Enter setVideoPath()");
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        LOG("Enter setVideoURI()");
        this.mUri = uri;
        this.mStartWhenPrepared = false;
        this.mSeekWhenPrepared = 0;
        this.mCheckBookmark = false;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void stopPlayback() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
        }
    }

    public void openVideo() {
        LOG("Enter the openVideo()");
        if (this.mUri != null && this.mSurfaceHolder != null) {
            Intent i = new Intent("android.rk.RockAudioPlayer.musicservicecommand");
            i.putExtra("command", "pause");
            this.mContext.sendBroadcast(i);
            LOG("Intent i sended");
            Intent j = new Intent("com.android.music.musicservicecommand");
            j.putExtra("command", "pause");
            this.mContext.sendBroadcast(j);
            LOG("Intent j sended");
            LOG("mContext = " + this.mContext + "mUri = " + this.mUri);
            this.mPlayTime = DBUtils.getBookmark(this.mContext, this.mUri);
            LOG("mPlayTime = " + this.mPlayTime);
            if (this.mMediaPlayer != null) {
                this.mMediaPlayer.reset();
                this.mMediaPlayer.release();
                this.mMediaPlayer = null;
            }
            try {
                this.mMediaPlayer = new MediaPlayer();
                LOG("mMediaPlayer = " + this.mMediaPlayer);
                this.mIsPrepared = false;
                this.mMediaPlayer.setOnPreparedListener(this.mPreparedListener);
                LOG("reset duration to -1 in openVideo");
                this.mDuration = -1;
                this.mMediaPlayer.setOnVideoSizeChangedListener(this.mSizeChangedListener);
                this.mMediaPlayer.setOnCompletionListener(this.mCompletionListener);
                this.mMediaPlayer.setOnErrorListener(this.mErrorListener);
                this.mMediaPlayer.setOnBufferingUpdateListener(this.mBufferingUpdateListener);
                this.mMediaPlayer.setOnSeekCompleteListener(this.mOnSeekCompleteListener);
                this.mCurrentBufferPercentage = 0;
                LOG("openVideo() : Video to be played : " + this.mUri);
                this.mMediaPlayer.setDataSource(this.mContext, this.mUri);
                this.mMediaPlayer.setDisplay(this.mSurfaceHolder);
                this.mMediaPlayer.setAudioStreamType(3);
                this.mMediaPlayer.setScreenOnWhilePlaying(false);
                LOG("openVideo() : To prepare player asynchronously.");
                this.mMediaPlayer.prepareAsync();
                attachMediaController();
                DBUtils.setBacklight(this.mContext, this.mActivity.getBrightMode());
                LOG("VideoDisplayView:bright = " + DBUtils.getBacklight(this.mContext, this.mActivity.getBrightMode()));
                CheckAndsetBrihtness(DBUtils.getBacklight(this.mContext, this.mActivity.getBrightMode()));
            } catch (IOException ex) {
                Log.w(TAG, "Unable to open content: " + this.mUri, ex);
                this.mErrorListener.onError(this.mMediaPlayer, 0, 0);
            } catch (IllegalArgumentException ex2) {
                Log.w(TAG, "Unable to open content: " + this.mUri, ex2);
            }
        }
    }

    public void setMediaController(VideoController controller) {
        if (this.mVideoController != null) {
            this.mVideoController.hide();
        }
        this.mVideoController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (this.mMediaPlayer != null && this.mVideoController != null) {
            this.mVideoController.setMediaPlayer(this);
            View anchorView = getParent() instanceof View ? (View) getParent() : this;
            this.mVideoController.setAnchorView(anchorView);
            this.mVideoController.setEnabled(this.mIsPrepared);
            if (this.isNet3gp) {
                this.mVideoController.setDisableWhen3gp();
            }
        }
    }

    public void afterVideoComplete() {
        switch (this.mRepeatMode) {
            case 0:
                next(0);
                return;
            case 1:
                next(1);
                return;
            case DBUtils.Def.MODE_USER /* 2 */:
                Finish();
                return;
            default:
                return;
        }
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        LOG("Enter setOnPreparedListener()");
        this.mOnPreparedListener = l;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
        this.mOnCompletionListener = l;
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
        this.mOnErrorListener = l;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        if (!this.mIsPrepared || this.mMediaPlayer == null || this.mVideoController != null) {
        }
        return false;
    }

    @Override // android.view.View
    public boolean onTrackballEvent(MotionEvent ev) {
        if (!this.mIsPrepared || this.mMediaPlayer == null || this.mVideoController == null) {
            return false;
        }
        toggleMediaControlsVisiblity();
        return false;
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LOG("Enter onKeyDown() keyCode =" + keyCode);
        if (!(!this.mIsPrepared || keyCode == 24 || keyCode == 25 || keyCode == 82 || keyCode == 5 || keyCode == 6 || this.mMediaPlayer == null || this.mVideoController == null)) {
            if (keyCode == 79 || keyCode == 85) {
                if (this.mMediaPlayer.isPlaying()) {
                    pause();
                    this.mVideoController.show();
                } else {
                    start();
                    this.mVideoController.hide();
                }
                return true;
            } else if (keyCode == 86 && this.mMediaPlayer.isPlaying()) {
                pause();
                this.mVideoController.show();
            } else if (keyCode == 4) {
                Finish();
            } else {
                toggleMediaControlsVisiblity();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void toggleMediaControlsVisiblity() {
        LOG("Enter toggleMediaControlsVisiblity()");
        LOG("mVideoController = " + this.mVideoController);
        if (!this.mTVOutEnable && this.mMediaPlayer != null && this.mIsPrepared) {
            if (this.mVideoController.isShowing()) {
                this.mVideoController.hide();
            } else {
                this.mVideoController.show();
            }
        }
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void start() {
        if (this.mMediaPlayer == null || !this.mIsPrepared) {
            this.mStartWhenPrepared = true;
            return;
        }
        this.mMediaPlayer.start();
        this.mStartWhenPrepared = false;
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void pause() {
        if (this.mMediaPlayer != null && this.mIsPrepared && this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.pause();
            System.gc();
        }
        this.mStartWhenPrepared = false;
    }

    public void StopPlay() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.reset();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
        }
    }

    public void prev(int mode) {
        LOG("Enter prev() and mUri = " + this.mUri);
        switch (mode) {
            case 0:
                prev();
                return;
            case 1:
                prevWithRepeatCheck();
                return;
            case DBUtils.Def.MODE_USER /* 2 */:
                Finish();
                return;
            default:
                prev();
                return;
        }
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void prev() {
        if (this.mVideoController != null) {
            this.mVideoController.hide();
        }
        Cursor cur = DBUtils.getPrevCursor(this.mContext, this.mUri);
        if (cur != null) {
            Uri uri = MediaStore.Video.Media.getContentUri("external");
            Uri AimUri = Uri.withAppendedPath(uri, cur.getString(cur.getColumnIndexOrThrow("_id")));
            LOG(" Prev Uri = " + AimUri);
            cur.close();
            pause();
            StopPlay();
            this.mActivity.setUri(AimUri);
            setVideoURI(AimUri);
            initStatus();
        }
    }

    private void prevWithRepeatCheck() {
        Cursor cur;
        if (this.mRepeatMode == 1 && (cur = DBUtils.getCurrentCursor(this.mContext, this.mUri)) != null) {
            cur.close();
            pause();
            StopPlay();
            this.mActivity.setUri(this.mUri);
            setVideoURI(this.mUri);
            initStatus();
        }
    }

    public void next(int mode) {
        LOG("Enter next() and mUri = " + this.mUri);
        switch (mode) {
            case 0:
                next();
                return;
            case 1:
                nextWithRepeatCheck();
                return;
            case DBUtils.Def.MODE_USER /* 2 */:
                Finish();
                return;
            default:
                return;
        }
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void next() {
        if (this.mVideoController != null) {
            this.mVideoController.hide();
        }
        Cursor cur = DBUtils.getNextCursor(this.mContext, this.mUri);
        if (cur != null) {
            Uri uri = MediaStore.Video.Media.getContentUri("external");
            Uri AimUri = Uri.withAppendedPath(uri, cur.getString(cur.getColumnIndexOrThrow("_id")));
            cur.close();
            LOG(" Next Uri = " + AimUri);
            pause();
            StopPlay();
            this.mActivity.setUri(AimUri);
            setVideoURI(AimUri);
            initStatus();
            return;
        }
        this.mActivity.finish();
    }

    private void nextWithRepeatCheck() {
        if (this.mRepeatMode == 1) {
            Cursor cur = DBUtils.getCurrentCursor(this.mContext, this.mUri);
            if (cur != null) {
                cur.close();
                pause();
                StopPlay();
                this.mActivity.setUri(this.mUri);
                setVideoURI(this.mUri);
                initStatus();
                return;
            }
            this.mActivity.finish();
        }
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public int getDuration() {
        if (this.mMediaPlayer == null || !this.mIsPrepared) {
            this.mDuration = -1;
            return this.mDuration;
        } else if (this.mDuration > 0) {
            return this.mDuration;
        } else {
            this.mDuration = this.mMediaPlayer.getDuration();
            return this.mDuration;
        }
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public int getCurrentPosition() {
        if (this.mMediaPlayer == null || !this.mIsPrepared) {
            return 0;
        }
        return this.mMediaPlayer.getCurrentPosition();
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void seekTo(int msec, boolean isForceble) {
        if (this.mAsyncSeekCount != 0 && !isForceble) {
            return;
        }
        if (this.mMediaPlayer == null || !this.mIsPrepared) {
            this.mSeekWhenPrepared = msec;
            return;
        }
        LOG("seekTo() : to seek to '" + msec + "' ms.");
        this.mMediaPlayer.seekTo(msec);
        this.mAsyncSeekCount++;
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public boolean isPlaying() {
        if (this.mMediaPlayer == null || !this.mIsPrepared) {
            return false;
        }
        return this.mMediaPlayer.isPlaying();
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public boolean isSeeking() {
        return this.mAsyncSeekCount > 0;
    }

    public boolean isDismiss() {
        if (this.mMediaPlayer != null) {
            return this.mMediaPlayer.isPlaying();
        }
        return false;
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public int getBufferPercentage() {
        if (this.mMediaPlayer != null) {
            return this.mCurrentBufferPercentage;
        }
        return 0;
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public Uri getCurrentUri() {
        return this.mUri;
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public int setScreenBright(int ScreenBright) {
        return 0;
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void setScreenSize(int width, int height) {
        LOG("Enter setScreenSize() and width = " + width + " height = " + height + " sMaxWidth=" + sMaxWidth + " sMaxHeight=" + sMaxHeight);
        getHolder().setFixedSize(671492102, 671492102);
        getHolder().setType(3);
        this.mHandler.postDelayed(null, 100L);
        if (width > sMaxWidth) {
            width = sMaxWidth;
        }
        if (height > sMaxHeight) {
            height = sMaxHeight;
        }
        LOG("Enter setFixedSize() and width = " + width + " height = " + height);
        getHolder().setFixedSize(width, height);
        this.mVideoWidth = width;
        this.mVideoHeight = height;
        LOG("setScreenSize : setFixedSize() EXE");
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public int setScreenMode(int ScreenMode) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) this.mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int maxWidth = dm.widthPixels;
        int maxHeight = dm.heightPixels;
        LOG("maxWidth / maxHeight = " + maxWidth + "/ " + maxHeight);
        LOG(">>>>>>>ScreenMode:=" + ScreenMode);
        switch (ScreenMode) {
            case 0:
                setScreenSize(this.mMediaPlayer.getVideoWidth(), this.mMediaPlayer.getVideoHeight());
                return 0;
            case 1:
                setScreenSize(maxWidth, (maxWidth / 16) * 9);
                return 0;
            case DBUtils.Def.MODE_USER /* 2 */:
                setScreenSize((maxHeight / 3) * 4, maxHeight);
                return 0;
            case 3:
                if (this.mMediaPlayer.getVideoWidth() == 0 || this.mMediaPlayer.getVideoHeight() == 0) {
                    LOG("+++++++++++++++++++++ERROR:" + this.mMediaPlayer.getVideoWidth() + "*" + this.mMediaPlayer.getVideoHeight());
                    setScreenSize(maxWidth, maxHeight);
                    return 0;
                }
                setScreenSize(maxWidth, maxHeight);
                return 0;
            default:
                return 0;
        }
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public boolean Capture() {
        return true;
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void setBookmark(int bookmark) {
        LOG("Enter setBookmark()");
        Cursor cur = DBUtils.getCurrentCursor(this.mContext, this.mUri);
        LOG("Cur = " + cur);
        if (cur == null) {
            Toast.makeText(this.mContext, (int) R.string.addbookmark_false, 0).show();
        } else if (DBUtils.setBookmark(this.mContext, cur, bookmark)) {
            cur.close();
            Toast.makeText(this.mContext, (int) R.string.addbookmark_sucess, 0).show();
        } else {
            cur.close();
            Toast.makeText(this.mContext, (int) R.string.addbookmark_false, 0).show();
        }
    }

    public void setActivity(VideoPlayActivity newactivity) {
        this.mActivity = newactivity;
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public int getDefaultWidth() {
        return this.mMediaPlayer.getVideoWidth();
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public int getDefaultHeight() {
        return this.mMediaPlayer.getVideoHeight();
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void DisplayVersionView() {
        this.mActivity.DisplayVersionView();
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void Finish() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.pause();
        }
        StopPlay();
        this.mActivity.finish();
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void deleteBookmark() {
        DBUtils.deleteBookmark(this.mContext, this.mUri);
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void DisplayHelpView() {
        this.mActivity.DisplayHelpView();
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void PlayFormBeginning() {
        if (!DBUtils.isBookmarkNull(this.mContext, this.mUri)) {
            StopPlay();
            DBUtils.deleteBookmark(this.mContext, this.mUri);
            setVideoURI(this.mUri);
        }
    }

    public void CheckIfHaveBookmark() {
        this.mActivity.dismissLoading();
        if (this.mMediaPlayer != null && this.mIsPrepared && this.mPlayTime != null && this.mPlayTime.intValue() > 0) {
            if (this.mVideoController != null) {
                this.mVideoController.hide();
            }
            if (this.BookmarkCheckDialog == null) {
                this.mHandler.removeMessages(2);
                this.BookmarkCheckDialog = new AlertDialog.Builder(this.mContext).setView(this.mBookmarkCheckView).setTitle(R.string.VideoView_playfromchoice_title).setCancelable(true).show();
                Message msg = this.mHandler.obtainMessage(2);
                this.mHandler.sendMessageDelayed(msg, 5000L);
                return;
            }
            Message msg2 = this.mHandler.obtainMessage(2);
            this.mHandler.sendMessageDelayed(msg2, 5000L);
            this.BookmarkCheckDialog.show();
        }
    }

    public void setDisableWhen3gp(boolean ifTrue) {
        this.isNet3gp = true;
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void setEnable(boolean mode) {
        this.mEnable = mode;
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public boolean isSeekComplete() {
        return this.mSeekComplete;
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void setSeekComplete(boolean mode) {
        this.mSeekComplete = false;
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void hideController() {
        if (this.mVideoController != null) {
            this.mVideoController.hide();
        }
    }

    private void initStatus() {
        this.mEnable = true;
        this.mSeekComplete = true;
        this.mVideoController.mSeekTime = 0L;
        this.mSetScreebSizeClicked = false;
    }

    private int[] AdjustSize(int width, int height) {
        int[] a = new int[2];
        if (sMaxWidth * height > sMaxHeight * width) {
            Log.i("@@@", "image too tall, correcting");
            height = (sMaxHeight * width) / sMaxWidth;
        } else if (sMaxWidth * height < sMaxHeight * width) {
            Log.i("@@@", "image too wide, correcting");
            width = (sMaxWidth * height) / sMaxHeight;
        } else {
            Log.i("@@@", "aspect ratio is correct: " + width + "/" + height + "=" + sMaxWidth + "/" + sMaxHeight);
        }
        a[0] = width;
        a[1] = height;
        return a;
    }

    private View inflatView(int layoutid) {
        return LayoutInflater.from(this.mContext).inflate(layoutid, (ViewGroup) null);
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void DisplayRepeat() {
        this.mHandler.removeMessages(1);
        this.RepeatDialog = new AlertDialog.Builder(getContext()).setAdapter(new ChoiceListAdapter(getContext(), R.layout.choice_item, addChoices()), new DialogInterface.OnClickListener() { // from class: android.rk.RockVideoPlayer.VideoDisplayView.16
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        VideoDisplayView.this.mRepeatMode = 2;
                        VideoDisplayView.this.StoreRepeatMode(2);
                        VideoDisplayView.this.RepeatDialog.dismiss();
                        break;
                    case 1:
                        VideoDisplayView.this.mRepeatMode = 1;
                        VideoDisplayView.this.StoreRepeatMode(1);
                        VideoDisplayView.this.RepeatDialog.dismiss();
                        break;
                    case DBUtils.Def.MODE_USER /* 2 */:
                        VideoDisplayView.this.mRepeatMode = 0;
                        VideoDisplayView.this.StoreRepeatMode(0);
                        VideoDisplayView.this.RepeatDialog.dismiss();
                        break;
                    case 3:
                        VideoDisplayView.this.RepeatDialog.dismiss();
                        break;
                    default:
                        VideoDisplayView.this.RepeatDialog.dismiss();
                        break;
                }
                VideoDisplayView.this.mMediaPlayer.start();
            }
        }).setTitle(R.string.selection_repeatmode_title).show();
        Message msg = this.mHandler.obtainMessage(1);
        this.mHandler.sendMessageDelayed(msg, 8000L);
    }

    public void StoreRepeatMode(int mode) {
        SharedPreferences settings = this.mContext.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("RepeatMode", mode);
        editor.commit();
    }

    public int GetRepeatMode() {
        SharedPreferences settings = this.mContext.getSharedPreferences(PREFS_NAME, 0);
        int mode = settings.getInt("RepeatMode", 0);
        return mode;
    }

    private ArrayList addChoices() {
        Resources r = getContext().getResources();
        String str1 = r.getString(R.string.selection_repeat_sigle);
        String str2 = r.getString(R.string.selection_repeat_repeatone);
        String str3 = r.getString(R.string.selection_repeat_repeatall);
        String str4 = r.getString(R.string.alert_close);
        CharSequence[] items = {str1, str2, str3, str4};
        this.choiceItems = new ArrayList<ChoiceItem>();
        for (int i = 0; i < 4; i++) {
            ChoiceItem item = new ChoiceItem();
            item.content = (String) items[i];
            this.choiceItems.add(item);
        }
        return this.choiceItems;
    }

    /* loaded from: classes.dex */
    class ChoiceListAdapter extends ArrayAdapter {
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

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public int getRepeatMode() {
        return this.mRepeatMode;
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void SetScreenSizeClickMode(boolean mode) {
        this.mSetScreebSizeClicked = mode;
    }

    public void CheckAndsetBrihtness(int brightness) {
        if (this.mVideoController != null && this.mVideoController.mScreenBrightButton != null) {
            LOG("VideDiaplayView : mVideoController /  mVideoController.mScreenBrightButton = " + this.mVideoController + "/" + this.mVideoController.mScreenBrightButton);
            if (this.mBrightness > 0 && this.mBrightness <= 78) {
                this.mVideoController.mScreenBrightButton.setImageResource(R.drawable.video_bright1);
            } else if (78 < this.mBrightness && this.mBrightness <= 102) {
                this.mVideoController.mScreenBrightButton.setImageResource(R.drawable.video_bright2);
            } else if (102 < this.mBrightness && this.mBrightness <= 138) {
                this.mVideoController.mScreenBrightButton.setImageResource(R.drawable.video_bright3);
            } else if (138 < this.mBrightness && this.mBrightness <= 188) {
                this.mVideoController.mScreenBrightButton.setImageResource(R.drawable.video_bright4);
            } else if (188 < this.mBrightness && this.mBrightness <= 255) {
                this.mVideoController.mScreenBrightButton.setImageResource(R.drawable.video_bright5);
            }
        }
    }

    public void hideMediaControls() {
        if (this.mVideoController.isShowing()) {
            this.mVideoController.hide();
        }
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public int getBrightMode() {
        return this.mActivity.mBrightMode;
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public int getVolumeMode() {
        return this.mActivity.mVolumeMode;
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void DisplayHdmi() {
        this.mActivity.DisplayHdmi();
    }

    @Override // android.rk.RockVideoPlayer.VideoController.VideoPlayerControl
    public void DisplayTvout() {
        this.mActivity.DisplayTvout();
    }
}
