package tickide.lexin.com.tickide.User;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tickide.lexin.com.tickide.MainActivity;
import tickide.lexin.com.tickide.R;

import static tickide.lexin.com.tickide.User.Content.COOKIE;
import static tickide.lexin.com.tickide.User.Content.TOKEN;
import static tickide.lexin.com.tickide.User.Content.USER_NAME;
import static tickide.lexin.com.tickide.User.Content.getTokenUrl;


/**
 * Created by xushun on 16/6/27.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String resultData = "";
    HttpURLConnection urlConn = null;
    private EditText loginEditText, pswEditText;
    private TextView regetpsdTv;
    private Button loginBtn;
    private Context context;
    public static final int SHOW_TOAST = 0;
    private LinearLayout loginLL;
    private int AUTO_LOGIN_FLAG = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        preferences = getSharedPreferences("USERINFO", MODE_PRIVATE);
        editor = preferences.edit();
        if (!preferences.getString("USERNAME", "").toString().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("正在登录....");
            builder.setMessage("请稍后！");
            builder.create().show();
            loginIn(preferences.getString("USERNAME", ""), preferences.getString("PASSWORD", ""));
            AUTO_LOGIN_FLAG = 1;
        } else {
            setContentView(R.layout.activity_login);
            loginEditText = (EditText) findViewById(R.id.loginEditTextId);
            regetpsdTv = (TextView) findViewById(R.id.regetpsdTvid);
            regetpsdTv.setOnClickListener(this);
            pswEditText = (EditText) findViewById(R.id.pswEditTextId);
            loginBtn = (Button) findViewById(R.id.bnLogin);
            loginBtn.setOnClickListener(this);
            loginLL = (LinearLayout) findViewById(R.id.loginLLId);

        }
//        PushAgent mPushAgent = PushAgent.getInstance(this);
//        mPushAgent.enable();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bnLogin:
                loginIn(loginEditText.getText().toString(), pswEditText.getText().toString());
                break;
            case R.id.regetpsdTvid:
                Intent intent = new Intent(LoginActivity.this,ForgetPsd.class);
                startActivity(intent);
                break;
        }

    }

    private void HttpUrlConnection_Post(String un, String ps) {
        try {
            URL gettokenurl = new URL(getTokenUrl);
            urlConn = (HttpURLConnection) gettokenurl.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("POST");
            urlConn.setUseCaches(false);
//            urlConn.setRequestProperty("uname",loginEditText.getText().toString());
//            urlConn.setRequestProperty("passwd",pswEditText.getText().toString());
            // 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
            // 要注意的是connection.getOutputStream会隐含的进行connect。
            urlConn.connect();
            //DataOutputStream流
            DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());
            //要上传的参数
            String content_0 = "uname=" + un;
            String content_1 = "passwd=" + ps;
            //将要上传的内容写入流中
            StringBuffer params = new StringBuffer();
            // 表单参数与get形式一样
            params.append(content_0).append("&").append(content_1);
            byte[] bypes = params.toString().getBytes();
            urlConn.getOutputStream().write(bypes);// 输入参数
            //刷新、关闭
            out.flush();
            out.close();
            Map<String, List<String>> map = urlConn.getHeaderFields();
            Set<String> set = map.keySet();
            for (Iterator iterator = set.iterator(); iterator.hasNext(); ) {
                String key = (String) iterator.next();
                if (key == null) {

                } else if (key.equals("Set-Cookie")) {
                    System.out.println("key=" + key + ",开始获取cookie");
                    List<String> list = map.get(key);
                    StringBuilder builder = new StringBuilder();
                    for (String str : list) {
                        builder.append(str).toString();
                    }
                    COOKIE = builder.toString();
                    COOKIE = COOKIE.split("=")[1].split(";")[0];
                    System.out.println("cookie=" + COOKIE);
                }
            }


            DataInput di = new DataInputStream(urlConn.getInputStream());
            String returnedString = di.readLine().toString();
//            System.out.println("returnedString--->" + returnedString+"!");
            if (returnedString != "" && returnedString.contains("success")) {
                String dataString = JSON.parseObject(returnedString).get("data").toString();
                //               System.out.println("dataString---->" + dataString);
//                String token = "";
                try {
                    //                   token = JSON.parseObject(dataString).getString("token");
                    Map<String, String> list = JSON.parseObject(dataString, new TypeReference<Map<String, String>>() {
                    });
                    TOKEN = list.get("token");
                    USER_NAME = un;

                    //存储到sharedPreference中

                    editor.clear();
                    editor.putString("USERNAME", USER_NAME);
                    editor.putString("PASSWORD", ps);
                    editor.commit();

//                    System.out.println("token---->" + TOKEN);
                    if (AUTO_LOGIN_FLAG == 1) {
                        Thread.currentThread().sleep(800);
                    }
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                //     List<Map<String,String>> data =
                //               System.out.println("json---->" + JSON.parseObject(returnedString).getString("err_msg"));

            } else {
                if (returnedString != "" && returnedString.contains("BAD")) {
//                    System.out.println("登录失败");
                    Message message = new Message();
                    message.what = SHOW_TOAST;
                    message.obj = resultData;
                    handler.sendMessage(message);
                }
            }
        } catch (Exception e) {
            resultData = "连接超时+0";

            Message message = new Message();
            message.what = SHOW_TOAST;
            message.obj = resultData;
            handler.sendMessage(message);

            e.printStackTrace();


        }
    }

    private void loginIn(final String un, final String ps) {
        new Thread() {
            public void run() {
                try {
                    HttpUrlConnection_Post(un, ps);

                } catch (Exception e) {
                    resultData = "连接超时+1";
                    e.printStackTrace();
                } finally {
                    try {
                        urlConn.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String m = (String) msg.obj;
            Toast.makeText(context, "登录失败！请检查账号信息！", Toast.LENGTH_SHORT).show();
        }
    };
}


