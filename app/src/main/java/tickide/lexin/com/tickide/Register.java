package tickide.lexin.com.tickide;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static tickide.lexin.com.tickide.Content.registerUrl;

/**
 * Created by xushun on 16/7/1.
 */
public class Register extends AppCompatActivity implements View.OnClickListener {
    private Button registerBtn;
    private EditText userNameEdt, pswEdt, psw_sureEdt, emailEdt, telEdt;
    private String uname = "", passwd = "", sure_passwd = "", email = "", tel = "";
    private Context content;
    HttpURLConnection registerurlConn= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        content = this;
        userNameEdt = (EditText) findViewById(R.id.register_usernameId);
        pswEdt = (EditText) findViewById(R.id.register_pswId);
        psw_sureEdt = (EditText) findViewById(R.id.register_psw_sureId);
        emailEdt = (EditText) findViewById(R.id.register_emallId);
        telEdt = (EditText) findViewById(R.id.register_telId);
        registerBtn = (Button) findViewById(R.id.registerBtnId);
        registerBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.registerBtnId) {
            uname = userNameEdt.getText().toString();
            passwd = pswEdt.getText().toString();
            sure_passwd = psw_sureEdt.getText().toString();
            email = emailEdt.getText().toString();
            tel = telEdt.getText().toString();
            System.out.println("uname:" + uname + "  passwd:" + passwd + "  sure_passwd:" + sure_passwd
                    + "  email:" + email + "  tel:" + tel);
//            if (!uname.equals("") && !passwd.equals("") && !sure_passwd.equals("") && !email.equals("") && !tel.equals("") && !passwd.equals(sure_passwd)) {
//
//            }
            if (uname.equals("") || passwd.equals("") || sure_passwd.equals("") || email.equals("") || tel.equals("")) {
                Toast.makeText(content, "请填写完整信息！", Toast.LENGTH_SHORT).show();

            } else if (!passwd.equals(sure_passwd)) {
                Toast.makeText(content, "两次密码输入不一致！", Toast.LENGTH_SHORT).show();
            }
            else{
                System.out.println("registerTask--->" );

                String[] params = {uname,passwd,email,tel,"0"};
                new registerTask().execute(params);
            }
        }
    }

    class registerTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL registerurl = new URL(registerUrl);
                registerurlConn = (HttpURLConnection) registerurl.openConnection();
                registerurlConn.setDoInput(true);
                registerurlConn.setDoOutput(true);
                registerurlConn.setRequestMethod("POST");
                registerurlConn.setUseCaches(false);
                registerurlConn.connect();
                //DataOutputStream流
                DataOutputStream out = new DataOutputStream(registerurlConn.getOutputStream());
                //要上传的参数
                //将要上传的内容写入流中
                StringBuffer bufferparams = new StringBuffer();

                // 表单参数与get形式一样
                bufferparams.append("uname=").append(params[0]).append("&")
                        .append("passwd=").append(params[1]).append("&")
                        .append("email=").append(params[2]).append("&")
                        .append("tel=").append(params[3]).append("&")
                        .append("level=").append(params[4]);
                byte[] bypes = bufferparams.toString().getBytes();
                registerurlConn.getOutputStream().write(bypes);// 输入参数
                //刷新、关闭
                out.flush();
                out.close();
                DataInput di = new DataInputStream(registerurlConn.getInputStream());
                String returnedString = di.readLine().toString();
                System.out.println("returnedString--->" + returnedString);
                String error_code = JSON.parseObject(returnedString).get("err_code").toString();
                System.out.println("error_code:"+error_code);

                switch (error_code){
                    case "0":  //状态码  0:成功
                        return "注册成功";
                    case "10004": //状态码  10004:被注册过
                        return "该用户名已经注册过了";
                    default:
                        return "请检查信息是否正确";
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "...";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("注册成功")) {
                registerurlConn.disconnect();
                Intent intent1 = new Intent(Register.this,LoginActivity.class);
                startActivity(intent1);

            }else{
                System.out.println("onPostExecute:" + s);
                Toast.makeText(content,s, Toast.LENGTH_SHORT).show();
            }

        }
    }
}
