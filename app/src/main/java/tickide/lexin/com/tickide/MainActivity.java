package tickide.lexin.com.tickide;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import tickide.lexin.com.tickide.BLE.ConnectBle;
import tickide.lexin.com.tickide.BLE.UploadProgram;
import tickide.lexin.com.tickide.DataApi.ReturnHex;
import tickide.lexin.com.tickide.Utils.FileUtils;
import tickide.lexin.com.tickide.Views.MyProgressBar;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private PushAgent mPushAgent;
    Context context;
    private WebView webView;
    HttpURLConnection submitCodeConn = null;
    private String CompileUrl = "http://180.76.183.13/compiler/youMustChangeThis/v1";
    private int val = 0;
    MyProgressBar myProgressBar;




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
                val = val + 10;



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
        myProgressBar.setMax_progress(0);

    }

    @JavascriptInterface
    public void hello(String data) {//对应js中xxx.hello("")
        data = data.replace("\n", "\\n") + "\\n";
        data = data.replace("\"", "\\\"");
        data = data.replace("\r", "\\n");

        System.out.println("code:" + data);
        submitCode(data);
    }

    private void submitCode(final String code) {

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
                            "}\n" +
                            "],\n" +
                            "\"libraries\":\n" +
                            "{\n" +
                            "},\n" +
                            "\"format\":\"hex\",\n" +
                            "\"version\":\"105\",\n" +
                            "\"build\":{\n" +
                            "\"mcu\":\"atmega328p\",\n" +
                            "\"f_cpu\":\"16000000L\",\n" +
                            "\"core\":\"arduino\",\n" +
                            "\"variant\":\"standard\"\n" +
                            "}\n" +
                            "}");
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
                        ReturnHex returnHex = JSONObject.parseObject(returnedString, ReturnHex.class);
                        System.out.println("returnHex" + returnHex.getOutput());

                        String filePath = "/mnt/sdcard/";
                        String fileName = "firmware.hex";
                        FileUtils.writeTxtToFile(returnHex.getOutput(), filePath, fileName);
                        Intent intent = new Intent(MainActivity.this, UploadProgram.class);
                        startActivity(intent);
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

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
}
