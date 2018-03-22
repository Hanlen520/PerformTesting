package com.example.dell.performtesting;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.TrafficStats;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.example.dell.performtesting.CMforApp.CMListener;
import com.example.dell.performtesting.CMforApp.CMUtil;
import com.example.dell.performtesting.CacheMine.MyDataCleanManager;
import com.example.dell.performtesting.DeviceCheckUtil.CheckUtils;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.takt.Audience;
import jp.wasabeef.takt.Seat;
import jp.wasabeef.takt.Takt;

public class MainActivity extends AppCompatActivity implements CMListener {

    private RecyclerView recyclerView;
    private AppInfoAdapters appInfoAdapters;
    private List<AppTrafficBean> listApps = new ArrayList<>();

    private TextView trafficShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Takt.stock(getApplication())
                .seat(Seat.RIGHT_CENTER)
                .color(Color.BLUE)
                .size(20f)
                .listener(new Audience() {
                    @Override
                    public void heartbeat(double v) {
                        Log.i("FPS检测",v + "fps");
                    }
                })
                .play();

        String info = CheckUtils.getTotalCacheSize(MainActivity.this);
        Log.i("本应用缓存",info);
        initView();//初始化组件

        //集成Bugly
        CheckUtils.setBugly(getApplicationContext());

        //遍历有联网权限的应用程序的流量记录(从本次开机起开始记录)
        //trafficMonitor();
        //appInfoAdapters.notifyDataSetChanged();//刷新recyclerview

        //查看设备的CPU和内存使用情况
        CMUse();
    }

    //程序终止时结束FPS检测
    @Override
    protected void onDestroy() {
        Takt.finish();
        super.onDestroy();
    }

    /**初始化组件*/
    public void initView(){
        //初始化组件
        trafficShow = (TextView) findViewById(R.id.traffic_show);
        recyclerView = (RecyclerView) findViewById(R.id.rv_main);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appInfoAdapters = new AppInfoAdapters(listApps);
        recyclerView.setAdapter(appInfoAdapters);
    }


    /**
     * 获取开机到现在各应用的上传下载流量：
     * 遍历有联网权限的应用程序的流量记录
     * 获取手机通过 2G/3G 接收的字节流量总数：TrafficStats.getMobileRxBytes();
     * 获取手机通过 2G/3G 接收的数据包总数：TrafficStats.getMobileRxPackets();
     * 获取手机通过 2G/3G 发出的字节流量总数：TrafficStats.getMobileTxBytes();
     * 获取手机通过 2G/3G 发出的数据包总数：TrafficStats.getMobileTxPackets();
     * 获取手机通过所有网络方式接收的字节流量总数(包括 wifi)：TrafficStats.getTotalRxBytes();
     * 获取手机通过所有网络方式接收的数据包总数(包括 wifi)：TrafficStats.getTotalRxPackets();
     * 获取手机通过所有网络方式发送的字节流量总数(包括 wifi)：TrafficStats.getTotalTxBytes();
     * 获取手机通过所有网络方式发送的数据包总数(包括 wifi)：TrafficStats.getTotalTxPackets();
     * 获取手机指定 UID 对应的应程序用通过所有网络方式接收的字节流量总数(包括 wifi)：TrafficStats.getUidRxBytes(uid);
     * 获取手机指定 UID 对应的应用程序通过所有网络方式发送的字节流量总数(包括 wifi)：TrafficStats.getUidTxBytes(uid);*/
    private void trafficMonitor(){
        PackageManager pm = this.getPackageManager();
        //获得所有已经安装的列表
        List<PackageInfo> packInfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_PERMISSIONS);
        for (PackageInfo info : packInfos) {
            String[] premissions = info.requestedPermissions;//获取本应用的权限数组
            if (premissions != null && premissions.length > 0) {
                //若本应用有联网权限，则记录其上传和下载的流量
                for (String premission : premissions) {
                    if ("android.permission.INTERNET".equals(premission)) {
                        int uid = info.applicationInfo.uid;

                        //根据UID获取此app的上传下行流量
                        long rx = TrafficStats.getUidRxBytes(uid);
                        long tx = TrafficStats.getUidTxBytes(uid);

                        //将本应用的基本情况存储起来
                        AppTrafficBean appTrafficBean = new AppTrafficBean();
                        appTrafficBean.setAppInfo(info.applicationInfo);
                        appTrafficBean.setDownload(rx);
                        appTrafficBean.setUpload(tx);
                        listApps.add(appTrafficBean);
                    }
                }
            }
        }
    }

    /**
     * 查看本应用的CPU和内存使用情况
     */
    private void CMUse(){
        CMUtil cmUtil = new CMUtil(MainActivity.this);
        cmUtil.getInstance().init(getApplicationContext(), 1000L);
        cmUtil.getInstance().start();
    }

    /**
     * 监测内存和CPU的方法在另一个线程执行
     * 故需要使用回调将数据传递出来
     */
    @Override
    public void getCMFinish(final String CMData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                trafficShow.setText(CMData);
            }
        });
    }
}