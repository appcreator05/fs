package com.vdosky.app;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

/**
 * Real Native Google ExoPlayer Activity for VDOSky.
 * Engineered for hardware acceleration, software decoder fallback on bad encoding,
 * instant streaming buffer control, and cross-protocol redirect support identical to AppCreator24.
 */
@androidx.annotation.OptIn(markerClass = UnstableApi.class)
public class ExoPlayerActivity extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView playerView;
    private ProgressBar bufferingSpinner;
    private TextView resizeModeBtn;
    private String videoUrl;
    private String videoTitle;
    private int currentResizeModeIndex = 0;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    private static final int[] RESIZE_MODES = {
        AspectRatioFrameLayout.RESIZE_MODE_FIT,
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
        AspectRatioFrameLayout.RESIZE_MODE_FILL
    };
    private static final String[] RESIZE_LABELS = { "FIT", "ZOOM", "STRETCH" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep device screen active during playback
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // True edge-to-edge immersive full-screen
        hideSystemBars();

        videoUrl = getIntent().getStringExtra("VIDEO_URL");
        videoTitle = getIntent().getStringExtra("VIDEO_TITLE");
        if (videoTitle == null || videoTitle.trim().isEmpty()) {
            videoTitle = "VDOSky Stream";
        }

        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            Toast.makeText(this, "No video URL provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Root container
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        root.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // Native Google ExoPlayer PlayerView
        playerView = new PlayerView(this);
        playerView.setLayoutParams(new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        playerView.setBackgroundColor(Color.BLACK);
        playerView.setResizeMode(RESIZE_MODES[currentResizeModeIndex]);
        playerView.setControllerShowTimeoutMs(3500);
        playerView.setControllerHideOnTouch(true);
        root.addView(playerView);

        // Centered Buffering Progress Spinner
        bufferingSpinner = new ProgressBar(this);
        bufferingSpinner.setIndeterminate(true);
        FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        spinnerParams.gravity = Gravity.CENTER;
        bufferingSpinner.setLayoutParams(spinnerParams);
        bufferingSpinner.setVisibility(View.VISIBLE);
        root.addView(bufferingSpinner);

        // Top bar overlay (Back button + Video Title + Aspect Ratio Toggle)
        FrameLayout topBar = new FrameLayout(this);
        FrameLayout.LayoutParams topBarParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        topBar.setLayoutParams(topBarParams);
        topBar.setPadding(32, 32, 32, 32);

        // Back button
        ImageButton backBtn = new ImageButton(this);
        backBtn.setBackgroundColor(Color.parseColor("#66000000"));
        backBtn.setImageResource(android.R.drawable.ic_menu_revert);
        backBtn.setColorFilter(Color.WHITE);
        backBtn.setPadding(16, 16, 16, 16);
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(110, 110);
        backBtn.setLayoutParams(backParams);
        backBtn.setOnClickListener(v -> finish());
        topBar.addView(backBtn);

        // Title text
        TextView titleTv = new TextView(this);
        titleTv.setText(videoTitle);
        titleTv.setTextColor(Color.WHITE);
        titleTv.setTextSize(16);
        titleTv.setTypeface(null, android.graphics.Typeface.BOLD);
        titleTv.setShadowLayer(4, 2, 2, Color.BLACK);
        FrameLayout.LayoutParams titleParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.leftMargin = 130;
        titleParams.rightMargin = 150;
        titleParams.topMargin = 20;
        titleTv.setLayoutParams(titleParams);
        topBar.addView(titleTv);

        // Aspect ratio (Fit / Zoom / Stretch) toggle button on the right
        resizeModeBtn = new TextView(this);
        resizeModeBtn.setText(RESIZE_LABELS[currentResizeModeIndex]);
        resizeModeBtn.setTextColor(Color.parseColor("#00d2ff"));
        resizeModeBtn.setTextSize(13);
        resizeModeBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        resizeModeBtn.setBackgroundColor(Color.parseColor("#66000000"));
        resizeModeBtn.setPadding(24, 14, 24, 14);
        FrameLayout.LayoutParams resizeParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        resizeParams.gravity = Gravity.END | Gravity.TOP;
        resizeParams.topMargin = 10;
        resizeParams.rightMargin = 20;
        resizeModeBtn.setLayoutParams(resizeParams);
        resizeModeBtn.setOnClickListener(v -> cycleResizeMode());
        topBar.addView(resizeModeBtn);

        root.addView(topBar);
        setContentView(root);

        initExoPlayer();
    }

    private void cycleResizeMode() {
        currentResizeModeIndex = (currentResizeModeIndex + 1) % RESIZE_MODES.length;
        if (playerView != null) {
            playerView.setResizeMode(RESIZE_MODES[currentResizeModeIndex]);
        }
        if (resizeModeBtn != null) {
            resizeModeBtn.setText(RESIZE_LABELS[currentResizeModeIndex]);
        }
    }

    private void initExoPlayer() {
        try {
            // 1. Decoder Fallback & Software Decoder preference if hardware decoder fails on video encoding (AppCreator24 grade)
            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                .setEnableDecoderFallback(true);

            // 2. Lenient Track Selection allowing playback even if video constraints/resolution exceed official limits
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(
                trackSelector.buildUponParameters()
                    .setExceedRendererCapabilitiesIfNecessary(true)
                    .setExceedVideoConstraintsIfNecessary(true)
                    .setAllowVideoMixedMimeTypeAdaptiveness(true)
                    .setAllowVideoNonSeamlessAdaptiveness(true)
            );

            // 3. Ultra-responsive LoadControl (instant buffer start like AppCreator24, prevents stuck playback)
            DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    2_500,  // Min buffer 2.5s (instant start)
                    30_000, // Max buffer 30s
                    1_000,  // Buffer for playback 1.0s
                    2_000   // Buffer for playback after rebuffer 2.0s
                )
                .setPrioritizeTimeOverSizeThresholds(true)
                .setBackBuffer(10_000, true)
                .build();

            // 4. Custom HTTP DataSource with Cross-Protocol Redirects & Standard User Agent
            DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36")
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15_000)
                .setReadTimeoutMs(20_000);

            DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this, httpDataSourceFactory);
            DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(dataSourceFactory);

            // 5. Build ExoPlayer
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();

            player = new ExoPlayer.Builder(this, renderersFactory)
                .setMediaSourceFactory(mediaSourceFactory)
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .setAudioAttributes(audioAttributes, true)
                .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                .build();

            player.setHandleAudioBecomingNoisy(true);
            player.setWakeMode(C.WAKE_MODE_NETWORK);

            playerView.setPlayer(player);

            Uri uri = Uri.parse(videoUrl.trim());
            MediaItem mediaItem = MediaItem.fromUri(uri);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(true);

            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_BUFFERING) {
                        if (bufferingSpinner != null) bufferingSpinner.setVisibility(View.VISIBLE);
                    } else if (state == Player.STATE_READY) {
                        if (bufferingSpinner != null) bufferingSpinner.setVisibility(View.GONE);
                        retryCount = 0;
                    } else if (state == Player.STATE_ENDED) {
                        if (bufferingSpinner != null) bufferingSpinner.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    if (bufferingSpinner != null) bufferingSpinner.setVisibility(View.GONE);
                    // Automatic recovery for encoding/decoder errors or network timeouts
                    if (retryCount < MAX_RETRIES && player != null) {
                        retryCount++;
                        long currentPos = Math.max(0, player.getCurrentPosition());
                        Toast.makeText(ExoPlayerActivity.this, "Re-syncing stream...", Toast.LENGTH_SHORT).show();
                        player.prepare();
                        player.seekTo(currentPos);
                        player.play();
                    } else {
                        Toast.makeText(ExoPlayerActivity.this, "Stream error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to start ExoPlayer: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void hideSystemBars() {
        try {
            Window window = getWindow();
            WindowCompat.setDecorFitsSystemWindows(window, false);
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
            if (controller != null) {
                controller.hide(WindowInsetsCompat.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
