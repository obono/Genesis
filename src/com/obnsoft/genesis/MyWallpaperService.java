/*
 * Copyright (C) 2012, 2013 OBN-soft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.obnsoft.genesis;

import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class MyWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    public static void myLog(String msg) {
        Log.d("Genesis", msg);
    }

    /*-----------------------------------------------------------------------*/

    class MyEngine extends Engine {

        private MyThread mThread = null;

        @Override
        public void onCreate(SurfaceHolder holder) {
            myLog("onCreated");
            super.onCreate(holder);

            holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            myLog("onSurfaceCreated");
            super.onSurfaceCreated(holder);

            mThread = new MyThread(getSurfaceHolder());
            mThread.start();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            myLog("onSurfaceChanged: " + width + "x" + height);
            super.onSurfaceChanged(holder, format, width, height);
            if (mThread != null) {
                mThread.setSize(width, height);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            myLog("onVisibilityChanged: " + visible);
            super.onVisibilityChanged(visible);
            if (mThread != null) {
                if (visible) {
                    mThread.onResume();
                } else {
                    mThread.onPause();
                }
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            myLog("onSurfaceDestroyed");
            super.onSurfaceDestroyed(holder);
            if (mThread != null) {
                mThread.onDestroy();
                mThread = null;
            }
        }

        @Override
        public void onDestroy() {
            myLog("onDestroy");
            super.onDestroy();
        }

    }

    /*-----------------------------------------------------------------------*/

    class MyThread extends Thread {

        private static final int DRAW_INTERVAL = 50;
        private static final int WAIT_INTERVAL = 500;

        private SurfaceHolder mHolder;
        private int mWindowWidth;
        private int mWindowHeight;
        private boolean mLoop;
        private boolean mPause;

        public MyThread(SurfaceHolder holder) {
            mHolder = holder;
        }

        @Override
        public void run() {
            myLog("Thread loop start");
            MyRenderer renderer = new MyRenderer();
            renderer.onStartDrawing(MyWallpaperService.this, mHolder);
            long tm = System.currentTimeMillis();
            boolean stopped = false;
            mLoop = true;
            mPause = false;
            while (mLoop) {
                if (mPause) {
                    stopped = true;
                    tm += WAIT_INTERVAL;
                } else {
                    if (stopped) {
                        renderer.onResumeDrawing();
                        stopped = false;
                    }
                    renderer.onDrawFrame(mWindowWidth, mWindowHeight);
                    tm += DRAW_INTERVAL;
                }
                long wait = tm - System.currentTimeMillis();
                if (wait > 0) {
                    try {
                        Thread.sleep(wait);
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    yield();
                    tm -= wait;
                }
            }
            renderer.onFinishDrawing();
            myLog("Thread loop end");
        }

        public void setSize(int width, int height) {
            mWindowWidth = width;
            mWindowHeight = height;
        }

        public void onPause() {
            mPause = true;
        }

        public void onResume() {
            mPause = false;
        }

        public void onDestroy() {
            synchronized(this) {
                mLoop = false;
            }
            try {
                join();
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
