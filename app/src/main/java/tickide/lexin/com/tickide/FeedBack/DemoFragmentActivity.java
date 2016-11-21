package tickide.lexin.com.tickide.FeedBack;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;

import tickide.lexin.com.tickide.R;

/**
 * Created by wuer on 16/9/21.
 */
public class DemoFragmentActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FeedbackAPI.activity = this; // 设置activity
        //support-v4包中的Fragment
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        Fragment feedback = FeedbackAPI.getFeedbackFragment();
        transaction.replace(R.id.content, feedback);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FeedbackAPI.cleanFeedbackFragment();
        FeedbackAPI.cleanActivity();
    }
}
