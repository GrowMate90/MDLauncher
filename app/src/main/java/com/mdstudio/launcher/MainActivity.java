package com.mdstudio.launcher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private FrameLayout root;
    private static final float CONCEPT_TOP = 280f / 3120f;
    private static final float CONCEPT_H = 2560f / 3120f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        ImageView background = new ImageView(this);
        background.setImageResource(R.drawable.home_concept);
        background.setScaleType(ImageView.ScaleType.FIT_XY);
        root.addView(background, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        setContentView(root);

        // Create click areas only after the root has a real size.
        root.post(() -> {
            addHotspot("Famiglia",     0.050f, 0.216f, 0.435f, 0.455f);
            addHotspot("Trading",      0.505f, 0.216f, 0.935f, 0.455f);
            addHotspot("Crescita",     0.050f, 0.468f, 0.435f, 0.708f);
            addHotspot("Lavoro",       0.505f, 0.468f, 0.935f, 0.708f);
            addHotspot("Tempo Libero", 0.050f, 0.718f, 0.435f, 0.958f);
            addHotspot("Utilità",      0.505f, 0.718f, 0.935f, 0.958f);
            addLiaHotspot(0.045f, 0.965f, 0.945f, 1.075f);
        });
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
            if (launch != null) {
                startActivity(launch);
                return;
            }
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://chatgpt.com/")));
        } catch (Exception e) {
            Toast.makeText(this, "Impossibile aprire ChatGPT", Toast.LENGTH_SHORT).show();
        }
    }
}
