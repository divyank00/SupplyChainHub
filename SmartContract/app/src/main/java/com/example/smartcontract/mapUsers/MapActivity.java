package com.example.smartcontract.mapUsers;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.smartcontract.R;
import com.google.api.Distribution;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {

    ArrayList<String> arr = new ArrayList<>();
    LinearLayout linear,linear1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        linear = findViewById(R.id.linear);
       // linear1 = findViewById(R.id.linear1);

        arr.add("Pune");
        arr.add("Pune2");
        arr.add("Pune3");
        arr.add("Pune4");
        arr.add("Pune5");
        arr.add("Pune6");

        createUI();

    }

    public void createUI(){

        //linear.removeAllViews();
        for(int i=0;i<arr.size();i++){

            LinearLayout linear1 = new LinearLayout(this);
            linear1.setOrientation(LinearLayout.HORIZONTAL);
            linear1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linear1.setGravity(Gravity.CENTER_HORIZONTAL);


            ImageView image = new ImageView(this);
            LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(140,140);
            l.setMargins(150,0,30,10);
            image.setLayoutParams(l);

            int strokeWidth = 5;
            int strokeColor = Color.parseColor("#a200e0");
            int fillColor = Color.parseColor("#b39afd");
            GradientDrawable gD = new GradientDrawable();
            gD.setColor(fillColor);
            gD.setShape(GradientDrawable.OVAL);
            gD.setStroke(strokeWidth, strokeColor);
            image.setBackground(gD);

            linear1.addView(image);

            TextView text = new TextView(this);
            text.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            text.setText(arr.get(i));
            text.setGravity(Gravity.CENTER_VERTICAL);

            linear1.addView(text);

            LinearLayout linear2 = new LinearLayout(this);
            linear2.setOrientation(LinearLayout.VERTICAL);
            linear2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100));
            linear2.setGravity(Gravity.CENTER_HORIZONTAL);

            TextView text1 = new TextView(this);
            text1.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            text1.setGravity(Gravity.CENTER_HORIZONTAL);
            text1.setText(" ");
            text1.setBackgroundColor(Color.BLACK);
            text1.setTextSize(15);
            linear2.addView(text1);

            linear.addView(linear1);
            linear.addView(linear2);
        }

        TextView tex = new TextView(this);
        tex.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tex.setGravity(Gravity.CENTER_HORIZONTAL);
        tex.setText("Customer");
        tex.setTextColor(Color.BLACK);
        tex.setTextSize(20);
        tex.setGravity(Gravity.CENTER);

        linear.addView(tex);

    }
}