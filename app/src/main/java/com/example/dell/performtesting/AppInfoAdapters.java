package com.example.dell.performtesting;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * 应用信息的adapter
 * Created by dell on 2018/3/15.
 */

public class AppInfoAdapters extends RecyclerView.Adapter<AppInfoAdapters.ViewHolder>{
    List<AppTrafficBean> appInfoList;//新建一个集合用来存储APP信息(图标，名称，上行下行流量)
    Context context;
    PackageManager pm;

    //构造adapter时，需传入app信息集合
    public AppInfoAdapters(List<AppTrafficBean> appInfoList){
        this.appInfoList = appInfoList;
    }

    /**
     * 构造ViewHolder，并初始化组件
     * */
    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView ivLauncher;
        TextView tvName;
        TextView tvDownload;
        TextView tvUpload;

        public ViewHolder(View itemView) {
            super(itemView);
            ivLauncher = itemView.findViewById(R.id.iv_adapter_item_label);
            tvName = itemView.findViewById(R.id.tv_adapter_item_name);
            tvDownload = itemView.findViewById(R.id.tv_adapter_item_download);
            tvUpload = itemView.findViewById(R.id.tv_adapter_item_upload);
        }
    }

    /**用布局文件实例化ViewHolder*/
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();//获得上下文环境
        View view = LayoutInflater.from(context).inflate(R.layout.layout_main_item, parent, false);
        pm = context.getPackageManager();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppTrafficBean trafficModel = appInfoList.get(position);//单独获取每个app信息对象
        holder.ivLauncher.setImageDrawable(trafficModel.getAppInfo().loadIcon(pm));
        holder.tvName.setText((String) pm.getApplicationLabel(trafficModel.getAppInfo()));
        holder.tvDownload.setText("下行：" + Formatter.formatFileSize(context, trafficModel.getDownload()));
        holder.tvUpload.setText("上行：" + Formatter.formatFileSize(context, trafficModel.getUpload()));
    }

    @Override
    public int getItemCount() {
        return appInfoList.size();
    }
}