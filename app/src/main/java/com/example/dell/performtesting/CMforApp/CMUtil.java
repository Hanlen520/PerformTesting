package com.example.dell.performtesting.CMforApp;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Process;
import android.util.Log;

import java.io.RandomAccessFile;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * 监测本应用在设备上的CPU和内存使用率
 * 使用方法：
 * CMUtil cmUtil = new CMUtil(CMListener);
 * cmUtil.getInstance().init(getApplicationContext(), 1000L);
 * cmUtil.getInstance().start();
 * Created by dell on 2018/3/15.
 */

public class CMUtil implements Runnable {

    private volatile static CMUtil instance = null;
    private ScheduledExecutorService scheduler;
    private ActivityManager activityManager;
    private long freq;// freq为采样周期
    private Long lastCpuTime;
    private Long lastAppCpuTime;
    private RandomAccessFile procStatFile;
    private RandomAccessFile appStatFile;

    CMListener cmListener;

    Context mContext;

    public CMUtil(CMListener cmListener) {
        scheduler = Executors.newSingleThreadScheduledExecutor();

        this.cmListener = cmListener;
    }

    public CMUtil getInstance() {
        if (instance == null) {
            synchronized (CMUtil.class) {
                if (instance == null) {
                    instance = new CMUtil(cmListener);
                }
            }
        }
        return instance;
    }

    // freq为采样周期
    public void init(Context context, long freq) {
        mContext = context;
        activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        this.freq = freq;
    }

    public void start() {
        scheduler.scheduleWithFixedDelay(this, 0L, freq, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        double cpu = sampleCPU();
        double mem = sampleMemory();
        cmListener.getCMFinish("CPU：" + cpu + "\n" + "内存：" + mem);
        //Log.d("Sampler", "CPU: " + cpu + "%" + "    Memory: " + mem + "MB");
    }

    /**
     * 查看本应用的CPU使用情况
     * 附：
     * /proc/stat文件动态记录所有CPU活动的信息，
     * 该文件中的所有值都是从系统启动开始累计到当前时刻。
     * 所以，计算系统当前的CPU占用率的方法就是，
     * 计算在间隔较短（ms级）的时间内，
     * cpu的各活动信息的变化量，
     * 作为当前的实时CPU占用率。*/
    private double sampleCPU() {
        long cpuTime;
        long appTime;
        double sampleValue = 0.0D;
        try {
            if (procStatFile == null || appStatFile == null) {
                procStatFile = new RandomAccessFile("/proc/stat", "r");
                appStatFile = new RandomAccessFile("/proc/" + Process.myPid() + "/stat", "r");
            } else {
                procStatFile.seek(0L);
                appStatFile.seek(0L);
            }
            String procStatString = procStatFile.readLine();
            String appStatString = appStatFile.readLine();
            String procStats[] = procStatString.split(" ");
            String appStats[] = appStatString.split(" ");
            cpuTime = Long.parseLong(procStats[2]) + Long.parseLong(procStats[3])
                    + Long.parseLong(procStats[4]) + Long.parseLong(procStats[5])
                    + Long.parseLong(procStats[6]) + Long.parseLong(procStats[7])
                    + Long.parseLong(procStats[8]);
            appTime = Long.parseLong(appStats[13]) + Long.parseLong(appStats[14]);
            if (lastCpuTime == null && lastAppCpuTime == null) {
                lastCpuTime = cpuTime;
                lastAppCpuTime = appTime;
                return sampleValue;
            }
            sampleValue = ((double) (appTime - lastAppCpuTime) / (double) (cpuTime - lastCpuTime)) * 100D;
            lastCpuTime = cpuTime;
            lastAppCpuTime = appTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sampleValue;
    }

    /**获取本应用的内存分配情况
     * 附：
     * 从ActivityManager获得MemoryInfo
     * 从MemoryInfo里获得TotalPss：memInfo[0].getTotalPss()，其中TotalPss = dalvikPss + nativePss + otherPss*/
    private double sampleMemory() {
        double mem = 0.0D;
        try {
            // 统计进程的内存信息 totalPss
            final Debug.MemoryInfo[] memInfo = activityManager.getProcessMemoryInfo(new int[]{Process.myPid()});
            if (memInfo.length > 0) {
                // TotalPss = dalvikPss + nativePss + otherPss, in KB
                final int totalPss = memInfo[0].getTotalPss();
                if (totalPss >= 0) {
                    // Mem in MB
                    mem = totalPss / 1024.0D;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mem;
    }

    /**获取最大分配，现分配和空闲内存*/
    private void getMemoryInfo(){
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
        //最大分配内存
        int memory = activityManager.getMemoryClass();
        System.out.println("memory: "+memory);
        //最大分配内存获取方法2
        float maxMemory = (float) (Runtime.getRuntime().maxMemory() * 1.0/ (1024 * 1024));
        //当前分配的总内存
        float totalMemory = (float) (Runtime.getRuntime().totalMemory() * 1.0/ (1024 * 1024));
        //剩余内存
        float freeMemory = (float) (Runtime.getRuntime().freeMemory() * 1.0/ (1024 * 1024));
        Log.i("maxMemory: ",maxMemory+"");
        Log.i("totalMemory: ",totalMemory+"");
        Log.i("freeMemory: ",freeMemory+"");
    }
}