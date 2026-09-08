package com.mdstudio.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
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
        root.setPadding(dp(18),dp(28),dp(18),dp(18));
        root.setBackgroundColor(Color.rgb(7,18,28));

        TextView title=new TextView(this);
        title.setText(category);
        title.setTextColor(Color.WHITE); title.setTextSize(32); title.setGravity(Gravity.CENTER);
        root.addView(title,new LinearLayout.LayoutParams(-1,dp(72)));

        TextView hint=new TextView(this);
        hint.setText("Tocca + per scegliere un'app. Tocca un'app per aprirla.");
        hint.setTextColor(Color.rgb(180,205,220)); hint.setTextSize(15); hint.setGravity(Gravity.CENTER);
        root.addView(hint,new LinearLayout.LayoutParams(-1,dp(54)));

        grid=new GridLayout(this); grid.setColumnCount(2);
        root.addView(grid,new LinearLayout.LayoutParams(-1,0,1));
        refreshSlots();

        Button home=new Button(this); home.setText("← Torna alla Home");
        home.setOnClickListener(v->finish());
        root.addView(home,new LinearLayout.LayoutParams(-1,dp(58)));
        setContentView(root);
    }

    private void refreshSlots() {
        grid.removeAllViews();
        for(int i=0;i<6;i++) addSlot(i);
    }

    private void addSlot(int slot) {
        String key=category+"_"+slot;
        String flat=prefs.getString(key,null);
        Button b=new Button(this);
        b.setAllCaps(false); b.setTextSize(17); b.setTextColor(Color.WHITE);
        GradientDrawable gd=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(12,39,54),Color.rgb(28,18,48)});
        gd.setCornerRadius(dp(22)); gd.setStroke(dp(1),Color.rgb(48,220,235));
        b.setBackground(gd);
        if(flat==null) {
            b.setText("＋\nAggiungi app");
            b.setOnClickListener(v->chooseApp(slot));
        } else {
            ComponentName c=ComponentName.unflattenFromString(flat);
            String label=c==null?"App":c.getPackageName();
            try { label=getPackageManager().getActivityInfo(c,0).loadLabel(getPackageManager()).toString(); } catch(Exception ignored){}
            b.setText(label);
            b.setOnClickListener(v->launch(flat));
            b.setOnLongClickListener(v->{ chooseApp(slot); return true; });
        }
        GridLayout.LayoutParams lp=new GridLayout.LayoutParams();
        lp.width=0; lp.height=dp(150); lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
        lp.setMargins(dp(6),dp(6),dp(6),dp(6));
        grid.addView(b,lp);
    }

    private void chooseApp(int slot) {
        Intent q=new Intent(Intent.ACTION_MAIN,null); q.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps=new ArrayList<>(getPackageManager().queryIntentActivities(q,0));
        Collections.sort(apps, Comparator.comparing(a->a.loadLabel(getPackageManager()).toString(),String.CASE_INSENSITIVE_ORDER));
        String[] labels=new String[apps.size()]; for(int i=0;i<apps.size();i++) labels[i]=apps.get(i).loadLabel(getPackageManager()).toString();
        new AlertDialog.Builder(this).setTitle("Scegli un'app").setItems(labels,(d,which)->{
            ResolveInfo r=apps.get(which);
            ComponentName c=new ComponentName(r.activityInfo.packageName,r.activityInfo.name);
            prefs.edit().putString(category+"_"+slot,c.flattenToString()).apply(); refreshSlots();
        }).setNegativeButton("Annulla",null).show();
    }

    private void launch(String flat) {
        try { ComponentName c=ComponentName.unflattenFromString(flat); Intent i=new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_LAUNCHER); i.setComponent(c); startActivity(i); }
        catch(Exception e){ Toast.makeText(this,"Impossibile aprire l'app",Toast.LENGTH_SHORT).show(); }
    }

    private int dp(int n){ return (int)(n*getResources().getDisplayMetrics().density+0.5f); }
}
