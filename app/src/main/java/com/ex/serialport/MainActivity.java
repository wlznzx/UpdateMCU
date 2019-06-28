package com.ex.serialport;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ex.serialport.adapter.LogListAdapter;
import com.ex.serialport.adapter.SpAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import android_serialport_api.SerialPortFinder;
import tp.xmaihh.serialport.SerialHelper;
import tp.xmaihh.serialport.bean.ComBean;
import tp.xmaihh.serialport.utils.ByteUtil;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recy;
    private Spinner spSerial;
    private EditText edInput;
    private Button btSend;
    private RadioGroup radioGroup;
    private RadioButton radioButton1;
    private RadioButton radioButton2;
    private SerialPortFinder serialPortFinder;
    private SerialHelper serialHelper;
    private Spinner spBote;
    private Button btOpen, btnUpdate, btnSelectFile;
    private LogListAdapter logListAdapter;
    private Spinner spDatab;
    private Spinner spParity;
    private Spinner spStopb;
    private Spinner spFlowcon;

    private TextView versionTV, selectFileTV;

    private ProgressBar updateBar;
    private String filePath;

    int _progress = 0;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialHelper.close();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recy = (RecyclerView) findViewById(R.id.recyclerView);
        spSerial = (Spinner) findViewById(R.id.sp_serial);
        edInput = (EditText) findViewById(R.id.ed_input);
        btSend = (Button) findViewById(R.id.btn_send);
        spBote = (Spinner) findViewById(R.id.sp_baudrate);
        btOpen = (Button) findViewById(R.id.btn_open);
        btnUpdate = findViewById(R.id.btn_update);
        selectFileTV = findViewById(R.id.tv_select_file);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioButton1 = (RadioButton) findViewById(R.id.radioButton1);
        radioButton2 = (RadioButton) findViewById(R.id.radioButton2);
        btnSelectFile = findViewById(R.id.btn_select_file);

        spDatab = (Spinner) findViewById(R.id.sp_databits);
        spParity = (Spinner) findViewById(R.id.sp_parity);
        spStopb = (Spinner) findViewById(R.id.sp_stopbits);
        spFlowcon = (Spinner) findViewById(R.id.sp_flowcon);
        updateBar = findViewById(R.id.update_progress);
        versionTV = findViewById(R.id.version_tv);
        logListAdapter = new LogListAdapter(null);
        recy.setLayoutManager(new LinearLayoutManager(this));
        recy.setAdapter(logListAdapter);
        recy.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        serialPortFinder = new SerialPortFinder();
        serialHelper = new SerialHelper("/dev/ttyMT1", 9600) {
            @Override
            protected void onDataReceived(final ComBean comBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (radioGroup.getCheckedRadioButtonId() == R.id.radioButton1) {
                            try {
                                // Toast.makeText(getBaseContext(), new String(comBean.bRec, "UTF-8"), Toast.LENGTH_SHORT).show();
                                logListAdapter.addData(comBean.sRecTime + ":   " + new String(comBean.bRec, "UTF-8"));
                                if (logListAdapter.getData() != null && logListAdapter.getData().size() > 0) {
                                    recy.smoothScrollToPosition(logListAdapter.getData().size());
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Toast.makeText(getBaseContext(), ByteUtil.ByteArrToHex(comBean.bRec), Toast.LENGTH_SHORT).show();

                            if (comBean.bRec[0] == 0x05) {
                                versionTV.setText("当前版本：v" + comBean.bRec[4]);
                                // Toast.makeText(getBaseContext(), "" + comBean.bRec[4], Toast.LENGTH_SHORT).show();
                            }
                            Log.d("wlDebug", "data = " + ByteUtil.ByteArrToHex(comBean.bRec));
                            logListAdapter.addData(comBean.sRecTime + ":   " + ByteUtil.ByteArrToHex(comBean.bRec));
                            if (logListAdapter.getData() != null && logListAdapter.getData().size() > 0) {
                                recy.smoothScrollToPosition(logListAdapter.getData().size());
                            }
                        }
                    }
                });
            }
        };

        // final String[] ports = serialPortFinder.getAllDevicesPath();
        final String[] ports = {"/dev/ttyMT1"};
        final String[] botes = new String[]{"0", "50", "75", "110", "134", "150", "200", "300", "600", "1200", "1800", "2400", "4800", "9600", "19200", "38400", "57600", "115200", "230400", "460800", "500000", "576000", "921600", "1000000", "1152000", "1500000", "2000000", "2500000", "3000000", "3500000", "4000000"};
        final String[] databits = new String[]{"8", "7", "6", "5"};
        final String[] paritys = new String[]{"NONE", "ODD", "EVEN"};
        final String[] stopbits = new String[]{"1", "2"};
        final String[] flowcons = new String[]{"NONE", "RTS/CTS", "XON/XOFF"};


        SpAdapter spAdapter = new SpAdapter(this);
        spAdapter.setDatas(ports);
        spSerial.setAdapter(spAdapter);

        spSerial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // serialHelper.close();
                serialHelper.setPort(ports[position]);
                btOpen.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SpAdapter spAdapter2 = new SpAdapter(this);
        spAdapter2.setDatas(botes);
        spBote.setAdapter(spAdapter2);

        spBote.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // serialHelper.close();
                serialHelper.setBaudRate(botes[position]);
                btOpen.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SpAdapter spAdapter3 = new SpAdapter(this);
        spAdapter3.setDatas(databits);
        spDatab.setAdapter(spAdapter3);

        spDatab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // serialHelper.close();
                serialHelper.setDataBits(Integer.parseInt(databits[position]));
                btOpen.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SpAdapter spAdapter4 = new SpAdapter(this);
        spAdapter4.setDatas(paritys);
        spParity.setAdapter(spAdapter4);

        spParity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // serialHelper.close();
                serialHelper.setParity(position);
                btOpen.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SpAdapter spAdapter5 = new SpAdapter(this);
        spAdapter5.setDatas(stopbits);
        spStopb.setAdapter(spAdapter5);

        spStopb.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // serialHelper.close();
                serialHelper.setStopBits(Integer.parseInt(stopbits[position]));
                btOpen.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SpAdapter spAdapter6 = new SpAdapter(this);
        spAdapter6.setDatas(flowcons);
        spFlowcon.setAdapter(spAdapter6);

        spFlowcon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // serialHelper.close();
                serialHelper.setFlowCon(position);
                btOpen.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    serialHelper.open();
                    btOpen.setEnabled(false);
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "msg: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (SecurityException ex) {
                    Toast.makeText(MainActivity.this, "msg: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });


        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serialHelper.isOpen()) {
                    if (filePath == null) {
                        Toast.makeText(MainActivity.this, "請先選擇升級文件.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnUpdate.setEnabled(false);
                                    btSend.setEnabled(false);
                                    btnSelectFile.setEnabled(false);
                                    updateBar.setVisibility(View.VISIBLE);
                                }
                            });


                            // 升級程序
                            /*
                            byte[] tempbytes = new byte[32];
                            int byteread = 0;
                            try {
                                InputStream is = getAssets().open("Updata-V14.0-v1.1gpio");

                                final int totalSize = is.available();

                                Log.d("wlDebug", "send start.");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateBar.setMax(totalSize);
                                    }
                                });
                                while ((byteread = is.read(tempbytes)) != -1) {
                                    serialHelper.send(tempbytes);
                                    Thread.sleep(100);
                                    Log.d("wlDebug", "send = " + bytesToHexString(tempbytes));
                                    _progress += byteread;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateBar.setProgress(_progress);
                                        }
                                    });
                                }
                                Thread.sleep(1000);
                                serialHelper.sendHex("04b0040048");
                                Log.d("wlDebug", "result" + byteread);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            */

                            File f = new File(filePath);
                            if (f.exists() && f.isFile()) {

                            } else {

                            }
                            byte[] tempbytes = new byte[32];
                            int byteread = 0;
                            _progress = 0;
                            try {
                                FileInputStream in = new FileInputStream(f);
                                final int totalSize = in.available();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateBar.setMax(totalSize);
                                    }
                                });

                                while ((byteread = in.read(tempbytes)) != -1) {
                                    serialHelper.send(tempbytes);
                                    Thread.sleep(100);
                                    _progress += byteread;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateBar.setProgress(_progress);
                                        }
                                    });
                                    Log.d("wlDebug", "send = " + bytesToHexString(tempbytes));
                                }
                                Log.d("wlDebug", "send end.");
                                Thread.sleep(1000);
                                serialHelper.sendHex("04b0040048");
                                Log.d("wlDebug", "result" + byteread);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            // 升級程序end.

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnUpdate.setEnabled(true);
                                    btSend.setEnabled(true);
                                    btnSelectFile.setEnabled(true);
                                    updateBar.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    }).start();
                } else {
                    Toast.makeText(getBaseContext(), "串口没打开", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.radioButton1) {
                    if (edInput.getText().toString().length() > 0) {
                        if (serialHelper.isOpen()) {
                            // serialHelper.sendTxt(edInput.getText().toString());
                            if (filePath == null) {
                                Toast.makeText(MainActivity.this, "請先選擇升級文件.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    /**/
                                    File f = new File(filePath);
                                    if (f.exists() && f.isFile()) {

                                    } else {

                                    }
                                    byte[] tempbytes = new byte[32];
                                    int byteread = 0;
                                    try {
                                        FileInputStream in = new FileInputStream(f);
                                        while ((byteread = in.read(tempbytes)) != -1) {
                                            serialHelper.send(tempbytes);
                                            Thread.sleep(100);
                                            Log.d("wlDebug", "send = " + bytesToHexString(tempbytes));
                                        }
                                        Log.d("wlDebug", "send end.");
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    /*
                                    byte[] tempbytes = new byte[32];
                                    int byteread = 0;
                                    try {
                                        InputStream is = getAssets().open("Updata-V8.0.bin");
                                        int lenght = is.available();
//                                        String result = new String(buffer, "utf8");
//                                        serialHelper.send(buffer);

//                                        FileInputStream in = new FileInputStream(f);
                                        Log.d("wlDebug", "send start.");
                                        while ((byteread = is.read(tempbytes)) != -1) {
                                            serialHelper.send(tempbytes);
                                            Thread.sleep(100);
                                            Log.d("wlDebug", "send = " + bytesToHexString(tempbytes));
                                        }
                                        Log.d("wlDebug", "result" + byteread);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    */

                                }
                            }).start();
                        } else {
                            Toast.makeText(getBaseContext(), "串口没打开", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "先填数据吧", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (edInput.getText().toString().length() > 0) {
                        if (serialHelper.isOpen()) {
                            serialHelper.sendHex(edInput.getText().toString());
                        } else {
                            Toast.makeText(getBaseContext(), "串口没打开", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "先填数据吧", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        try {
            serialHelper.open();
            btOpen.setEnabled(false);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "msg: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (SecurityException ex) {
            Toast.makeText(MainActivity.this, "msg: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        requestStoragePermission(this);
    }

    public String getFileName(String pathandname) {

        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return pathandname.substring(start + 1, end);
        } else {
            return null;
        }

    }

    String[] PERMISSION_STORAGES = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,//读内存卡的权限
    };
    private static final int REQUEST_CODE_STORAGE = 1;

    /**
     * 申请权限 SD卡的读写权限
     *
     * @param activity
     */
    private void requestStoragePermission(Activity activity) {
        //检测权限
        int permission = ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //没有权限 则申请权限  弹出对话框
            ActivityCompat.requestPermissions(activity, PERMISSION_STORAGES, REQUEST_CODE_STORAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String path;
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                path = uri.getPath();
                Toast.makeText(this, path + "11111", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                path = getPath(this, uri);
                if (path != null) {
                    Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
                    filePath = path;
                    selectFileTV.setText(getFileName(path));
                }
            } else {//4.4以下下系统调用方法
            }
        }
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clean:
                logListAdapter.clean(); //清空
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private String createUpdateStr(String filepath) throws FileNotFoundException {
        File f = new File(filepath);
        if (f.exists() && f.isFile()) {
            // logger.info(f.length());
        } else {
            return null;
        }
        byte[] cmd = new byte[15];
        cmd[0] = 0x1b;
        cmd[1] = 0x23;
        cmd[2] = 0x23;
        cmd[3] = 0x55;
        cmd[4] = 0x50;
        cmd[5] = 0x50;
        cmd[6] = 0x47;
        // 校验和
        cmd[7] = 0;
        cmd[8] = 0;
        cmd[9] = 0;
        cmd[10] = 0;
        // 长度
        cmd[11] = 0;
        cmd[12] = 0;
        cmd[13] = 0;
        cmd[14] = 0;

        cmd[11] = (byte) (f.length() >> 0);
        cmd[12] = (byte) (f.length() >> 8);
        cmd[13] = (byte) (f.length() >> 16);
        cmd[14] = (byte) (f.length() >> 24);

        long fileCheckSum = 0;
        byte[] buf = new byte[1];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream fileInputStream = new FileInputStream(f);
        try {
            while (fileInputStream.read(buf) != -1) {
                fileCheckSum += buf[0];
                out.write(buf, 0, 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        cmd[7] = (byte) (fileCheckSum >> 0);
        cmd[8] = (byte) (fileCheckSum >> 8);
        cmd[9] = (byte) (fileCheckSum >> 16);
        cmd[10] = (byte) (fileCheckSum >> 24);

        // String result = bytesToHexString(cmd) + bytesToHexString(out.toByteArray());
        String result = bytesToHexString(out.toByteArray());

        /*
        byte[] array = out.toByteArray();
        for(int i = 0;i < out.toByteArray().length;i++){
            byte b = array[i];
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            Log.d("Ginger", "i = " + hv);
        }

        return result;
        */
        return result;
    }

    private byte[] createUpdateBytes(String filepath) throws FileNotFoundException {
        File f = new File(filepath);
        if (f.exists() && f.isFile()) {
            // logger.info(f.length());
        } else {
            return null;
        }
        // String result = bytesToHexString(cmd) + bytesToHexString(out.toByteArray());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] array = out.toByteArray();
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return array;
    }

    /*
     * byte——>String
     *
     * */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
