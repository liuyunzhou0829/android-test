package com.example.ninegrid;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    final String[] labels={"庄庄","庄闲","闲庄","闲闲","闲庄","庄闲","闲庄","庄闲","闲庄"};
    final int[] bets={1,3,9,27,81,175,450,1000};
    Button[] cells=new Button[9];
    int selected=-1, stage=0, losses=0;
    boolean pause=false;
    double profit=0;
    TextView info,status;

    int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    TextView txt(String s,int size,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.DKGRAY);
        t.setGravity(Gravity.CENTER_VERTICAL);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;
    }
    @Override public void onCreate(Bundle b){super.onCreate(b);build();}

    void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14),dp(10),dp(14),dp(10));root.setBackgroundColor(Color.WHITE);

        TextView title=txt("九格投注记录器",22,true);title.setGravity(Gravity.CENTER);
        root.addView(title,new LinearLayout.LayoutParams(-1,dp(48)));

        GridLayout grid=new GridLayout(this);grid.setColumnCount(3);grid.setRowCount(3);
        for(int i=0;i<9;i++){
            final int n=i;Button b=new Button(this);cells[i]=b;
            b.setText((i+1)+"\n"+labels[i]);b.setTextSize(15);b.setAllCaps(false);b.setPadding(0,0,0,0);
            b.setBackgroundResource(R.drawable.cell_normal);
            b.setOnClickListener(v->{selected=n;refreshCells();update();});
            GridLayout.LayoutParams p=new GridLayout.LayoutParams();
            p.width=0;p.height=dp(82);p.columnSpec=GridLayout.spec(i%3,1f);p.rowSpec=GridLayout.spec(i/3,1f);
            p.setMargins(dp(4),dp(4),dp(4),dp(4));grid.addView(b,p);
        }
        root.addView(grid,new LinearLayout.LayoutParams(-1,dp(270)));

        info=txt("",16,true);status=txt("",15,false);
        root.addView(info,new LinearLayout.LayoutParams(-1,dp(104)));
        root.addView(status,new LinearLayout.LayoutParams(-1,dp(48)));

        LinearLayout row=new LinearLayout(this);
        Button win=button("赢",R.drawable.win),loss=button("输",R.drawable.loss);
        win.setOnClickListener(v->record(true));loss.setOnClickListener(v->record(false));
        row.addView(win,new LinearLayout.LayoutParams(0,dp(58),1));
        row.addView(loss,new LinearLayout.LayoutParams(0,dp(58),1));
        root.addView(row);

        Button reset=button("重新开始",R.drawable.reset);
        reset.setOnClickListener(v->reset());
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(52));rp.topMargin=dp(10);root.addView(reset,rp);

        TextView note=txt("九格只负责记录序号，你自己判断本期输赢。赢：回第1档；输：下一档。连续输4次后暂停1期；暂停期若点“赢”，下一期从第5档（81）开始。",12,false);
        note.setTextColor(Color.GRAY);root.addView(note,new LinearLayout.LayoutParams(-1,dp(68)));

        setContentView(root);update();
    }

    Button button(String s,int bg){
        Button b=new Button(this);b.setText(s);b.setTextSize(18);b.setTextColor(Color.WHITE);b.setAllCaps(false);
        b.setBackgroundResource(bg);return b;
    }
    void refreshCells(){for(int i=0;i<9;i++)cells[i].setBackgroundResource(i==selected?R.drawable.cell_selected:R.drawable.cell_normal);}

    void record(boolean win){
        if(selected<0){Toast.makeText(this,"请先选择一个九格序号",Toast.LENGTH_SHORT).show();return;}

        if(pause){
            // 暂停期间不下注：
            // 输：继续保持暂停；
            // 赢：解除暂停，下一期从第5档开始。
            if(win){
                stage=4;losses=0;pause=false;
                status.setText("暂停期：赢 → 下一期从第5档开始");
            }else{
                pause=true;
                status.setText("暂停期：继续输 → 继续暂停");
            }
            update();return;
        }

        int stake=bets[stage];
        if(win){
            profit += stake;
            stage=0;losses=0;
            status.setText("本期：赢｜下一期第1档");
        }else{
            profit -= stake;
            losses++;
            if(losses>=4){
                pause=true;
                status.setText("本期：输｜连续4输，下一期暂停");
            }else{
                stage=Math.min(stage+1,bets.length-1);
                status.setText("本期：输｜下一期第"+(stage+1)+"档");
            }
        }
        update();
    }

    void update(){
        String sel=selected<0?"未选择":(selected+1)+"号 "+labels[selected];
        String st=pause?"暂停期（本期不下注）":"第"+(stage+1)+"档";
        info.setText("当前选择："+sel+"\n投注状态："+st+"    投注额："+(pause?"0":bets[stage])+
                "\n连续输："+losses+"次    累计净额："+String.format("%.2f",profit));
        if(selected<0)status.setText("请选择一个九格序号");
    }

    void reset(){
        selected=-1;stage=0;losses=0;pause=false;profit=0;refreshCells();update();
        status.setText("已重新开始：第1档");
    }
}