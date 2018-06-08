package com.cvte;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.cvte.widget.gallery.EcoGalleryAdapterView;
import com.cvte.widget.gallery.FancyCoverFlow;
import com.cvte.widget.gallery.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestActivity extends Activity {
    DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImageLoader(getApplicationContext());

        setContentView(R.layout.activity_test);
        init(savedInstanceState);
    }

    public void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you
        // may tune some of them,
        // or you can create default configuration by
        // ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
        ImageLoader.getInstance().clearDiscCache();
        ImageLoader.getInstance().clearMemoryCache();


        // 配置option
        options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.img0)
                .showImageForEmptyUri(R.drawable.img0)
                .showImageOnFail(R.drawable.img0).cacheInMemory(true)
                .cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    private FancyCoverFlow fancyCoverFlow;
    private ImageAdapter adapter;


    private int cur_index = 0;
    private int count_drawble;
    private static int MSG_UPDATE = 1;
    // 定时任务
    private ScheduledExecutorService scheduledExecutorService;
    private int start = 10;
    private int time = 800;
    private int sleep = 80;
    private boolean stop = false;


    private void reset() {
        start = 10;
        time = 1000;
        sleep = 80;
    }
    // 通过handler来更新主界面
    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE) {
                // fancyCoverFlow.setSelection(cur_index, true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                            // fancyCoverFlow.moveToNext(time);

                        //     while (!stop) {
                    //         int rand = new Random().nextInt(14);
                    //         Log.d("CGallery", String.format("rand:%d", rand));
                    //     fancyCoverFlow.moveToNext(start);
                            if (start <= 1000) {
                                start += 10;
                            }
                    //         if (time >= 10) {
                    //             time /= 2;
                    //         }
                    //         if (sleep >= 10) {
                    //             sleep -= 1;
                    //         }
                    //         try {
                    //             Thread.sleep(sleep);
                    //         } catch (InterruptedException e) {
                    //             break;
                    //         }
                    //     }
                    }
                }).start();

            }
        }
    };

    public void init(Bundle savedInstanceState) {
        adapter = new ImageAdapter(this, ListInfo.getfilmInfo(), options, ImageLoader.getInstance());

        fancyCoverFlow = (FancyCoverFlow) findViewById(R.id.fancyCoverFlow);
        // fancyCoverFlow = new FancyCoverFlow(this);
        // LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //
        // this.addView(gallery, parms);
        // this.setBackgroundColor(Color.BLACK);


        // item之间的间隙可以近似认为是imageview的宽度与缩放比例的乘积的一半
        fancyCoverFlow.setSpacing(-80);
        fancyCoverFlow.setAdapter(adapter);

        fancyCoverFlow.setOnMoveListener(new EcoGalleryAdapterView.OnMoveListener() {
            @Override
            public void onMoveFinish(EcoGalleryAdapterView<?> parent, View view, int position, long id) {
                // Message msg = handler.obtainMessage(MSG_UPDATE);
                // handler.sendMessage(msg);
                // fancyCoverFlow.moveToNext(2000, 10);

                Log.d("", "moveFinish");
                // fancyCoverFlow.moveToNext(time);
                //         if (time > 50) {
                //             time /= 1.2;
                //         }
                // fancyCoverFlow.startFling();
            }
        });

        fancyCoverFlow.setOnItemSelectedListener(new EcoGalleryAdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(EcoGalleryAdapterView<?> parent, View view, int position, long id) {
                int len = adapter.getData().size();
                Log.d("Gallery", "1 cur index:"+position);

                // 使得所有的图像在[0.5, 1.5)*len之间，无限循环切换
                boolean refresh = false;
                // int first = len / 2, last = first + len;
                // while (position < first) {
                //     position += len;
                //     refresh = true;
                // }
                // while (position >= last) {
                //     position -= len;
                //     refresh = true;
                // }
                // if (refresh) {
                //     fancyCoverFlow.setSelection(position);
                // }

                // fancyCoverFlow.setSelection(position);
                // adapter.notifyDataSetChanged();
                cur_index = position;
                Log.d("Gallery", "2 cur index:"+cur_index);
            }

            @Override
            public void onNothingSelected(EcoGalleryAdapterView<?> parent) {

            }

            // @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                cur_index = position;
                Log.d("Gallery", "cur index:"+cur_index);

                int len = adapter.getData().size();

                // 使得所有的图像在[0.5, 1.5)*len之间，无限循环切换
                int first = len / 2, last = first + len;
                while (position < first)
                    position += len;
                while (position >= last)
                    position -= len;

                fancyCoverFlow.setSelection(position);
                adapter.notifyDataSetChanged();

                //记录选择的图像索引
                // if(cur_index >= len) cur_index -= len;
            }

            // @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 点击事件
        fancyCoverFlow.setOnItemClickListener(new EcoGalleryAdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(EcoGalleryAdapterView<?> parent, View view, int position, long id) {

            }

            // @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
            }
        });

        findViewById(R.id.startBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // fancyCoverFlow.moveToNext(start);
                // startPlay();
                // stop = false;
                // Message msg = handler.obtainMessage(MSG_UPDATE);
                // handler.sendMessage(msg);
                Log.d("CGallery", "width:"+fancyCoverFlow.getWidth());
                fancyCoverFlow.startFling(-100);
            }
        });

        findViewById(R.id.stopBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // stopPlay();
                stop = true;
                reset();
                fancyCoverFlow.stop();
            }
        });

        findViewById(R.id.startFlipBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fancyCoverFlow.startFling(-100);
            }
        });


        // 开启自动轮播
        count_drawble = adapter.getCount();
        // startPlay();
        // fancyCoverFlow.startFling();
        fancyCoverFlow.setSelection(Integer.MAX_VALUE/2);

        fancyCoverFlow.setBackgroundColor(Color.GRAY);

        // TranslateAnimation translateAnimation = new TranslateAnimation(
        //         Animation.RELATIVE_TO_SELF, 0f,
        //         Animation.RELATIVE_TO_SELF, 1f,
        //         Animation.RELATIVE_TO_SELF, 0f,
        //         Animation.RELATIVE_TO_SELF, 0f);
        // translateAnimation.setDuration(15000);
        // fancyCoverFlow.startAnimation(translateAnimation);
    }

    /**
     * 开始轮播图切换
     */
    private void startPlay() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new AutoPlayTask(), 1, 400,
                TimeUnit.MICROSECONDS);
    }

    /**
     * 停止轮播图切换
     */
    private void stopPlay() {
        scheduledExecutorService.shutdown();
    }

    /**
     * 执行轮播图切换任务
     *
     */
    private class AutoPlayTask implements Runnable {

        @Override
        public void run() {

            cur_index = cur_index % count_drawble; // 图片区间[0,count_drawable)
            Message msg = handler.obtainMessage(MSG_UPDATE);
            handler.sendMessage(msg);
            cur_index++;
        }
    }
}
