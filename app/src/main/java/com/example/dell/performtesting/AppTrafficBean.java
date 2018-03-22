package com.example.dell.performtesting;

import android.content.pm.ApplicationInfo;

import java.io.Serializable;

/**
 * app的信息集合(名字，图标，上行下行流量)
 * Created by dell on 2018/3/15.
 */
public class AppTrafficBean implements Serializable {

    private ApplicationInfo appInfo;

    private long download;
    private long upload;


    public long getDownload() {
        return download;
    }

    public void setDownload(long download) {
        this.download = download;
    }

    public long getUpload() {
        return upload;
    }

    public void setUpload(long upload) {
        this.upload = upload;
    }

    public ApplicationInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(ApplicationInfo appInfo) {
        this.appInfo = appInfo;
    }
}
