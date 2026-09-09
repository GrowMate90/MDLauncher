package com.mdstudio.launcher;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CategoryActivity extends Activity {
    private String category;
    private String displayName;
    private SharedPreferences prefs;
    private GridLayout grid;
    private boolean allAppsMode;
    private int themeIndex;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        enableImmersive();
        category=getIntent().getStringExtra("category");
        if(category==null) category="Sezione";
        allAppsMode=getIntent().getBooleanExtra("allApps",false);
        prefs=getSharedPreferences("mdlauncher",MODE_PRIVATE);
        displayName=prefs.getString("name_"+category,category);
        themeIndex=prefs.getInt("theme_"+category,0);
        build();
    }

    @Override protected void onResume(){ super.onResume(); enableImmersive(); }

    private void build() {
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14),dp(16),dp(14),dp(10));
        root.setBackground(makePageBackground());

        LinearLayout header=new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=new TextView(this);
        title.setText(allAppsMode?"Tutte le app":displayName);
        title.setTextColor(Color.WHITE); title.setTextSize(29); title.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        header.addView(title,new LinearLayout.LayoutParams(0,dp(62),1));

        if(!allAppsMode){
            TextView edit=new TextView(this); edit.setText("✦"); edit.setTextColor(Color.rgb(80,235,245)); edit.setTextSize(26);
            edit.setGravity(Gravity.CENTER); edit.setOnClickListener(v->openCustomizeDialog());
            header.addView(edit,new LinearLayout.LayoutParams(dp(54),dp(54)));
        }
        root.addView(header,new LinearLayout.LayoutParams(-1,dp(66)));

        TextView hint=new TextView(this);
        hint.setText(allAppsMode ? "Tutte le app installate" : "Tocca + per aggiungere un'app • pressione lunga per cambiarla");
        hint.setTextColor(Color.rgb(180,210,222)); hint.setTextSize(13); hint.setGravity(Gravity.CENTER);
        root.addView(hint,new LinearLayout.LayoutParams(-1,dp(38)));

        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true);
        grid=new GridLayout(this); grid.setColumnCount(3); grid.setPadding(0,dp(4),0,dp(18));
        scroll.addView(grid,new ScrollView.LayoutParams(-1,-2));
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        if(allAppsMode) populateAllApps(); else refreshSlots();

        root.addView(makeBottomNavigation(),new LinearLayout.LayoutParams(-1,dp(62)));
        setContentView(root);
    }

    private Drawable makePageBackground(){
        int[][] themes={
                {Color.rgb(5,15,25),Color.rgb(16,24,42)},
                {Color.rgb(10,20,18),Color.rgb(18,45,35)},
                {Color.rgb(24,12,30),Color.rgb(48,20,42)},
                {Color.rgb(15,18,28),Color.rgb(35,38,50)}
        };
        int[] c=themes[Math.max(0,Math.min(themeIndex,themes.length-1))];
        return new GradientDrawable(GradientDrawable.Orientation.TL_BR,c);
    }

    private LinearLayout makeBottomNavigation(){
        LinearLayout bar=new LinearLayout(this); bar.setOrientation(LinearLayout.HORIZONTAL); bar.setGravity(Gravity.CENTER);
        GradientDrawable bg=new GradientDrawable(); bg.setColor(Color.argb(190,5,15,25)); bg.setCornerRadius(dp(22));
        bg.setStroke(dp(1),Color.argb(150,45,220,235)); bar.setBackground(bg);
        TextView menu=navButton("≡"), home=navButton("○"), back=navButton("‹");
        menu.setOnClickListener(v->{ if(!allAppsMode){ Intent i=new Intent(this,CategoryActivity.class); i.putExtra("category","Tutte le app"); i.putExtra("allApps",true); startActivity(i);} });
        home.setOnClickListener(v->{ Intent i=new Intent(this,MainActivity.class); i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP); startActivity(i); finish(); });
        back.setOnClickListener(v->finish());
        bar.addView(menu,new LinearLayout.LayoutParams(0,-1,1));
        bar.addView(home,new LinearLayout.LayoutParams(0,-1,1));
        bar.addView(back,new LinearLayout.LayoutParams(0,-1,1));
        return bar;
    }

    private TextView navButton(String text){
        TextView v=new TextView(this); v.setText(text); v.setTextColor(Color.WHITE); v.setTextSize(31); v.setGravity(Gravity.CENTER); return v;
    }

    private void openCustomizeDialog(){
        Dialog d=new Dialog(this);
        LinearLayout shell=new LinearLayout(this); shell.setOrientation(LinearLayout.VERTICAL); shell.setPadding(dp(18),dp(18),dp(18),dp(16));
        shell.setBackgroundColor(Color.rgb(6,16,26));
        TextView h=new TextView(this); h.setText("Personalizza questo mondo"); h.setTextColor(Color.WHITE); h.setTextSize(23); h.setGravity(Gravity.CENTER);
        shell.addView(h,new LinearLayout.LayoutParams(-1,dp(58)));
        EditText name=new EditText(this); name.setText(displayName); name.setHint("Nome"); name.setTextColor(Color.WHITE); name.setHintTextColor(Color.GRAY);
        shell.addView(name,new LinearLayout.LayoutParams(-1,dp(58)));
        TextView themes=new TextView(this); themes.setText("Tema: tocca per cambiare"); themes.setTextColor(Color.rgb(70,230,240)); themes.setTextSize(17); themes.setGravity(Gravity.CENTER);
        themes.setOnClickListener(v->{ themeIndex=(themeIndex+1)%4; prefs.edit().putInt("theme_"+category,themeIndex).apply(); Toast.makeText(this,"Tema cambiato",Toast.LENGTH_SHORT).show(); });
        shell.addView(themes,new LinearLayout.LayoutParams(-1,dp(54)));
        TextView save=new TextView(this); save.setText("SALVA"); save.setTextColor(Color.WHITE); save.setTextSize(18); save.setGravity(Gravity.CENTER);
        save.setBackground(tileBackground()); save.setOnClickListener(v->{ String n=name.getText().toString().trim(); if(!n.isEmpty()) prefs.edit().putString("name_"+category,n).apply(); d.dismiss(); recreate(); });
        LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,dp(54)); slp.topMargin=dp(10); shell.addView(save,slp);
        d.setContentView(shell); d.show(); if(d.getWindow()!=null){ d.getWindow().setLayout(-1,-2); d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);}    }

    private void populateAllApps(){
        Intent q=new Intent(Intent.ACTION_MAIN,null); q.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps=new ArrayList<>(getPackageManager().queryIntentActivities(q,0));
        Collections.sort(apps, Comparator.comparing(a->a.loadLabel(getPackageManager()).toString(),String.CASE_INSENSITIVE_ORDER));
        for(ResolveInfo r:apps) addAppTile(r,false,-1,null);
    }

    private void refreshSlots() {
        grid.removeAllViews();
        for(int i=0;i<18;i++) addSlot(i);
    }

    private GradientDrawable tileBackground() {
        GradientDrawable gd=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(10,35,50),Color.rgb(26,17,48)});
        gd.setCornerRadius(dp(20)); gd.setStroke(dp(1),Color.rgb(45,220,235));
        return gd;
    }

    private void addSlot(int slot) {
        String key=category+"_"+slot;
        String flat=prefs.getString(key,null);
        if(flat==null){
            LinearLayout tile=baseTile();
            ImageView icon=new ImageView(this);
            TextView label=baseLabel("＋\nAggiungi");
            tile.addView(icon,new LinearLayout.LayoutParams(dp(58),dp(58)));
            LinearLayout.LayoutParams tlp=new LinearLayout.LayoutParams(-1,dp(42)); tlp.topMargin=dp(7); tile.addView(label,tlp);
            tile.setOnClickListener(v->chooseApp(slot)); addTileToGrid(tile); return;
        }
        ComponentName c=ComponentName.unflattenFromString(flat);
        ResolveInfo info=findResolveInfo(c);
        if(info!=null) addAppTile(info,true,slot,flat); else prefs.edit().remove(key).apply();
    }

    private LinearLayout baseTile(){
        LinearLayout tile=new LinearLayout(this); tile.setOrientation(LinearLayout.VERTICAL); tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(8),dp(12),dp(8),dp(8)); tile.setBackground(tileBackground()); return tile;
    }
    private TextView baseLabel(CharSequence t){
        TextView label=new TextView(this); label.setText(t); label.setTextColor(Color.WHITE); label.setTextSize(13); label.setGravity(Gravity.CENTER); label.setMaxLines(2); return label;
    }
    private void addTileToGrid(View tile){
        GridLayout.LayoutParams lp=new GridLayout.LayoutParams(); lp.width=0; lp.height=dp(128); lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
        lp.setMargins(dp(5),dp(5),dp(5),dp(5)); grid.addView(tile,lp);
    }

    private void addAppTile(ResolveInfo info, boolean editable, int slot, String flat){
        LinearLayout tile=baseTile(); ImageView icon=new ImageView(this); icon.setImageDrawable(info.loadIcon(getPackageManager()));
        TextView label=baseLabel(info.loadLabel(getPackageManager()));
        tile.addView(icon,new LinearLayout.LayoutParams(dp(58),dp(58))); LinearLayout.LayoutParams tlp=new LinearLayout.LayoutParams(-1,dp(42)); tlp.topMargin=dp(7); tile.addView(label,tlp);
        ComponentName c=new ComponentName(info.activityInfo.packageName,info.activityInfo.name);
        tile.setOnClickListener(v->launch(c.flattenToString()));
        if(editable) tile.setOnLongClickListener(v->{chooseApp(slot); return true;});
        addTileToGrid(tile);
    }

    private ResolveInfo findResolveInfo(ComponentName c) {
        if(c==null) return null;
        Intent q=new Intent(Intent.ACTION_MAIN); q.addCategory(Intent.CATEGORY_LAUNCHER); q.setComponent(c);
        List<ResolveInfo> list=getPackageManager().queryIntentActivities(q,0);
        return list.isEmpty()?null:list.get(0);
    }

    private void chooseApp(int slot) {
        Intent q=new Intent(Intent.ACTION_MAIN,null); q.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps=new ArrayList<>(getPackageManager().queryIntentActivities(q,0));
        Collections.sort(apps, Comparator.comparing(a->a.loadLabel(getPackageManager()).toString(),String.CASE_INSENSITIVE_ORDER));

        final Dialog dialog=new Dialog(this);
        LinearLayout shell=new LinearLayout(this); shell.setOrientation(LinearLayout.VERTICAL);
        shell.setPadding(dp(16),dp(18),dp(16),dp(12)); shell.setBackgroundColor(Color.rgb(5,15,25));
        TextView heading=new TextView(this); heading.setText("Scegli un'app"); heading.setTextColor(Color.WHITE);
        heading.setTextSize(25); heading.setGravity(Gravity.CENTER); shell.addView(heading,new LinearLayout.LayoutParams(-1,dp(58)));
        ScrollView scroll=new ScrollView(this); GridLayout appGrid=new GridLayout(this); appGrid.setColumnCount(3);
        for(ResolveInfo r:apps){
            LinearLayout item=baseTile();
            ImageView iv=new ImageView(this); iv.setImageDrawable(r.loadIcon(getPackageManager()));
            TextView tv=baseLabel(r.loadLabel(getPackageManager()));
            item.addView(iv,new LinearLayout.LayoutParams(dp(52),dp(52))); item.addView(tv,new LinearLayout.LayoutParams(-1,dp(38)));
            item.setOnClickListener(v->{ ComponentName c=new ComponentName(r.activityInfo.packageName,r.activityInfo.name);
                prefs.edit().putString(category+"_"+slot,c.flattenToString()).apply(); dialog.dismiss(); refreshSlots(); });
            GridLayout.LayoutParams lp=new GridLayout.LayoutParams(); lp.width=0; lp.height=dp(112);
            lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); lp.setMargins(dp(4),dp(4),dp(4),dp(4)); appGrid.addView(item,lp);
        }
        scroll.addView(appGrid,new ScrollView.LayoutParams(-1,-2)); shell.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        TextView cancel=new TextView(this); cancel.setText("Annulla"); cancel.setTextColor(Color.rgb(45,230,240)); cancel.setTextSize(17);
        cancel.setGravity(Gravity.CENTER); cancel.setOnClickListener(v->dialog.dismiss()); shell.addView(cancel,new LinearLayout.LayoutParams(-1,dp(52)));
        dialog.setContentView(shell); dialog.show(); if(dialog.getWindow()!=null){dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); dialog.getWindow().setLayout(-1,-1);}
    }

    private void launch(String flat) {
        try { ComponentName c=ComponentName.unflattenFromString(flat); Intent i=new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_LAUNCHER); i.setComponent(c); startActivity(i); }
        catch(Exception e){ Toast.makeText(this,"Impossibile aprire l'app",Toast.LENGTH_SHORT).show(); }
    }

    private void enableImmersive(){
        Window window=getWindow();
        if(Build.VERSION.SDK_INT>=30){
            window.setDecorFitsSystemWindows(false); WindowInsetsController c=window.getInsetsController();
            if(c!=null){ c.hide(WindowInsets.Type.statusBars()|WindowInsets.Type.navigationBars()); c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE); }
        } else window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private int dp(int n){ return (int)(n*getResources().getDisplayMetrics().density+0.5f); }
}
