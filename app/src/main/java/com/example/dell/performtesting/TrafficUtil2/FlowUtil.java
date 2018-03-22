package com.example.dell.performtesting.TrafficUtil2;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by dell on 2018/3/15.
 */

public class FlowUtil {
    private static long Rx;//上传的流量
    private static long Tx;//下载的流量
    /**
     * 通过uid查询文件夹中的数据
     * 可查询从出厂到现在，所有应用的上传下载流量
     * @param localUid
     * @return
     */
    public Long getTotalBytesManual(int localUid) {
        File dir = new File("/proc/uid_stat/");
        String[] children = dir.list();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < children.length; i++) {
            stringBuffer.append(children[i]);
            stringBuffer.append("   ");
        }
        if (!Arrays.asList(children).contains(String.valueOf(localUid))) {
            return 0L;
        }
        File uidFileDir = new File("/proc/uid_stat/" + String.valueOf(localUid));
        File uidActualFileReceived = new File(uidFileDir, "tcp_rcv");
        File uidActualFileSent = new File(uidFileDir, "tcp_snd");
        String textReceived = "0";
        String textSent = "0";
        try {
            BufferedReader brReceived = new BufferedReader(new FileReader(uidActualFileReceived));
            BufferedReader brSent = new BufferedReader(new FileReader(uidActualFileSent));
            String receivedLine;
            String sentLine;

            if ((receivedLine = brReceived.readLine()) != null) {
                textReceived = receivedLine;
            }
            if ((sentLine = brSent.readLine()) != null) {
                textSent = sentLine;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Rx=Long.valueOf(textReceived).longValue();
        Tx=Long.valueOf(textSent).longValue();

        return Long.valueOf(textReceived).longValue() + Long.valueOf(textSent).longValue();
    }

    /**
     * 返回所有的有互联网访问权限的应用程序的流量信息。
     * TrafficInfo 为一个Bean 模型类。使用的时候可以自定义一个
     * @return
     */
    public List<TrafficInfo> getTrafficInfo(Context context) {
        //获取到配置权限信息的应用程序
        PackageManager pms = context.getPackageManager();
        List<PackageInfo> packinfos = pms
                .getInstalledPackages(PackageManager.GET_PERMISSIONS);
        //存放具有Internet权限信息的应用
        List<TrafficInfo> trafficInfos = new ArrayList<>();
        for (PackageInfo packinfo : packinfos) {
            //获取该应用的所有权限信息
            String[] permissions = packinfo.requestedPermissions;
            if (permissions != null && permissions.length > 0) {
                for (String permission : permissions) {
                    //筛选出具有Internet权限的应用程序
                    if ("android.permission.INTERNET".equals(permission)) {
                        //用于封装具有Internet权限的应用程序信息
                        TrafficInfo trafficInfo = new TrafficInfo();
                        //封装应用信息
                        trafficInfo.setPackagename(packinfo.packageName);
//                        trafficInfo.setIcon(packinfo.applicationInfo.loadIcon(pms));//设置图标，用到时再改
                        trafficInfo.setAppname(packinfo.applicationInfo.loadLabel(pms).toString());
                        //获取到应用的uid（user id）
                        int uid = packinfo.applicationInfo.uid;
                        //TrafficStats对象通过应用的uid来获取应用的下载、上传流量信息

                        //发送的 上传的流量byte
                        trafficInfo.setRx(TrafficStats.getUidRxBytes(uid));
                        //下载的流量 byte
                        trafficInfo.setTx(TrafficStats.getUidTxBytes(uid));
                        trafficInfos.add(trafficInfo);
                        if (getTotalBytesManual(uid) != 0) {
                            Log.i(TAG, "getTrafficInfo: "+"\n" +
                                    "包名：            " + trafficInfo.getPackagename() +"\n"+
                                    "APP名：           " + trafficInfo.getAppname() +"\n"+
                                    "uid:              " + uid +"\n"+
                                    "上传的流量  byte: " + Rx +"\n"+
                                    "下载的流量  byte: " + Tx +"\n"+
                                    "消耗总流量  byte：" + getTotalBytesManual(uid));
                        }
                        trafficInfo = null;
                        break;
                    }
                }
            }
        }
        return trafficInfos;
    }

}