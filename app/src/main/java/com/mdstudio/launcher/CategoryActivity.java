package com.mdstudio.launcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
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
    private SharedPreferences prefs;
    private GridLayout grid;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        category=getIntent().getStringExtra("category");
        if(category==null) category="Sezione";
        prefs=getSharedPreferences("mdlauncher",MODE_PRIVATE);
        build();
    }

    private void build() {
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(18),dp(16),dp(12));
        root.setBackgroundColor(Color.rgb(5,15,25));

        TextView title=new TextView(this);
        title.setText(category);
        title.setTextColor(Color.WHITE); title.setTextSize(30); title.setGravity(Gravity.CENTER);
        root.addView(title,new LinearLayout.LayoutParams(-1,dp(62)));

        TextView hint=new TextView(this);
        hint.setText("Tocca + per aggiungere un'app • pressione lunga per cambiarla");
        hint.setTextColor(Color.rgb(170,205,220)); hint.setTextSize(13); hint.setGravity(Gravity.CENTER);
        root.addView(hint,new LinearLayout.LayoutParams(-1,dp(42)));

        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        grid=new GridLayout(this); grid.setColumnCount(3); grid.setPadding(0,dp(4),0,dp(18));
        scroll.addView(grid,new ScrollView.LayoutParams(-1,-2));
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        refreshSlots();

        setContentView(root);
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
        LinearLayout tile=new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL); tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(8),dp(12),dp(8),dp(8)); tile.setBackground(tileBackground());

        ImageView icon=new ImageView(this);
        TextView label=new TextView(this);
        label.setTextColor(Color.WHITE); label.setTextSize(13); label.setGravity(Gravity.CENTER); label.setMaxLines(2);

        if(flat==null) {
            label.setText("＋\nAggiungi");
            tile.setOnClickListener(v->chooseApp(slot));
        } else {
            ComponentName c=ComponentName.unflattenFromString(flat);
            String name="App"; Drawable drawable=null;
            try {
                ResolveInfo info=findResolveInfo(c);
                if(info!=null){ name=info.loadLabel(getPackageManager()).toString(); drawable=info.loadIcon(getPackageManager()); }
            } catch(Exception ignored){}
            if(drawable!=null) icon.setImageDrawable(drawable);
            label.setText(name);
            tile.setOnClickListener(v->launch(flat));
            tile.setOnLongClickListener(v->{chooseApp(slot); return true;});
        }
        tile.addView(icon,new LinearLayout.LayoutParams(dp(58),dp(58)));
        LinearLayout.LayoutParams tlp=new LinearLayout.LayoutParams(-1,dp(42)); tlp.topMargin=dp(7);
        tile.addView(label,tlp);

        GridLayout.LayoutParams lp=new GridLayout.LayoutParams();
        lp.width=0; lp.height=dp(128); lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
        lp.setMargins(dp(5),dp(5),dp(5),dp(5)); grid.addView(tile,lp);
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

        final android.app.Dialog dialog=new android.app.Dialog(this);
        LinearLayout shell=new LinearLayout(this); shell.setOrientation(LinearLayout.VERTICAL);
        shell.setPadding(dp(16),dp(18),dp(16),dp(12)); shell.setBackgroundColor(Color.rgb(5,15,25));
        TextView heading=new TextView(this); heading.setText("Scegli un'app"); heading.setTextColor(Color.WHITE);
        heading.setTextSize(25); heading.setGravity(Gravity.CENTER); shell.addView(heading,new LinearLayout.LayoutParams(-1,dp(58)));
        ScrollView scroll=new ScrollView(this); GridLayout appGrid=new GridLayout(this); appGrid.setColumnCount(3);
        for(ResolveInfo r:apps){
            LinearLayout item=new LinearLayout(this); item.setOrientation(LinearLayout.VERTICAL); item.setGravity(Gravity.CENTER);
            item.setPadding(dp(5),dp(9),dp(5),dp(7)); item.setBackground(tileBackground());
            ImageView iv=new ImageView(this); iv.setImageDrawable(r.loadIcon(getPackageManager()));
            TextView tv=new TextView(this); tv.setText(r.loadLabel(getPackageManager())); tv.setTextColor(Color.WHITE);
            tv.setTextSize(12); tv.setGravity(Gravity.CENTER); tv.setMaxLines(2);
            item.addView(iv,new LinearLayout.LayoutParams(dp(52),dp(52))); item.addView(tv,new LinearLayout.LayoutParams(-1,dp(38)));
            item.setOnClickListener(v->{ ComponentName c=new ComponentName(r.activityInfo.packageName,r.activityInfo.name);
                prefs.edit().putString(category+"_"+slot,c.flattenToString()).apply(); dialog.dismiss(); refreshSlots(); });
            GridLayout.LayoutParams lp=new GridLayout.LayoutParams(); lp.width=0; lp.height=dp(112);
            lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); lp.setMargins(dp(4),dp(4),dp(4),dp(4)); appGrid.addView(item,lp);
        }
        scroll.addView(appGrid,new ScrollView.LayoutParams(-1,-2)); shell.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        TextView cancel=new TextView(this); cancel.setText("Annulla"); cancel.setTextColor(Color.rgb(45,230,240)); cancel.setTextSize(17);
        cancel.setGravity(Gravity.CENTER); cancel.setOnClickListener(v->dialog.dismiss()); shell.addView(cancel,new LinearLayout.LayoutParams(-1,dp(52)));
        dialog.setContentView(shell); if(dialog.getWindow()!=null){dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); dialog.getWindow().setLayout(-1,-1);} dialog.show();
        if(dialog.getWindow()!=null) dialog.getWindow().setLayout(-1,-1);
    }

    private void launch(String flat) {
        try { ComponentName c=ComponentName.unflattenFromString(flat); Intent i=new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_LAUNCHER); i.setComponent(c); startActivity(i); }
        catch(Exception e){ Toast.makeText(this,"Impossibile aprire l'app",Toast.LENGTH_SHORT).show(); }
    }
    private int dp(int n){ return (int)(n*getResources().getDisplayMetrics().density+0.5f); }
}
