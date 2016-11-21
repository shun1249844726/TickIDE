package tickide.lexin.com.tickide;

import android.app.Application;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

/**
 * Created by xushun on 2016/11/21.
 */

public class MyApplication extends Application {
    public final static String DEFAULT_APPKEY = "23538693";

    @Override
    public void onCreate() {
        super.onCreate();
        //建议放在此处做初始化
        FeedbackAPI.init(this, DEFAULT_APPKEY);


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
    }
}
