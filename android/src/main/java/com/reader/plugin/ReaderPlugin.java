package com.reader.plugin;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.library.fileviewer.FileViewer;
import com.tencent.smtt.sdk.QbSdk;


@NativePlugin(
        permissionRequestCode = 8002
)
public class ReaderPlugin extends Plugin {
    public static final int REQ_WRITE_READ = 8002;
    private String url,title,navbarColor;

    @PluginMethod()
    public void openFile(PluginCall call) {

        //openFileReader(this.getContext(),path);
        if (!this.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) || !this.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            this.pluginRequestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_WRITE_READ);
            saveCall(call);
        } else {
             url = call.getString("url");
            title = call.getString("title","");
            navbarColor = call.getString("navbarColor");
            FileViewer.viewFile(this.getContext(),url,title,navbarColor);
            call.success();
        }

    }

    // 初始化x5内核
    public static void initX5(final Context context){

        final QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                System.out.print("x5 init successed");
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
            }

            @Override
            public void onCoreInitFinished() {
                System.out.print("x5 init failed");
                // TODO Auto-generated method stub
            }
        };
        new Thread(new Runnable(){
            @Override
            public void run() {
                //x5内核初始化接口
                QbSdk.initX5Environment(context,  cb);
            }
          }).start();
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);
        PluginCall savedCall = getSavedCall();
        if (savedCall == null) {
            return;
        }

        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                savedCall.error("User denied permission");
                return;
            }
        }

        if (requestCode == REQ_WRITE_READ) {
            FileViewer.viewFile(this.getContext(),url,title,navbarColor);
            savedCall.success();
        }
        freeSavedCall();
    }
}
