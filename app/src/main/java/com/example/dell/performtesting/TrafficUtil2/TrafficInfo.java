package com.example.dell.performtesting.TrafficUtil2;

/**
 * Created by dell on 2018/3/15.
 */

public class TrafficInfo {
    private String Packagename;
    private String Appname;
    private int Icon;
    private long Rx;
    private long Tx;

    public long getRx() {
        return Rx;
    }

    public void setRx(long rx) {
        Rx = rx;
    }

    public long getTx() {
        return Tx;
    }

    public void setTx(long tx) {
        Tx = tx;
    }

    public String getPackagename() {
        return Packagename;
    }

    public void setPackagename(String packagename) {
        Packagename = packagename;
    }

    public String getAppname() {
        return Appname;
    }

    public void setAppname(String appname) {
        Appname = appname;
    }

    public int getIcon() {
        return Icon;
    }

    public void setIcon(int icon) {
        Icon = icon;
    }

}
