package com.mdstudio.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private FrameLayout root;
    private TextView clockView;
    private TextView dateView;
    private TextView batteryView;
    private final android.os.Handler clockHandler = new android.os.Handler();
    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            updateTopBar();
            clockHandler.postDelayed(this, 30000);
        }
    };

    private static final float CONCEPT_TOP = 280f / 3120f;
    private static final float CONCEPT_H = 2560f / 3120f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableImmersive();

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        ImageView background = new ImageView(this);
        background.setImageResource(R.drawable.home_concept);
        background.setScaleType(ImageView.ScaleType.FIT_XY);
        root.addView(background, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        setContentView(root);

        root.post(() -> {
            addHotspot("Famiglia",     0.050f, 0.216f, 0.435f, 0.455f);
            addHotspot("Trading",      0.505f, 0.216f, 0.935f, 0.455f);
            addHotspot("Crescita",     0.050f, 0.468f, 0.435f, 0.708f);
            addHotspot("Lavoro",       0.505f, 0.468f, 0.935f, 0.708f);
            addHotspot("Tempo Libero", 0.050f, 0.718f, 0.435f, 0.958f);
            addHotspot("Utilità",      0.505f, 0.718f, 0.935f, 0.958f);
            addLiaHotspot(0.045f, 0.965f, 0.945f, 1.075f);
            addLiveTopBar();
            addBottomNavigation();
        });

        clockHandler.post(clockTick);
    }

    @Override protected void onResume() {
        super.onResume();
        enableImmersive();
        updateTopBar();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        clockHandler.removeCallbacks(clockTick);
    }

    private void addLiveTopBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(18),0,dp(18),0);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.argb(120,0,0,0));
        bg.setCornerRadius(dp(18));
        bar.setBackground(bg);

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        clockView = new TextView(this);
        clockView.setTextColor(Color.WHITE); clockView.setTextSize(23); clockView.setGravity(Gravity.START);
        dateView = new TextView(this);
        dateView.setTextColor(Color.rgb(195,220,230)); dateView.setTextSize(12); dateView.setGravity(Gravity.START);
        left.addView(clockView,new LinearLayout.LayoutParams(-2,dp(30)));
        left.addView(dateView,new LinearLayout.LayoutParams(-2,dp(22)));
        bar.addView(left,new LinearLayout.LayoutParams(0,-1,1));

        batteryView = new TextView(this);
        batteryView.setTextColor(Color.WHITE); batteryView.setTextSize(15); batteryView.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
        bar.addView(batteryView,new LinearLayout.LayoutParams(dp(86),-1));

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1,dp(64));
        lp.leftMargin=dp(14); lp.rightMargin=dp(14); lp.topMargin=dp(12);
        root.addView(bar,lp);
        updateTopBar();
    }

    private void updateTopBar() {
        if(clockView==null) return;
        Date now = new Date();
        clockView.setText(new SimpleDateFormat("HH:mm", Locale.ITALIAN).format(now));
        dateView.setText(new SimpleDateFormat("EEE d MMM", Locale.ITALIAN).format(now));
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent status = registerReceiver(null, filter);
            int level = status == null ? -1 : status.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
            batteryView.setText(level >= 0 ? "🔋 " + level + "%" : "");
        } catch(Exception ignored) { batteryView.setText(""); }
    }

    private void addBottomNavigation() {
        LinearLayout bar=new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(dp(18),0,dp(18),0);
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.argb(190,5,15,25)); bg.setCornerRadius(dp(24));
        bg.setStroke(dp(1),Color.argb(150,45,220,235)); bar.setBackground(bg);

        TextView menu=navButton("≡");
        TextView home=navButton("○");
        TextView back=navButton("‹");

        menu.setOnClickListener(v -> openAllApps());
        home.setOnClickListener(v -> { /* already home */ });
        home.setOnLongClickListener(v -> { openHomeSettings(); return true; });
        back.setOnClickListener(v -> { /* launcher home remains open */ });

        bar.addView(menu,new LinearLayout.LayoutParams(0,-1,1));
        bar.addView(home,new LinearLayout.LayoutParams(0,-1,1));
        bar.addView(back,new LinearLayout.LayoutParams(0,-1,1));

        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,dp(62),Gravity.BOTTOM);
        lp.leftMargin=dp(18); lp.rightMargin=dp(18); lp.bottomMargin=dp(10);
        root.addView(bar,lp);
    }

    private TextView navButton(String text) {
        TextView v=new TextView(this); v.setText(text); v.setTextColor(Color.WHITE); v.setTextSize(32);
        v.setGravity(Gravity.CENTER); return v;
    }

    private void openAllApps() {
        Intent i=new Intent(this, CategoryActivity.class);
        i.putExtra("category","Tutte le app");
        i.putExtra("allApps",true);
        startActivity(i);
    }

    private void openHomeSettings() {
        try { startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); }
        catch(Exception e) { Toast.makeText(this,"Apri Impostazioni > App predefinite > App Home",Toast.LENGTH_LONG).show(); }
    }

    private void addHotspot(String category, float x1, float y1c, float x2, float y2c) {
        View hotspot = new View(this);
        hotspot.setBackgroundColor(Color.TRANSPARENT);
        hotspot.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryActivity.class);
            intent.putExtra("category", category);
            startActivity(intent);
        });
        root.addView(hotspot, makeLayoutParams(x1, y1c, x2, y2c));
    }

    private void addLiaHotspot(float x1, float y1c, float x2, float y2c) {
        View hotspot = new View(this);
        hotspot.setBackgroundColor(Color.TRANSPARENT);
        hotspot.setOnClickListener(v -> openChatGPT());
        root.addView(hotspot, makeLayoutParams(x1, y1c, x2, y2c));
    }

    private FrameLayout.LayoutParams makeLayoutParams(float x1, float y1c, float x2, float y2c) {
        int width = Math.max(1, root.getWidth());
        int height = Math.max(1, root.getHeight());
        float y1 = CONCEPT_TOP + y1c * CONCEPT_H;
        float y2 = CONCEPT_TOP + y2c * CONCEPT_H;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                Math.max(1, (int) ((x2 - x1) * width)),
                Math.max(1, (int) ((y2 - y1) * height)));
        lp.leftMargin = (int) (x1 * width);
        lp.topMargin = (int) (y1 * height);
        return lp;
    }

    private void openChatGPT() {
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.openai.chatgpt");
            if (launch != null) { startActivity(launch); return; }
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://chatgpt.com/")));
        } catch (Exception e) {
            Toast.makeText(this, "Impossibile aprire ChatGPT", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableImmersive() {
        Window window=getWindow();
        if(Build.VERSION.SDK_INT>=30) {
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController c=window.getInsetsController();
            if(c!=null) {
                c.hide(WindowInsets.Type.statusBars()|WindowInsets.Type.navigationBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private int dp(int n){ return (int)(n*getResources().getDisplayMetrics().density+0.5f); }
}
