package tickide.lexin.com.tickide;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Created by xushun on 16/7/1.
 */
public class LoginOrRegister extends AppCompatActivity implements View.OnClickListener {
    private Button gotoLoginBtn, gotoRegisterBtn;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        getSupportActionBar().hide();
        preferences = getSharedPreferences("USERINFO", MODE_PRIVATE);
        editor = preferences.edit();
        if (!preferences.getString("USERNAME", "").toString().equals("")) {
            Intent intent = new Intent(LoginOrRegister.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_loginorregister);
            gotoLoginBtn = (Button) findViewById(R.id.gotoLoginId);
            gotoLoginBtn.setOnClickListener(this);
            gotoRegisterBtn = (Button) findViewById(R.id.gotoregeistId);
            gotoRegisterBtn.setOnClickListener(this);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gotoLoginId:
                Intent intent = new Intent(LoginOrRegister.this, LoginActivity.class);
                startActivity(intent);

                break;
            case R.id.gotoregeistId:
                Intent intent1 = new Intent(LoginOrRegister.this, Register.class);
                startActivity(intent1);
                break;
            default:
                break;
        }
    }
}
