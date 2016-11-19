package tickide.lexin.com.tickide;

import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tickide.lexin.com.tickide.BLE.BLEService;
import tickide.lexin.com.tickide.BLE.ConnectBle;
import tickide.lexin.com.tickide.BLE.UploadProgram;
import tickide.lexin.com.tickide.BLE.tools.Tools;
import tickide.lexin.com.tickide.DataApi.ReturnHex;
import tickide.lexin.com.tickide.Utils.CustomToast;
import tickide.lexin.com.tickide.Utils.FileUtils;
import tickide.lexin.com.tickide.Views.MyProgressBar;
import tickide.lexin.com.tickide.Views.ToastUtil;

import static tickide.lexin.com.tickide.Utils.FileUtils.getMsg;
import static tickide.lexin.com.tickide.Utils.FileUtils.readProgram;
import static tickide.lexin.com.tickide.Utils.FileUtils.to2String;

import android.os.Handler;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private PushAgent mPushAgent;
    Context context;
    private WebView webView;
    HttpURLConnection submitCodeConn = null;
    private String CompileUrl = "http://compiler.lexinsmart.com/compiler/LexinSmart/v1";
    private String FetchLibUrl = "http://alib.lexinsmart.com/alib/LexinSmart/v1";


    private int val = 0;
    private MyProgressBar myProgressBar;
    private TextView testText;

    private BluetoothGattCharacteristic mBluetoothGattCharacteristic;
    // 设置广播监听
    private MainActivity.BroadcastReceiver bluetoothReceiver;
    private int read_fmt_int = 1; // 接收数据格式
    //    WaveView downloadVave;
    private Button uploadBtn;
    private int responseOk = 0;
    private int sendHexFlag = 0;
    int size = 0;
    int address = 0;
    int programIndex = 0;
    byte[] program;
    private int proper = 0; // 通道权限
    private String libariesContent = "";
    private static int sizeIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        PushAgent.getInstance(context).onAppStart();

        PushAgent mPushAgent = PushAgent.getInstance(this);
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {

            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回device token
            }

            @Override
            public void onFailure(String s, String s1) {

            }
        });
        webView = (WebView) findViewById(R.id.webView);
        webView.setInitialScale(200);
        webView.loadUrl("file:///android_asset/mblockly/blockly/apps/mixly/index.html");

        //如果访问的页面中有Javascript，则webview必须设置支持Javascript。
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setDomStorageEnabled(true);
        webView.addJavascriptInterface(this, "test");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent uploadIntent = new Intent(MainActivity.this, UploadProgram.class);
//                startActivity(uploadIntent);
                //       webView.loadUrl("javascript:callJS()");


            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 11);
            }
        }
        myProgressBar = (MyProgressBar) findViewById(R.id.myprogressBar);
        myProgressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Tools.mBLEService.isConnected()) {
                    Toast.makeText(MainActivity.this, "未连接设备", Toast.LENGTH_SHORT).show();
                } else {
                    webView.loadUrl("javascript:callJS()");
                }
            }
        });
        testText = (TextView) findViewById(R.id.testtext);
        testText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myProgressBar.setMax_progress(80);
                responseOk = 0;
                sendHexFlag = 0;
                size = 0;
                address = 0;
                programIndex = 0;
                program = readProgram();
//                        myProgressBar.setMax_progress(0);

                new MainActivity.uploadthread().start();
            }
        });
////下面
        setBroadcastReceiver();
        mBluetoothGattCharacteristic = Tools.mBLEService.mBluetoothGatt
                .getServices().get(3)
                .getCharacteristics().get(5);
        // 查看是有什么权限
        proper = mBluetoothGattCharacteristic.getProperties();
        if (0 != (proper & 0x02)) { // 可读
        }
        if ((0 != (proper & BluetoothGattCharacteristic.PROPERTY_WRITE))
                || (0 != (proper & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE))) { // 可写
        }
        if ((0 != (proper & BluetoothGattCharacteristic.PROPERTY_NOTIFY))
                || (0 != (proper & BluetoothGattCharacteristic.PROPERTY_INDICATE))) { // 通知
            Tools.mBLEService.mBluetoothGatt.setCharacteristicNotification(
                    mBluetoothGattCharacteristic, true);
            BluetoothGattDescriptor descriptor = mBluetoothGattCharacteristic
                    .getDescriptor(UUID
                            .fromString("00002902-0000-1000-8000-00805f9b34fb"));
            descriptor
                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            Tools.mBLEService.mBluetoothGatt.writeDescriptor(descriptor);
        }
////上面
    }

    @JavascriptInterface
    public void hello(String data) {//对应js中xxx.hello("")
        data = data.replace("\n", "\\n") + "\\n";
        data = data.replace("\"", "\\\"");
        data = data.replace("\r", "\\n");

        System.out.println("code:" + data);

        Pattern pattern = Pattern.compile("^#include.*");
        Matcher matcher = pattern.matcher(data);
        boolean b = matcher.matches();  //当条件满足时，将返回true，否则返回false
        if (b) {
            ArrayList<String> headers = new ArrayList<String>();
            String regex = "include <([^.]*)";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(data);
            while (m.find()) {
                headers.add(m.group(1));
            }
            sizeIndex = 0;
            for (int i = 0; i < headers.size(); i++) {
                getLibs(data, headers.get(i), headers.size());

//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        } else {
            submitCode(data, "");
        }

    }

    //获得库文件内容
    private synchronized void getLibs(final String code, final String header, final int size) {
        System.out.println(":" + header.toString());
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL fetchLibUrl = null;
                try {

                    fetchLibUrl = new URL(FetchLibUrl);
                    HttpURLConnection fetchLibConn = (HttpURLConnection) fetchLibUrl.openConnection();
                    fetchLibConn.setDoInput(true);
                    fetchLibConn.setDoOutput(true);
                    fetchLibConn.setRequestMethod("POST");
                    fetchLibConn.setUseCaches(false);
                    fetchLibConn.connect();
                    DataOutputStream out = new DataOutputStream(fetchLibConn.getOutputStream());

                    StringBuffer params = new StringBuffer();
                    params.append("{ \"type\":\"fetch\", \"library\":\"");
                    params.append(header);
                    params.append("\"}");
                    byte[] bypes = params.toString().getBytes();
                    fetchLibConn.getOutputStream().write(bypes);// 输入参数
                    //刷新、关闭
                    out.flush();
                    out.close();

                    //获得响应状态
                    int resultCode = fetchLibConn.getResponseCode();
                    if (HttpURLConnection.HTTP_OK == resultCode) {
                        DataInput di = new DataInputStream(fetchLibConn.getInputStream());
                        String returnedString = di.readLine().toString();
                        if (returnedString.contains("\"success\":true")) {
                            System.out.println("returnedString" + returnedString);

                            org.json.JSONObject obj = new org.json.JSONObject(returnedString);

                            sizeIndex++;

                            System.out.println("sizeindex:" + sizeIndex + "    size:" + size);
                            if (sizeIndex == 1) {
                                libariesContent += "\"" + header + "\":" + obj.get("files") + "";
                            } else {
                                libariesContent += "," + "\"" + header + "\":" + obj.get("files") + "";
                            }

                            if (sizeIndex == size) {
                                Message message = new Message();
                                message.what = 1;
                                message.obj = "编译成功！";
                                mHandler.sendMessage(message);

                                submitCode(code, libariesContent);
                                sizeIndex = 0;
                                libariesContent = "";
                            } else {
                                Message message = new Message();
                                message.what = 1;
                                message.obj = "正在编译请稍后..";
                                mHandler.sendMessage(message);
                            }
                        } else {
                            Message message = new Message();
                            message.what = 1;
                            message.obj = returnedString;
                            mHandler.sendMessage(message);
                        }

                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void submitCode(final String code, final String libs) {

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    URL compileUrl = new URL(CompileUrl);
                    submitCodeConn = (HttpURLConnection) compileUrl.openConnection();
                    submitCodeConn.setDoInput(true);
                    submitCodeConn.setDoOutput(true);
                    submitCodeConn.setRequestMethod("POST");
                    submitCodeConn.setUseCaches(false);
                    submitCodeConn.connect();
                    DataOutputStream out = new DataOutputStream(submitCodeConn.getOutputStream());
                    //将要上传的内容写入流中
                    StringBuffer params = new StringBuffer();
                    params.append("{\n" +
                            "\"files\":[\n" +
                            "{\n" +
                            "\"filename\":\"sketch.ino\",\n" +
                            "\"content\":\"");
                    params.append(code);
                    params.append("\"\n" +
                            "    }\n" +
                            "  ], \n" +
                            "  \"libraries\": {");
                    params.append(libs);
                    params.append(" }, \n" +
                            "  \"format\": \"hex\", \n" +
                            "  \"version\": \"105\", \n" +
                            "  \"build\": {\n" +
                            "    \"mcu\": \"atmega328p\", \n" +
                            "    \"f_cpu\": \"16000000L\", \n" +
                            "    \"core\": \"arduino\", \n" +
                            "    \"variant\": \"standard\"\n" +
                            "  }\n" +
                            "}");

                    String filePath_1 = "/mnt/sdcard/";
                    String fileName_1 = "libs.txt";
                    FileUtils.writeTxtToFile(params.toString(), filePath_1, fileName_1);

                    byte[] bypes = params.toString().getBytes();
                    submitCodeConn.getOutputStream().write(bypes);// 输入参数
                    //刷新、关闭
                    out.flush();
                    out.close();

                    //获得响应状态
                    int resultCode = submitCodeConn.getResponseCode();
                    if (HttpURLConnection.HTTP_OK == resultCode) {
                        DataInput di = new DataInputStream(submitCodeConn.getInputStream());
                        String returnedString = di.readLine().toString();
                        System.out.println("returnedString" + returnedString);

                        if (returnedString.contains("\"success\":true")) {


                            ReturnHex returnHex = JSONObject.parseObject(returnedString, ReturnHex.class);
                            System.out.println("returnHex" + returnHex.getOutput());

                            String filePath = "/mnt/sdcard/";
                            String fileName = "firmware.hex";
                            FileUtils.writeTxtToFile(returnHex.getOutput(), filePath, fileName);


                            responseOk = 0;
                            sendHexFlag = 0;
                            size = 0;
                            address = 0;
                            programIndex = 0;
                            program = readProgram();
//                        myProgressBar.setMax_progress(0);

                            new MainActivity.uploadthread().start();
                        } else {
                            Message message = new Message();
                            message.what = 1;
                            message.obj = returnedString;
                            mHandler.sendMessage(message);
                        }


//                        Intent intent = new Intent(MainActivity.this, UploadProgram.class);
//                        startActivity(intent);
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ToastUtil.showShort(MainActivity.this, msg.obj.toString());
                    break;
                case 2:
                    testText.setText(msg.obj.toString());
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_bluetooth:
                Intent connectBleIntent = new Intent(MainActivity.this, ConnectBle.class);
                startActivity(connectBleIntent);
                break;
            case R.id.action_settings:
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setBroadcastReceiver() {
        // 创建一个IntentFilter对象，将其action指定为BluetoothDevice.ACTION_FOUND
        IntentFilter intentFilter1 = new IntentFilter(
                BLEService.ACTION_DATA_CHANGE);
        intentFilter1.addAction(BLEService.ACTION_READ_OVER);
        intentFilter1.addAction(BLEService.ACTION_RSSI_READ);
        intentFilter1.addAction(BLEService.ACTION_STATE_CONNECTED);
        intentFilter1.addAction(BLEService.ACTION_STATE_DISCONNECTED);
        intentFilter1.addAction(BLEService.ACTION_WRITE_OVER);
        bluetoothReceiver = new MainActivity.BroadcastReceiver();

        // 注册广播接收器
        registerReceiver(bluetoothReceiver, intentFilter1);
    }

    private class BroadcastReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 数据改变通知
            if (BLEService.ACTION_DATA_CHANGE.equals(action)) {
                dis_recive_msg(intent.getByteArrayExtra("value"));
                return;
            }
            // 读取数据
            if (BLEService.ACTION_READ_OVER.equals(action)) {
                dis_recive_msg(intent.getByteArrayExtra("value"));
                return;
            }
            // 连接状态改变
            if (BLEService.ACTION_STATE_CONNECTED.equals(action)) {
                CustomToast.showToast(MainActivity.this, "已经连接", Toast.LENGTH_SHORT);
            }
            if (BLEService.ACTION_STATE_DISCONNECTED.equals(action)) {
                Tools.mBLEService.disConectBle();
                CustomToast.showToast(MainActivity.this, "已断开连接", Toast.LENGTH_SHORT);
            }
        }
    }

    private void dis_recive_msg(byte[] tmp_byte) {
//        if (talking_stopdis_btn.isChecked())
//            return; // 停止显示

        String tmp = "";
        if (0 == tmp_byte.length) {
            return;
        }

        switch (read_fmt_int) {
            case 0: // 字符串显示
                tmp = new String(tmp_byte);
                break;
            case 1: // 16进制显示

                for (int i = 0; i < tmp_byte.length; i++) {
                    String hex = Integer.toHexString(tmp_byte[i] & 0xFF);
                    if (hex.length() == 1) {
                        hex = '0' + hex;
                    }
                    tmp += ' ';
                    tmp = tmp + hex;
                }
                break;
            case 2: // 10进制显示
                int count = 0;
                for (int i = 0; i < tmp_byte.length; i++) {
                    count *= 256;
                    count += (tmp_byte[tmp_byte.length - 1 - i] & 0xFF);
                }
                tmp = Integer.toString(count);
                break;
            default:
                break;
        }
        System.out.println("receive-------------------<" + tmp);
        if (tmp.endsWith(Integer.toHexString(0x10 & 0xff))) {

            if (sendHexFlag == 0) {
                responseOk++;
            }
            System.out.println("responseOk:" + responseOk + ";");

            switch (responseOk) {
                case 5:
                    sendMessage(getMsg("418120418220", 1)); //查询软件主次版本号码
                    break;
                case 6:                                     //查询设置参数
                    sendMessage(getMsg("428600000101010103ffffffff008004000000", 1));
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sendMessage(getMsg("800020", 1));
                    break;
                case 7:                                     // 并行编程
                    sendMessage(getMsg("450504d7c20020", 1));
                    break;
                case 8:
                    sendMessage(getMsg("5020", 1));     //进入设置模式。
                    break;
                case 9:
                    sendMessage(getMsg("7520", 1)); //获取设备签名。
                    break;
                case 10:                            //发送Hex文件
                    sendHexFlag++;

                    if (sendHexFlag % 2 == 1) {   //发送地址
                        int laddress = address % 256;
                        int haddress = address / 256;
                        address += 64;
                        System.out.println("发送地址。" + address);
                        System.out.println("laddress。" + laddress + "    haddress" + haddress);

                        String addressPackage = "";
                        addressPackage = "55" + to2String(laddress) + to2String(haddress) + "20";

                        System.out.println("addressPackage。"+ addressPackage);

                        sendMessage(getMsg(addressPackage, 1));

                        System.out.println("after   laddress。" + laddress + "    haddress" + haddress);


                    } else { //发送内容
                        if (program.length - programIndex < 128) {
                            size = program.length - programIndex;
                        } else {
                            size = 128;
                        }
                        System.out.println("programming page size: " + size);
                        if (size < 128) {
                            int tempindex = programIndex;
                            byte[] lastbytes = new byte[128];
                            for (int i = 0; i < size - 6; i++) {
                                lastbytes[i] = program[tempindex++];
                            }
                            for (int i = (size - 6); i < 128; i++) {
                                lastbytes[i] = (byte) 0xff;
                            }
                            String size2str = to2String(128);
                            String oneLine = "";
                            for (int i = 0; i < 128; i++) {
                                String hex = Integer.toHexString(lastbytes[i] & 0xff);
                                if (hex.length() == 1) {
                                    hex = "0" + hex;
                                }
                                oneLine += hex;
                            }
                            oneLine = "6400" + size2str + "46" + oneLine + "20";
                            System.out.println("长度：" + oneLine.length());
                            for (int i = 0; i < ((int) (oneLine.length() / 38)); i++) {
                                String ss = oneLine.substring(i * 38, i * 38 + 38);
                                sendMessage(getMsg(ss, 1));
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            String size2str = to2String(size);
                            String oneLine = "";
                            for (int i = 0; i < size; i++) {
                                String hex = Integer.toHexString(program[programIndex++] & 0xff);
                                if (hex.length() == 1) {
                                    hex = "0" + hex;
                                }
                                oneLine += hex;
                            }
                            oneLine = "6400" + size2str + "46" + oneLine + "20";

                            for (int i = 0; i < ((int) (oneLine.length() / 38)); i++) {
                                String ss = oneLine.substring(i * 38, i * 38 + 38);
                                sendMessage(getMsg(ss, 1));
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (size != 0x80) {
                            sendHexFlag = 0;
                        }

                    }
                    System.out.println("program.length:" + program.length + "    programindex   " + programIndex);
                    //                   downloadVave.setProgress(programIndex * 100 / program.length);
                    myProgressBar.setMax_progress(programIndex * 100 / program.length);
                    testText.setText("" + programIndex * 100 / program.length);
                    break;
                case 11:
                    System.out.println("program index: " + programIndex);
                    System.out.println("leaving programming mode");
                    sendMessage(getMsg("5120", 1));
//                    downloadVave.setProgress(100);
                    myProgressBar.setMax_progress(100);
                    Toast.makeText(MainActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        } else {
            System.out.println(new String(tmp_byte));
        }

//        ChatMsgFmt entity2 = new ChatMsgFmt("Device", tmp, MESSAGE_FROM.OTHERS);
//        chat_list.add(entity2);
//        chat_list_adapter.notifyDataSetChanged();
    }

    private void sendMessage(byte[] sendmsg) {

        if (sendmsg == null)
            return;
        mBluetoothGattCharacteristic.setValue(sendmsg);
        Tools.mBLEService.mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);

        String tmp = "";
        for (int i = 0; i < sendmsg.length; i++) {
            String hex = Integer.toHexString(sendmsg[i] & 0xFF);

            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            tmp += ' ';
            tmp = tmp + hex;
        }
        System.out.println("send    ------------------->" + tmp);                                 //发送调试输出
    }

    class uploadthread extends Thread {
        @Override
        public synchronized void run() {
            super.run();
            System.out.println("program length: " + program.length);
            sendMessage(getMsg("AT+REAST~*&$@!", 0));
            sleepsec(1000);
            sendMessage(getMsg("30203020302030203020", 1));
            responseOk = 4;
        }

    }

    private void sleepsec(int mils) {
        try {
            MainActivity.uploadthread.sleep(mils);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
