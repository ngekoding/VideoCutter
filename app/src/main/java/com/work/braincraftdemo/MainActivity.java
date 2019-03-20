package com.work.braincraftdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.work.braincraftdemo.seekbar_frame.BackgroundTask;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ImageView iconBack, iconTick, iconPlay, iconCamera, imgStartHere, imgEndHere;

    TextView tvSimple, tvAdvanced, iconTrim, iconCut;

    VideoView videoView;

    RecyclerView recyclerView;
    FrameViewAdapter adapter;

    RangeSeekBar rangeSikbar;

    boolean isPlaying = false;

    int duration = 0;

    boolean trimVideoSelected = true;
    boolean cutVideoSelected = false;

    File dest;

    String[] command;

    String original_path;

    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        prepareVideoView();


    }


    private void initView() {
        iconBack = findViewById(R.id.iconBack);
        iconTick = findViewById(R.id.iconTick);
        iconPlay = findViewById(R.id.iconPlay);
        iconCamera = findViewById(R.id.iconCamera);
        iconTrim = findViewById(R.id.iconTrim);
        iconCut = findViewById(R.id.iconCut);
        tvSimple = findViewById(R.id.tvSimple);
        tvAdvanced = findViewById(R.id.tvAdvanced);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        imgStartHere = findViewById(R.id.imgStartHere);
        imgEndHere = findViewById(R.id.imgEndHere);

        initRecyclerView();

        iconBack.setOnClickListener(clickListener);
        iconTick.setOnClickListener(clickListener);
        iconPlay.setOnClickListener(clickListener);
        iconCamera.setOnClickListener(clickListener);
        iconTrim.setOnClickListener(clickListener);
        iconCut.setOnClickListener(clickListener);
        tvSimple.setOnClickListener(clickListener);
        tvAdvanced.setOnClickListener(clickListener);
        imgStartHere.setOnClickListener(clickListener);
        imgEndHere.setOnClickListener(clickListener);


        videoView = findViewById(R.id.videoView);

        rangeSikbar = findViewById(R.id.rangeSikbar);
    }

    private int mCenterPivot;
    int frameCurrentCenterPosition = 0;

    private void initRecyclerView() {

        adapter = new FrameViewAdapter(MainActivity.this, new FrameViewAdapter.OnRecyclerViewItemActionListener() {
            @Override
            public void onRecyclerViewActionClick(int position) {

            }
        });
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (mCenterPivot == 0) {

                    // Default pivot , Its a bit inaccurate .
                    // Better pass the center pivot as your Center Indicator view's
                    // calculated center on it OnGlobalLayoutListener event
                    mCenterPivot = lm.getOrientation() == LinearLayoutManager.HORIZONTAL ? (recyclerView.getLeft() + recyclerView.getRight()) : (recyclerView.getTop() + recyclerView.getBottom());
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //ScrollStoppped

                    View view = findCenterView(lm);//get the view nearest to center
                    //view.setBackgroundColor(Color.RED);

                    int position = recyclerView.getChildAdapterPosition(view) % thumbnailList.size();
                    Log.d("isideScroll", position + "");
                    frameCurrentCenterPosition = position;

                    videoView.seekTo(position * 1000);

                }
            }
        });
    }

    private View findCenterView(LinearLayoutManager lm) {

        int minDistance = 0;
        View view = null;
        View returnView = null;
        boolean notFound = true;

        for (int i = lm.findFirstVisibleItemPosition(); i <= lm.findLastVisibleItemPosition() && notFound; i++) {

            view = lm.findViewByPosition(i);

            int center = lm.getOrientation() == LinearLayoutManager.HORIZONTAL ? (view.getLeft() + view.getRight()) / 2 : (view.getTop() + view.getBottom()) / 2;
            int leastDifference = Math.abs(mCenterPivot - center);

            if (leastDifference <= minDistance || i == lm.findFirstVisibleItemPosition()) {
                minDistance = leastDifference;
                returnView = view;
            } else {
                notFound = false;

            }
        }
        return returnView;
    }


    @Override
    protected void onResume() {
        super.onResume();


    }

    private void prepareVideoView() {
        String path = "android.resource://" + getPackageName() + "/" + R.raw.sample_video;
        videoView.setVideoURI(Uri.parse(path));
        videoView.setZOrderOnTop(true);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

//                videoView.start();

                duration = mp.getDuration() / 1000;

                getBitmap();
                mp.setLooping(true);

                rangeSikbar.setRangeValues(0, duration);
                rangeSikbar.setSelectedMaxValue(duration);
                rangeSikbar.setSelectedMinValue(0);

                rangeSikbar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {

                        videoView.seekTo((int) minValue * 1000);

                    }

                });


                final Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (videoView.getCurrentPosition() >= rangeSikbar.getSelectedMaxValue().intValue() * 1000) {
                            videoView.seekTo(rangeSikbar.getSelectedMinValue().intValue() * 1000);
                        }

                    }
                }, 1000);


            }
        });
    }


//    private void setSeekBarPosition() {
//
//        if (duration >= mMaxDuration) {
//            mStartPosition = 0;
//            mEndPosition = mMaxDuration;
//
//            mCustomRangeSeekBarNew.setThumbValue(0, (mStartPosition * 100) / mDuration);
//            mCustomRangeSeekBarNew.setThumbValue(1, (mEndPosition * 100) / mDuration);
//
//        } else {
//            mStartPosition = 0;
//            mEndPosition = mDuration;
//        }
//
//
//        mTimeVideo = mDuration;
//        mCustomRangeSeekBarNew.initMaxWidth();
//        seekBarVideo.setMax(mMaxDuration * 1000);
//        videoView.seekTo(mStartPosition * 1000);
//
//    }


    private String getTime(int seconds) {
        int hr = seconds / 3600;
        int rem = seconds & 3600;
        int mn = rem / 60;
        int sec = rem % 60;


        return String.format("%02d", hr) + ":" + String.format("%02d", mn) + ":" + String.format("%02d", sec);

    }


    private void startStopVideo() {

        if (!isPlaying) {
            isPlaying = true;
            videoView.start();
            iconPlay.setImageResource(R.drawable.icon_pause);
        } else {
            isPlaying = false;
            videoView.pause();
            iconPlay.setImageResource(R.drawable.icon_play);
        }


    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.iconBack:
                    performBackAction();
                    break;
                case R.id.iconTick:
                    performTickAction();
                    break;
                case R.id.iconPlay:
                    performPlayAction();
                    break;
                case R.id.iconCamera:

                    break;
                case R.id.iconTrim:
                    performTrimAction();
                    break;
                case R.id.iconCut:
                    performCutAction();
                    break;

                case R.id.tvSimple:
                    break;

                case R.id.tvAdvanced:
                    break;

                case R.id.imgStartHere:
                    performStartHereAction();
                    break;

                case R.id.imgEndHere:
                    performEndHereAction();
                    break;
            }
        }
    };


    int startPosition = 0;

    int endPosition = 0;

    private void performStartHereAction() {
        startPosition = frameCurrentCenterPosition;
        imgStartHere.setImageResource(R.drawable.start_here_after_press);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imgStartHere.setImageResource(R.drawable.start_here_normal);
            }
        }, 200);


    }

    private void performEndHereAction() {
        endPosition = frameCurrentCenterPosition;
        imgEndHere.setImageResource(R.drawable.end_here_after_press);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imgEndHere.setImageResource(R.drawable.end_here_normal);
            }
        }, 200);
    }

    private void performBackAction() {
        Log.d("Action-Clicked", "performBackAction");
        finish();
    }


    private void performTickAction() {
        Log.d("Action-Clicked", "performTickAction");

        if (trimVideoSelected) {
//            progressBar.setVisibility(View.VISIBLE);
            Log.d("Action-Clicked", "performTickAction-trimVideoSelected");
            Log.d("startPosition", "" + startPosition);
            Log.d("endPosition", "" + endPosition);
            trimVideo(startPosition * 1000,
                    endPosition * 1000);

//            progressBar.setVisibility(View.GONE);
            Intent intent = new Intent(this, ProgressBarActivity.class);
            intent.putExtra("duration", duration);
            intent.putExtra("command", command);
            intent.putExtra("original_path", dest.getAbsolutePath());
            intent.putExtra("destination", dest.getAbsolutePath());

            startActivity(intent);


        } else if (cutVideoSelected) {
            Log.d("Action-Clicked", "performTickAction-cutVideoSelected");
        }
    }


    private void performPlayAction() {
        Log.d("Action-Clicked", "performPlayAction");
        startStopVideo();
    }


    private void performTrimAction() {
        Log.d("Action-Clicked", "performTrimAction");

        iconTrim.setBackground(getResources().getDrawable(R.drawable.bg_trim_selected));
        iconTrim.setTextColor(getResources().getColor(R.color.colorWhite));
        iconCut.setBackground(getResources().getDrawable(R.drawable.bg_cut_not_selected));
        iconCut.setTextColor(getResources().getColor(R.color.colorButtonSelected));

        trimVideoSelected = true;
        cutVideoSelected = false;

    }


    private void performCutAction() {
        Log.d("Action-Clicked", "performCutAction");

        iconTrim.setBackground(getResources().getDrawable(R.drawable.bg_trim_not_selected));
        iconTrim.setTextColor(getResources().getColor(R.color.colorButtonSelected));
        iconCut.setBackground(getResources().getDrawable(R.drawable.bg_cut_selected));
        iconCut.setTextColor(getResources().getColor(R.color.colorWhite));


        trimVideoSelected = false;
        cutVideoSelected = true;

    }


    String file_name;

    private void trimVideo(int startMs, int endMs) {

        File folder = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        );


        String filePrefix = "VideoEditor_" + System.currentTimeMillis();
        String fileFormat = ".mp4";

        Log.d("trimVideo-path", folder.getAbsolutePath());

        file_name = filePrefix + fileFormat;
        dest = new File(folder, file_name);

        duration = (endMs - startMs) / 1000;


        final InputStream ins = getResources().openRawResource(R.raw.sample_video);
        File fileSampleVideo = createFileFromInputStream(ins);
        original_path = fileSampleVideo.getAbsolutePath();


        Log.d("original_path", original_path);
        Log.d("dest", dest.getAbsolutePath());


        command = new String[]{"-ss", "" + startMs / 1000, "-y", "-i", original_path, "-t", "" + (endMs - startMs) / 1000, "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", dest.getAbsolutePath()};


    }


    private File createFileFromInputStream(InputStream inputStream) {

        try {
            String filePrefix = "VideoEditor_" + System.currentTimeMillis() + ".mp4";
            File f = new File(this.getExternalCacheDir() + File.separator + filePrefix);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        } catch (IOException e) {
            //Logging exception
        }

        return null;
    }

    private void getBitmap() {

        String path = "android.resource://" + getPackageName() + "/" + R.raw.sample_video;
        final Uri mVideoUri = Uri.parse(path);
        final int mHeightView = getResources().getDimensionPixelOffset(R.dimen.margin_100);

        BackgroundTask
                .execute(new BackgroundTask.Task("", 0L, "") {
                             @Override
                             public void execute() {
                                 try {
                                     ArrayList<Bitmap> thumbnailList = new ArrayList<>();

                                     MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                                     mediaMetadataRetriever.setDataSource(MainActivity.this, mVideoUri);

                                     // Retrieve media data
                                     long videoLengthInMs = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000;

                                     // Set thumbnail properties (Thumbs are squares)
                                     final int thumbWidth = mHeightView;
                                     final int thumbHeight = mHeightView;

//                                     int numThumbs = (int) Math.ceil(((float) viewWidth) / thumbWidth);
                                     int numThumbs = duration;

                                     Log.d("videoLengthInMs", "" + videoLengthInMs);
                                     Log.d("numThumbs", "" + numThumbs);

//                                     final long interval = videoLengthInMs / numThumbs;
                                     final long interval = 1000000;

                                     for (int i = 1; i < numThumbs; ++i) {
                                         Log.d("for numThumbs", interval * i + "");
                                         Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_NEXT_SYNC);

                                         try {
                                             bitmap = Bitmap.createScaledBitmap(bitmap, thumbWidth, thumbHeight, false);
                                         } catch (Exception e) {
                                             e.printStackTrace();
                                         }
                                         thumbnailList.add(bitmap);
                                     }

                                     mediaMetadataRetriever.release();
                                     addViewFrameInView(thumbnailList);
                                 } catch (final Throwable e) {
                                     Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                                 }
                             }
                         }
                );
    }

    ArrayList<Bitmap> thumbnailList;

    private void addViewFrameInView(final ArrayList<Bitmap> thumbnailList) {

        Log.d("thumbnailList", thumbnailList.toString());

        this.thumbnailList = thumbnailList;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                adapter.setData(thumbnailList);
            }
        });


    }


}
