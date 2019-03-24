package com.work.braincraftdemo;

import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dinuscxj.progressbar.CircleProgressBar;

public class ProgressBarActivity extends AppCompatActivity {


    CircleProgressBar circleProgressBar;
    int duration;
    String[] command;
    String path;

    ServiceConnection serviceConnection;

    FFMpegService ffMpegService;

    Integer res;

    TextView tvCancel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_bar_activity);


        initView();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private void initView() {
        tvCancel = findViewById(R.id.tvCancel);

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        circleProgressBar = findViewById(R.id.circleProgressBar);
        circleProgressBar.setMax(100);

        final Intent i = getIntent();

        if (i != null) {

            duration = i.getIntExtra("duration", 0);
            command = i.getStringArrayExtra("command");
            path = i.getStringExtra("destination");


            final Intent intent = new Intent(this, FFMpegService.class);
            intent.putExtra("duration", String.valueOf(duration));
            intent.putExtra("command", command);
            intent.putExtra("destination", path);
            startService(intent);


            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {

                    FFMpegService.LocalBinder binder = (FFMpegService.LocalBinder) service;

                    ffMpegService = binder.getServiceInstance();

                    ffMpegService.registerClient(getParent());

                    final Observer<Integer> resultObserver = new Observer<Integer>() {
                        @Override
                        public void onChanged(@Nullable Integer integer) {
                            res = integer;

                            if (res < 100) {
                                circleProgressBar.setProgress(res);

                            }

                            if (res == 100) {
                                circleProgressBar.setProgress(res);
                                stopService(intent);

                                Intent intent = new Intent(ProgressBarActivity.this, ShowActionVideo.class);
                                intent.putExtra("path", i.getStringExtra("original_path"));
                                startActivity(intent);

                                finish();

                                Toast.makeText(getApplicationContext(), "Video Trimmed Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };


                    ffMpegService.getPercentage().observe(ProgressBarActivity.this, resultObserver);

                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };


            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        }
    }
}
