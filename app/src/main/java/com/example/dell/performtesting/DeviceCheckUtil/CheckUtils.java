package com.example.dell.performtesting.DeviceCheckUtil;

import android.app.Application;
import android.content.Context;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.os.Environment;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import jp.wasabeef.takt.Audience;
import jp.wasabeef.takt.Seat;
import jp.wasabeef.takt.Takt;

/**
 * 对设备进行内存使用率，CPU使用率，
 * 流量消耗，电量消耗，耗时，
 * 移动终端相关资源利用率，帧率，渲染
 * crash，FPS，ANR等
 * 进行检测
 * Created by dell on 2018/3/16.
 */

public class CheckUtils {

    /**
     * 获取设备内存使用情况(设备的总内存情况)
     * 通过读取/proc/meminfo文件来获取内存信息
     *
     MemTotal: 所有可用RAM大小。
     MemFree: LowFree与HighFree的总和，被系统留着未使用的内存。
     Buffers: 用来给文件做缓冲大小。
     Cached: 被高速缓冲存储器（cache memory）用的内存的大小（等于diskcache minus SwapCache）。
     SwapCached:被高速缓冲存储器（cache memory）用的交换空间的大小。已经被交换出来的内存，仍然被存放在swapfile中，用来在需要的时候很快的被替换而不需要再次打开I/O端口。
     Active: 在活跃使用中的缓冲或高速缓冲存储器页面文件的大小，除非非常必要，否则不会被移作他用。
     Inactive: 在不经常使用中的缓冲或高速缓冲存储器页面文件的大小，可能被用于其他途径。
     SwapTotal: 交换空间的总大小。
     SwapFree: 未被使用交换空间的大小。
     Dirty: 等待被写回到磁盘的内存大小。
     Writeback: 正在被写回到磁盘的内存大小。
     AnonPages：未映射页的内存大小。
     Mapped: 设备和文件等映射的大小。
     Slab: 内核数据结构缓存的大小，可以减少申请和释放内存带来的消耗。
     SReclaimable:可收回Slab的大小。
     SUnreclaim：不可收回Slab的大小（SUnreclaim+SReclaimable＝Slab）。
     PageTables：管理内存分页页面的索引表的大小。
     NFS_Unstable:不稳定页表的大小。
     * */
    public static void getMemory(){//kB
        String path = "/proc/meminfo";//初始化文件路径
        String firstLine;
        String info;
        try{
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader,8192);
            //获取第一行数据，就是手机总内存大小
            while ((firstLine = br.readLine()) != null){
                info = "";
                String[] memInfo = firstLine.split("\\s+");
                //Log.i(memInfo[0],memInfo[1]+memInfo[2]);
                for (String s : memInfo){
                    info = info + s;
                }
                Log.i("内存信息-----------",info);
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取设备CPU核数
     * */
    public static int getNumCores() {
        class CpuFilter implements FileFilter{
            @Override
            public boolean accept(File pathname) {
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch (Exception e)
        {
            return 1;
        }
    }

    /**
     * 查看CPU使用情况
     * user：从系统启动开始累计到当前时刻，处于用户态的运行时间，不包含 nice值为负进程。；
     * nice：从系统启动开始累计到当前时刻，nice值为负的进程所占用的CPU时间；
     * system：从系统启动开始累计到当前时刻，处于核心态的运行时间；
     * idle：从系统启动开始累计到当前时刻，除IO等待时间以外的其它等待时间；
     * iowait：从系统启动开始累计到当前时刻，IO等待时间；
     * irq：从系统启动开始累计到当前时刻，硬中断时间；
     * softirq：从系统启动开始累计到当前时刻，软中断时间。
     * totalCpuTime是上述7个属性的和
     * 上述单位均为jiffies；*/
    public static void getCPU() {
        String procStatString;
        RandomAccessFile procStatFile = null;
        try {
            if (procStatFile == null) {
                procStatFile = new RandomAccessFile("/proc/stat", "r");
            } else {
                procStatFile.seek(0L);
            }
            //获取所有数据(cpu、cpu0、cpu1、intr、ctxt、btime、processes、procs_running、procs_blocked、softirq)
            /*while ((procStatString = procStatFile.readLine()) != null){
                Log.i("CPU使用情况--------",procStatString);
            }*/
            procStatString = procStatFile.readLine();//只取第一行数据
            String[] procStat = procStatString.split("\\s+");
            Log.i("user",procStat[1]);
            Log.i("nice",procStat[2]);
            Log.i("system",procStat[3]);
            Log.i("idle",procStat[4]);
            Log.i("iowait",procStat[5]);
            Log.i("irq",procStat[6]);
            Log.i("softirq",procStat[7]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 集成Bugly，实现ANR分析和Crash上传
     * */
    public static void setBugly(Context context){
        String packageName = context.getPackageName();
        String processName = getProcessName(Process.myPid());
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        CrashReport.initCrashReport(context, "6fdd37e893", false,strategy);
    }
    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 查看设备FPS
     * 需要在onCreate()中传入全局上下文，getApplication*/
    public static void getFPS(Application application){
        Takt.stock(application)
                .seat(Seat.RIGHT_CENTER)
                .color(Color.BLUE)
                .size(20f)
                .listener(new Audience() {
                    @Override
                    public void heartbeat(double v) {
                        Log.i("FPS检测",v + "fps");
                    }
                })
                .play();//FPS检测
    }

    /**
     * 查看各应用的缓存
     * */
    public static void getCaches(Context context){

        //因为此内部类会不断被调用，并在不确定的时间返回数据
        //故需要定义一个回调，当有新的数据返回时回调出去，在主界面刷新列表
        class MyPackageStateObserver extends IPackageStatsObserver.Stub {
            @Override
            public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                String packageName = pStats.packageName;
                long cacheSize = pStats.cacheSize;
                long dataSize = pStats.dataSize;
                StringBuffer sb = new StringBuffer();//每次调用此方法，创建一个缓冲区存储应用的Cache信息
                //if (cacheSize > 0) {
                //不判断cache的大小，将所有应用的缓存情况打印出来
                sb.append("packageName = " + packageName + "\n")
                        .append("   cacheSize: " + cacheSize + "\n")
                        .append("   dataSize: " + dataSize + "\n")
                        .append("-----------------------\n");
                Log.i("应用缓存情况",sb.toString());
                //}
            }
        }

        PackageManager pm = context.getPackageManager();//创建一个packageManager
        List<PackageInfo> packages = pm.getInstalledPackages(0);//用packageManager对象获取一个包含所有已安装应用的集合
        Log.i("应用个数",packages.size()+"");
        for (PackageInfo pinfo : packages) {
            String packageName = pinfo.packageName;//将集合中的应用逐个取出
            try {
                //调用内部类的方法进行处理
                Method getPackageSizeInfo = PackageManager.class.getDeclaredMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                getPackageSizeInfo.invoke(pm, packageName, new MyPackageStateObserver());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取本应用的缓存情况
     * Created by dell on 2018/3/21.
     */
    public static String getTotalCacheSize(Context context){
        try {
            //Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
            long cacheSize = getFolderSize(context.getCacheDir());
            //Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                cacheSize += getFolderSize(context.getExternalCacheDir());
            }
            return getFormatSize(cacheSize);
        } catch (Exception e) {
            Log.i("getTotalCacheSize方法出错","报错");
            e.printStackTrace();
        }
        return "error";
    }

    // 获取文件
    //Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
    //Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
    public static long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 格式化存储单位,KB、MB、GB等
     * @param size
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }
}