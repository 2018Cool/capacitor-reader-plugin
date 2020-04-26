package com.hanlyjiang.library.fileviewer.tbs;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import android.widget.TextView;
import android.widget.Toast;

import com.hanlyjiang.library.utils.FileViewerUtils;
import com.reader.plugin.capacitorreaderplugin.R;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;

/**
 * 此 Activity 使用 TBS （腾讯浏览服务）查看文件
 * <br/> 默认支持常见文件类型
 * <br/> <b>默认支持类型：</b>
 * <li>doc</li>
 * <li>docx</li>
 * <li>ppt</li>
 * <li>pptx</li>
 * <li>xls</li>
 * <li>xlsx</li>
 * <li>txt</li>
 * <li>pdf</li>
 * <li>epub</li>
 * <br/>
 * intent参数: filePath - 文件路径
 *
 * @author hanlyjiang
 */
public class TBSFileViewActivity extends AppCompatActivity implements TbsReaderView.ReaderCallback {
    public static final String FILE_PATH = "filePath";

    private static final String TAG = "TBSFileViewActivity";

    private TbsReaderView mTbsReaderView;
    private FrameLayout rootViewParent;

    private TextView downloadTextView;
    private DownloadManager mDownloadManager;
    private long mRequestId;
    private DownloadObserver mDownloadObserver;
    private ViewGroup downloadLayout;

    private ViewGroup errorHandleLayout;
    private String mFileUrl; //文件url
    private String navbarColor; //文件url
    private String mFileName; //由文件url截取的文件名 上个页面传过来用于显示的文件名

    public static void viewFile(Context context, String fileUrl,String fileName,String navbarColor) {
        Intent intent = new Intent(context, TBSFileViewActivity.class);
        intent.putExtra("fileUrl", fileUrl);
        intent.putExtra("fileName", fileName);
        intent.putExtra("navbarColor", navbarColor);
        context.startActivity(intent);
    }

    /**
     * 获取传过来的文件url和文件名
     */
    private void getFileUrlByIntent() {
        Intent intent = getIntent();
        mFileUrl = intent.getStringExtra("fileUrl");
        mFileName = intent.getStringExtra("fileName");
        navbarColor = intent.getStringExtra("navbarColor");
        if(mFileName.equals("")){
            mFileName = getFileName(mFileUrl);
        }
    }

    public static String getFileName(String filePath) {
        if (filePath == null) {
            return "";
        }
        int lastSlashIndex = filePath.lastIndexOf("/") + 1;
        if (lastSlashIndex == -1) {
            return filePath;
        }
        return filePath.substring(lastSlashIndex);
    }

    private class DownloadObserver extends ContentObserver {

        private DownloadObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            queryDownloadStatus();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void queryDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query()
                .setFilterById(mRequestId);
        Cursor cursor = null;
        try {
            cursor = mDownloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                // 已经下载的字节数
                long currentBytes = cursor
                        .getLong(cursor
                                .getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                // 总需下载的字节数
                long totalBytes = cursor
                        .getLong(cursor
                                .getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                // 状态所在的列索引
                int status = cursor.getInt(cursor
                        .getColumnIndex(DownloadManager.COLUMN_STATUS));
                int progress = (int) ((currentBytes * 1.0) / totalBytes * 100);
                downloadTextView.setText("加载中...(" + progress + "%)");


                Log.i("downloadUpdate: ", currentBytes + " " + totalBytes + " "
                        + status + " " + progress);
                if (DownloadManager.STATUS_SUCCESSFUL == status
                        && downloadLayout.getVisibility() == View.VISIBLE) {
                    downloadLayout.setVisibility(View.GONE);
                    downloadLayout.performClick();
                    displayFile();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    private void startDownload() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                String fileType = parseFormat(mFileName);
                boolean result = mTbsReaderView.preOpen(fileType, false);
                if(!result){
                    mTbsReaderView.downloadPlugin(fileType);
                }
                //加载插件

            }
        }).start();
        mDownloadObserver = new DownloadObserver(new Handler());
        getContentResolver().registerContentObserver(
                Uri.parse("content://downloads/my_downloads"), true,
                mDownloadObserver);

        mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        //将含有中文的url进行encode
        String fileUrl = toUtf8String(mFileUrl);


        try {

            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(fileUrl));
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, mFileName);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            mRequestId = mDownloadManager.enqueue(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getLocalFile() {
        return new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                mFileName);
    }

    private String toUtf8String(String url) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = String.valueOf(c).getBytes("utf-8");
                } catch (Exception ex) {
                    System.out.println(ex);
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0)
                        k += 256;
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }
    private String getFileType(String fName) {

        String type = "";
        // 获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        /* 获取文件的后缀名 */
        type = fName.substring(dotIndex, fName.length()).toLowerCase();
        return type;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tbs_file_view_layout);
        rootViewParent = (FrameLayout) findViewById(R.id.fl_rootview);
        errorHandleLayout = (ViewGroup) findViewById(R.id.ll_error_handle);
        downloadLayout = (ViewGroup) findViewById(R.id.download_view);
        downloadTextView = findViewById(R.id.tv_download);
        initErrorHandleLayout(errorHandleLayout);

        getFileUrlByIntent();
        if (TextUtils.isEmpty(mFileUrl)) {
            Toast.makeText(this, "文件URL为空", Toast.LENGTH_SHORT).show();
            finish();
        }
        ActionBar actionBar = getSupportActionBar();
        ActionBar.LayoutParams lp =new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        View mActionBarView = LayoutInflater.from(this).inflate(R.layout.actionbar_layout, null);
        FrameLayout layout = mActionBarView.findViewById(R.id.mainBar);
        layout.setBackgroundColor(Color.parseColor(navbarColor));
        TextView textview= mActionBarView.findViewById(R.id.title);
        textview.setText(mFileName);
        ImageButton backButton = (ImageButton) mActionBarView.findViewById(R.id.ib_go_back);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        actionBar.setCustomView(mActionBarView, lp);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);


        mTbsReaderView = new TbsReaderView(this, this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        mTbsReaderView.setLayoutParams(layoutParams);
        mTbsReaderView.setBackgroundColor(Color.WHITE);
        rootViewParent.addView(mTbsReaderView);
        mTbsReaderView.setVisibility(View.GONE);
        errorHandleLayout.setVisibility(View.GONE);
        startDownload();
    }

    private void initErrorHandleLayout(ViewGroup errorHandleLayout) {
        findViewById(R.id.btn_retry_with_tbs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayFile();
            }
        });
        findViewById(R.id.btn_view_with_other_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileViewerUtils.viewFile4_4(v.getContext(), getLocalFile().getAbsolutePath());
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCallBackAction(Integer integer, Object long1, Object long2) {
        Log.d(TAG, "onCallBackAction " + integer + "," + long1 + "," + long2);
    }

    private void displayFile() {
        Bundle bundle = new Bundle();
        bundle.putString("filePath", getLocalFile().getPath());
        bundle.putString("tempPath", Environment.getExternalStorageDirectory().getPath());
        // preOpen 需要文件后缀名 用以判断是否支持
        boolean result=false;
        try{
            result = mTbsReaderView.preOpen(parseFormat(mFileName), false);
        }catch (Exception e){
            e.printStackTrace();
        }

        if (result) {
            mTbsReaderView.setVisibility(View.VISIBLE);
            errorHandleLayout.setVisibility(View.GONE);
            mTbsReaderView.openFile(bundle);
        } else {
            mTbsReaderView.setVisibility(View.GONE);
            errorHandleLayout.setVisibility(View.VISIBLE);
        }
    }


    private String parseFormat(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTbsReaderView.onStop();
        if (mDownloadObserver != null) {
            getContentResolver().unregisterContentObserver(mDownloadObserver);
        }
    }
}
