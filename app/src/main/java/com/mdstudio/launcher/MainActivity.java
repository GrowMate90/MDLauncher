package com.mdstudio.launcher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private FrameLayout root;
    // Approved concept was extended to 1440x3120, centered at y=280 with a 1440x2560 concept area.
    private static final float CONCEPT_TOP = 280f / 3120f;
    private static final float CONCEPT_H = 2560f / 3120f;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        hideSystemUi();
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        ImageView bg = new ImageView(this);
        bg.setImageResource(com.mdstudio.launcher.R.drawable.home_concept);
        bg.setScaleType(ImageView.ScaleType.FIT_XY);
        root.addView(bg, new FrameLayout.LayoutParams(-1,-1));

        // Coordinates refer to the original 941x1672 concept, converted to normalized positions.
        addHotspot("Famiglia",    0.050f, 0.216f, 0.435f, 0.455f);
        addHotspot("Trading",     0.505f, 0.216f, 0.935f, 0.455f);
        addHotspot("Crescita",    0.050f, 0.468f, 0.435f, 0.708f);
        addHotspot("Lavoro",      0.505f, 0.468f, 0.935f, 0.708f);
        addHotspot("Tempo Libero",0.050f, 0.718f, 0.435f, 0.958f);
        addHotspot("Utilità",     0.505f, 0.718f, 0.935f, 0.958f);
        addLiaHotspot(0.045f, 0.965f, 0.945f, 1.075f);
        setContentView(root);
    }

    private void addHotspot(String category, float x1, float y1, float x2, float y2) {
        View v = new View(this);
        v.setBackgroundColor(Color.TRANSPARENT);
        v.setOnClickListener(view -> {
            Intent i = new Intent(this, CategoryActivity.class);
            i.putExtra("category", category);
            startActivity(i);
        });
        root.post(() -> place(v, x1,y1,x2,y2));
        root.addView(v);
    }

    private void addLiaHotspot(float x1,float y1,float x2,float y2) {
        View v = new View(this);
        v.setBackgroundColor(Color.TRANSPARENT);
        v.setOnClickListener(view -> openChatGPT());
        root.post(() -> place(v, x1,y1,x2,y2));
        root.addView(v);
    }

    private void place(View v, float x1, float y1c, float x2, float y2c) {
        int W=root.getWidth(), H=root.getHeight();
        float y1 = CONCEPT_TOP + y1c * CONCEPT_H;
        float y2 = CONCEPT_TOP + y2c * CONCEPT_H;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                Math.max(1,(int)((x2-x1)*W)), Math.max(1,(int)((y2-y1)*H)));
        lp.leftMargin=(int)(x1*W); lp.topMargin=(int)(y1*H);
        v.setLayoutParams(lp);
    }

    private void openChatGPT() {
        Intent launch = getPackageManager().getLaunchIntentForPackage("com.openai.chatgpt");
        if (launch != null) { startActivity(launch); return; }
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://chatgpt.com/"))); }
        catch (Exception e) { Toast.makeText(this,"ChatGPT non trovato",Toast.LENGTH_SHORT).show(); }
    }

    private void hideSystemUi() {
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController c=getWindow().getInsetsController();
            if(c!=null) c.hide(WindowInsets.Type.statusBars()|WindowInsets.Type.navigationBars());
        } else {
            getWindow().getDecorView().setSystemUiVisibility(5894);
        }
    }

    @Override public void onBackPressed() { /* launcher home: remain here */ }
}
