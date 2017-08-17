package com.robsessions.donutprogressbar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        final DonutProgress donutProgress = (DonutProgress) findViewById(R.id.donut_progress);

        donutProgress.setProgress(1);
        donutProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (donutProgress.getProgress() == 5) {
                    donutProgress.reset();
                    return;
                }

                donutProgress.setProgress(donutProgress.getProgress() + 1);
            }
        });

    }
}
