package tickide.lexin.com.tickide.User;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import tickide.lexin.com.tickide.R;

import static tickide.lexin.com.tickide.User.Content.getUserListUrl;
import static tickide.lexin.com.tickide.User.Content.userUpdateUrl;

/**
 * Created by xushun on 16/7/20.
 */

public class ForgetPsd extends AppCompatActivity implements View.OnClickListener {
    private EditText usernameEdt, passwordEdt, surepasswordEdt, emailEdt, telEdt;
    private Button sureBtn;
    String uname = "";
    String passwd = "";
    String surepasswd = "";
    String email = "";
    String tel = "";
    String id = "";
    Context context;
    HttpURLConnection getUserListConn = null;
    HttpURLConnection userUpdateConn = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_forgetpassword);
        findViewsById();
        sureBtn.setOnClickListener(this);
    }

    private void findViewsById() {
        usernameEdt = (EditText) findViewById(R.id.forget_usernameId);
        passwordEdt = (EditText) findViewById(R.id.forget_pswId);
        surepasswordEdt = (EditText) findViewById(R.id.forget_psw_sureId);
        emailEdt = (EditText) findViewById(R.id.forget_emallId);
        telEdt = (EditText) findViewById(R.id.forget_telId);
        sureBtn = (Button) findViewById(R.id.forget_sure_BtnId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forget_sure_BtnId:
                uname = usernameEdt.getText().toString();
                passwd = passwordEdt.getText().toString();
                surepasswd = surepasswordEdt.getText().toString();
                email = emailEdt.getText().toString();
                tel = telEdt.getText().toString();
                if (!uname.equals("")
                        && !passwd.equals("")
                        && !surepasswd.equals("")
                        && !email.equals("")
                        && !tel.equals("")) {
                    if (passwd.equals(surepasswd)) {
                        String[] params = {uname};
                        new getUserList().execute(params);

                    } else {
                        Toast.makeText(context, "两次输入的密码不一致!", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(context, "请填写完整信息!", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    class getUserList extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL getallcoverurl = new URL(getUserListUrl);
                getUserListConn = (HttpURLConnection) getallcoverurl.openConnection();
                getUserListConn.setDoInput(true);
                getUserListConn.setDoOutput(true);
                getUserListConn.setRequestMethod("POST");
                getUserListConn.setUseCaches(false);
                getUserListConn.connect();
                //DataOutputStream流
                DataOutputStream out = new DataOutputStream(getUserListConn.getOutputStream());
                //要上传的参数
                //将要上传的内容写入流中
                StringBuffer bufferparams = new StringBuffer();
                // 表单参数与get形式一样
                bufferparams.append("uname=").append(params[0]);
                byte[] bypes = bufferparams.toString().getBytes();
                getUserListConn.getOutputStream().write(bypes);// 输入参数
                //刷新、关闭
                out.flush();
                out.close();

                DataInput di = new DataInputStream(getUserListConn.getInputStream());
                String returnedString = di.readLine().toString();
                System.out.println("returnedString--->" + returnedString);
                String error_code = JSON.parseObject(returnedString).get("err_code").toString();
                System.out.println("error_code:" + error_code);

                if (error_code .equals("0") ) {
                    String data = JSON.parseObject(returnedString).get("data").toString();
                    String items = JSON.parseObject(data).get("items").toString();
                    JSONObject itemsObj = JSON.parseObject(JSON.parseArray(items).get(0).toString());

                    id = itemsObj.get("id").toString();
                    String returnedEmail = itemsObj.get("email").toString();
                    String returnedTel = itemsObj.get("tel").toString();
                    if (email.equals(returnedEmail) && tel.equals(returnedTel)) {
                        return "ok";
                    } else {
                        return "信息有误";
                    }
                } else {
                    return "请检查输入信息";
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            switch (s) {
                case "ok":
                    String[] uptateparams = {id, uname, passwd, email, tel};
                    System.out.println("uptateparams:"+tel);
                    new changeMessage().execute(uptateparams);
                    break;
                default:
                    Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    class changeMessage extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL updateUrl = new URL(userUpdateUrl);
                userUpdateConn = (HttpURLConnection) updateUrl.openConnection();
                userUpdateConn.setDoInput(true);
                userUpdateConn.setDoOutput(true);
                userUpdateConn.setRequestMethod("POST");
                userUpdateConn.setUseCaches(false);
                userUpdateConn.connect();
                //DataOutputStream流
                DataOutputStream out = new DataOutputStream(userUpdateConn.getOutputStream());
                //要上传的参数
                //将要上传的内容写入流中
                StringBuffer bufferparams = new StringBuffer();
                // 表单参数与get形式一样
                bufferparams.append("id=").append(params[0]).append("&");
                bufferparams.append("uname=").append(params[1]).append("&");
                bufferparams.append("passwd=").append(params[2]).append("&");
                bufferparams.append("email=").append(params[3]).append("&");
                bufferparams.append("tel=").append(params[4]);
                byte[] bypes = bufferparams.toString().getBytes();
                userUpdateConn.getOutputStream().write(bypes);// 输入参数
                //刷新、关闭
                out.flush();
                out.close();

                DataInput di = new DataInputStream(userUpdateConn.getInputStream());
                String returnedString = di.readLine().toString();
                System.out.println("returnedString_1--->" + returnedString);
                String error_code = JSON.parseObject(returnedString).get("err_code").toString();
                System.out.println("error_code_1:" + error_code);
                if (error_code.equals("0")){
                    return "修改成功!";
                }else {
                    return error_code;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
            Toast.makeText(context,s, Toast.LENGTH_SHORT).show();
        }
    }
}
