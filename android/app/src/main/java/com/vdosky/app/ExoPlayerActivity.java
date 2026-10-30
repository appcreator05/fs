package com.vdosky.app;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

/**
 * Real Native Google ExoPlayer Activity for VDOSky.
 * Delivers hardware-accelerated, high-performance, stutter-free streaming identical to AppCreator24.
 */
public class ExoPlayerActivity extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView playerView;
    private String videoUrl;
    private String videoTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep device screen active during playback
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        playerView.setControllerShowTimeoutMs(3500);
        playerView.setControllerHideOnTouch(true);
        root.addView(playerView);

        // Top bar overlay (Back button + Video Title)
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
        titleParams.topMargin = 20;
        titleTv.setLayoutParams(titleParams);
        topBar.addView(titleTv);

        root.addView(topBar);
        setContentView(root);

        initExoPlayer();
    }

    private void initExoPlayer() {
        try {
            // Buffer optimization for smooth streaming (like AppCreator24)
            DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    15_000, // Min buffer 15s
                    60_000, // Max buffer 60s
                    1_500,  // Buffer for playback 1.5s
                    3_000   // Buffer for playback after rebuffer 3s
                )
                .build();

            player = new ExoPlayer.Builder(this)
                .setLoadControl(loadControl)
                .build();

            playerView.setPlayer(player);

            Uri uri = Uri.parse(videoUrl.trim());
            MediaItem mediaItem = MediaItem.fromUri(uri);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(true);

            player.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(PlaybackException error) {
                    Toast.makeText(ExoPlayerActivity.this, "Stream error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
