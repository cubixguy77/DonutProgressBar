package com.robsessions.donutprogressbar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        final DonutProgress donutProgress = findViewById(R.id.donut_progress);
        final Button startButton = findViewById(R.id.start);
        final Button incrementButton = findViewById(R.id.increment);
        final Button decrementButton = findViewById(R.id.decrement);
        final Button collapseButton = findViewById(R.id.collapse);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                donutProgress.startUp();
            }
        });

        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                donutProgress.setProgress(donutProgress.getProgress() + 1);
            }
        });

        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                donutProgress.setProgress(1);
            }
        });

        collapseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                donutProgress.collapse();
            }
        });
    }
}
