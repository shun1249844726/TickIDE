package tickide.lexin.com.tickide.BLE;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import tickide.lexin.com.tickide.BLE.mtblesdk.MTBeacon;
import tickide.lexin.com.tickide.BLE.tools.SampleGattAttributes;
import tickide.lexin.com.tickide.BLE.tools.Tools;
import tickide.lexin.com.tickide.MainActivity;
import tickide.lexin.com.tickide.R;
import tickide.lexin.com.tickide.Views.CircleWaveView;


/**
 * Created by xushun on 2016/11/9.
 */

public class ConnectBle extends Activity{
    private BluetoothDevice device;
    private CircleWaveView mCircleWaveView;

    private final static int REQUEST_ENABLE_BT = 2001;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BLEService.LocalBinder binder = (BLEService.LocalBinder) service;
            Tools.mBLEService = binder.getService();
            if (Tools.mBLEService.initBle()) {
                // scanBle(); // 开始扫描设备
                if (!Tools.mBLEService.mBluetoothAdapter.isEnabled()) {
                    final Intent enableBtIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    scanBle(); // 开始扫描设备
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                scanBle(); // 开始扫描设备
            } else {
                // finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_ble);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 11);
            }
        }
        initView();

        bindService(new Intent(this, BLEService.class), connection,
                Context.BIND_AUTO_CREATE);



    }

    // 初始化控件
    private LayoutInflater mInflater;
    private ListView ble_listview;
    private List<MTBeacon> scan_devices = new ArrayList<MTBeacon>();
    private List<MTBeacon> scan_devices_dis = new ArrayList<MTBeacon>();
    private BaseAdapter list_adapter = new BaseAdapter() {

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.devicefmt, null);
            }
            TextView device_name_txt = (TextView) convertView
                    .findViewById(R.id.device_name_txt);
            TextView device_rssi_txt = (TextView) convertView
                    .findViewById(R.id.device_rssi_txt);
            TextView device_mac_txt = (TextView) convertView
                    .findViewById(R.id.device_mac_txt);
            device_name_txt.setText(getItem(position).GetDevice().getName());
            device_mac_txt.setText("Mac: "
                    + getItem(position).GetDevice().getAddress());
            device_rssi_txt.setText("Rssi: "
                    + getItem(position).GetAveragerssi());
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public MTBeacon getItem(int position) {
            return scan_devices_dis.get(position);
        }

        @Override
        public int getCount() {
            return scan_devices_dis.size();
        }
    };

    private void initView() {
        mInflater = LayoutInflater.from(this);
        ble_listview = (ListView) findViewById(R.id.ble_listview);

        ble_listview.setAdapter(list_adapter);
        ble_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // scan_flag = false;
                // Tools.mBLEService.stopscanBle(mLeScanCallback);
                device = scan_devices_dis.get(position).GetDevice();
                setBroadcastReceiver();
                initExpandableListView();
                getDefaultName();
            }


        });

        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        float width = wm.getDefaultDisplay().getWidth();
        float height = wm.getDefaultDisplay().getHeight();
        mCircleWaveView = (CircleWaveView) findViewById(R.id.circle_wave_view);
        mCircleWaveView.setDuration(5000);
        mCircleWaveView.setStyle(Paint.Style.FILL);
        mCircleWaveView.setSpeed(1000);
        mCircleWaveView.setAlpha((float) 0.2);

        mCircleWaveView.setMaxRadius(height);
        mCircleWaveView.setColor(Color.rgb(27,121,254));
        mCircleWaveView.setInterpolator(new LinearOutSlowInInterpolator());
        mCircleWaveView.start();

//        mCircleWaveView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mCircleWaveView.stop();
//            }
//        }, 10000);
    }
    // 设置广播监听
    private BluetoothReceiver bluetoothReceiver = null;

    private void setBroadcastReceiver() {
        // 创建一个IntentFilter对象，将其action指定为BluetoothDevice.ACTION_FOUND
        IntentFilter intentFilter = new IntentFilter(
                BLEService.ACTION_READ_Descriptor_OVER);
        intentFilter.addAction(BLEService.ACTION_ServicesDiscovered_OVER);
        intentFilter.addAction(BLEService.ACTION_STATE_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        bluetoothReceiver = new BluetoothReceiver();
        // 注册广播接收器
        registerReceiver(bluetoothReceiver, intentFilter);
    }
    private class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BLEService.ACTION_READ_Descriptor_OVER.equals(action)) {
                if (BluetoothGatt.GATT_SUCCESS == intent.getIntExtra("value",
                        -1)) {
                    read_name_flag = true;
                }
                return;
            }
            if (BLEService.ACTION_ServicesDiscovered_OVER.equals(action)) {
                connect_flag = true;
                return;
            }
            if (BLEService.ACTION_STATE_CONNECTED.equals(action)) {
//				connect_flag = true;
                return;
            }

            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
//				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(BluetoothDevice.BOND_BONDED == device.getBondState()){
                    Tools.mBLEService.disConectBle();
                    readNameFail.sendEmptyMessageDelayed(0, 200);
                }else if(BluetoothDevice.BOND_BONDING == device.getBondState()){
                    bind_flag = true;
                    System.out.println("正在配对");
                }
                return;
            }
        }
    }

    // 获取名字
    private boolean read_name_flag = false;
    //	private boolean servicesdiscovered_flag = false;
    private boolean connect_flag = false;
    private boolean bind_flag = false;
    private Handler dis_services_handl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            service_list_adapter.notifyDataSetChanged();
            pd.dismiss();
        }
    };
    private Handler readNameFail = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Tools.mBLEService.disConectBle();
            new readNameThread().start();
        }

    };
    private Handler connect_fail_handl = new Handler() {
        public void handleMessage(Message msg) {
            Tools.mBLEService.disConectBle();
            Toast.makeText(getApplicationContext(), "连接失败",
                    Toast.LENGTH_LONG).show();
            finish();
        };
    };

    private ProgressDialog pd;
    private Handler reflashDialogMessage = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            pd.setMessage(b.getString("msg"));
        }
    };
    private void getDefaultName() {
        // 开启一个缓冲对话框
        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setTitle("正在加载...");
        pd.setMessage("正在连接");
        pd.show();
        System.out.println("devices:"+device.getName());
        new readNameThread().start();
    }
    // 初始化控件
    private List<List<BluetoothGattCharacteristic>> mBluetoothGattCharacteristic; // 记录所有特征
    private ExpandableListView service_list_view;
    private SimpleExpandableListAdapter service_list_adapter;
    private List<Map<String, String>> grounps;// 一级条目
    private List<List<Map<String, String>>> childs;// 二级条目
    private void initExpandableListView() {
       // LinearLayout deviceListLL = (LinearLayout) findViewById(R.id.device_listLL);
       // deviceListLL.setVisibility(View.GONE);

        service_list_view = (ExpandableListView) findViewById(R.id.service_list_view);
//        service_list_view.setVisibility(View.VISIBLE);
        grounps = new ArrayList<Map<String, String>>();
        childs = new ArrayList<List<Map<String, String>>>();
        mBluetoothGattCharacteristic = new ArrayList<List<BluetoothGattCharacteristic>>();
        service_list_adapter = new SimpleExpandableListAdapter(this, grounps,
                R.layout.service_grounp_fmt, new String[] { "name", "Uuid" },
                new int[] { R.id.grounpname_txt, R.id.grounp_uuid_txt },
                childs, R.layout.service_child_fmt, new String[] { "name",
                "prov", "uuid" }, new int[] { R.id.childname_txt,
                R.id.prov, R.id.child_uuid_txt });
        service_list_view.setAdapter(service_list_adapter);
        service_list_view.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

//                Intent intent = new Intent(getApplicationContext(),
//                        TalkActivity.class);
//                intent.putExtra("one", groupPosition);
//                intent.putExtra("two", childPosition);
//                startActivityForResult(intent, 0);
                //startActivity(intent);
                return false;
            }
        });
    }
    private boolean exit_activity = false;
    // 读取线程
    private class readNameThread extends Thread {
        @Override
        public void run() {
            super.run();
            Message msg = reflashDialogMessage.obtainMessage();
            Bundle b = new Bundle();
            msg.setData(b);

            try {
                //for (int i = 0; i < 10; i++) {
                while (true) {
                    connect_flag = false;
                    System.out.println("conectBle");
                    if (exit_activity)
                        return;  // 如果已经退出程序，则结束线程
                    Tools.mBLEService.conectBle(device);
                    for (int j = 0; j < 50; j++) {
                        if (connect_flag) {
                            break;
                        }
                        sleep(100);
                    }
                    if (connect_flag) {
                        break;
                    }
                    //if(i>1){
                    //	connect_fail_handl.sendEmptyMessage(0);
                    //	return;
                    //}
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            read_name_flag = false; // 读取设备名
            List<BluetoothGattService> services = Tools.mBLEService.mBluetoothGatt
                    .getServices();

            System.out.println("services.size-->" + services.size());
            if (services.size() == 0) {
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    readNameFail.sendEmptyMessage(0);
                }
                return;
            }

            String uuid;
            b.putString("msg", "读取通道信息");
            reflashDialogMessage.sendMessage(msg);
            grounps.clear();
            childs.clear();
            for (BluetoothGattService service : services) {
                uuid = service.getUuid().toString();
                List<BluetoothGattCharacteristic> gattCharacteristics = service
                        .getCharacteristics();
                if (gattCharacteristics.size() == 0) {
                    continue;
                }
                // 添加一个一级目录
                Map<String, String> grounp = new HashMap<String, String>();
                grounp.put("name",
                        SampleGattAttributes.lookup(uuid, "unknow"));
                grounp.put("Uuid", uuid);
                grounps.add(grounp);
                List<BluetoothGattCharacteristic> grounpCharacteristic = new ArrayList<BluetoothGattCharacteristic>();

                List<Map<String, String>> child = new ArrayList<Map<String, String>>();

                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    Map<String, String> child_data = new HashMap<String, String>(); // 添加一个二级条目
                    uuid = gattCharacteristic.getUuid().toString();
                    BluetoothGattDescriptor descriptor = gattCharacteristic
                            .getDescriptor(UUID
                                    .fromString("00002901-0000-1000-8000-00805f9b34fb"));
                    if (null != descriptor) {
                        read_name_flag = false;
                        Tools.mBLEService.mBluetoothGatt
                                .readDescriptor(descriptor);
                        while (!read_name_flag) {// 等待读取完成
                            if (exit_activity || bind_flag) {
                                bind_flag = false;
                                System.out.println("read fail");
                                return; // 读取超时，结束线程
                            }
                        }
                        try {
                            child_data.put("name",
                                    new String(descriptor.getValue(),
                                            "GB2312"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        child_data
                                .put("name", SampleGattAttributes.lookup(
                                        uuid, "unknow"));
                    }

                    String pro = "";
                    if (0 != (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ)) { // 可读
                        pro += "可读,";
                    }
                    if ((0 != (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE)) ||
                            (0 != (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE))) { // 可写
                        pro += "可写,";
                    }
                    if ((0 != (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY)) ||
                            (0 != (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE))) { // 通知
                        pro += "可通知";
                    }
                    child_data.put("prov", pro);
                    child_data.put("uuid", uuid);
                    child.add(child_data);
                    grounpCharacteristic.add(gattCharacteristic);
                }
                childs.add(child); // 一个一级条目添加完成
                mBluetoothGattCharacteristic.add(grounpCharacteristic);
            }
            Intent intent = new Intent(ConnectBle.this,MainActivity.class);
            startActivity(intent);
            dis_services_handl.sendEmptyMessage(0);
        }
    }
    // 开始扫描
    private int scan_timer_select = 0;
    private boolean scan_flag = true;
    private Handler search_timer = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            search_timer.sendEmptyMessageDelayed(0, 500);

            if (!scan_flag) {
                return;
            }

            if (!Tools.mBLEService.mBluetoothAdapter.isEnabled()) {
                return;
            }

            // 扫描时间调度
            switch (scan_timer_select) {
                case 1: // 开始扫描
                    Tools.mBLEService.scanBle(mLeScanCallback);
                    break;
                case 3: // 停止扫描(结算)
                    Tools.mBLEService.stopscanBle(mLeScanCallback); // 停止扫描

                    for (int i = 0; i < scan_devices.size();) { // 防抖
                        if (scan_devices.get(i).CheckSearchcount() > 2) {
                            scan_devices.remove(i);
                        } else {
                            i++;
                        }
                    }
                    scan_devices_dis.clear(); // 显示出来
                    for (MTBeacon device : scan_devices) {
                        scan_devices_dis.add(device);
                    }
                    list_adapter.notifyDataSetChanged();

                    break;

                default:
                    break;
            }
            scan_timer_select = (scan_timer_select + 1) % 4;
        }

    };
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            int i = 0;
            // 检查是否是搜索过的设备，并且更新
            for (i = 0; i < scan_devices.size(); i++) {
                if (0 == device.getAddress().compareTo(
                        scan_devices.get(i).GetDevice().getAddress())) {
                    scan_devices.get(i).ReflashInf(device, rssi, scanRecord); // 更新信息
                    return;
                }
            }

            // 增加新设备
            scan_devices.add(new MTBeacon(device, rssi, scanRecord));
        }
    };

    private void scanBle() {
        search_timer.sendEmptyMessageDelayed(0, 500);
    }



    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause");

        scan_flag = false;
        Tools.mBLEService.stopscanBle(mLeScanCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onresume");
        scan_flag = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("ondestroy");

        unbindService(connection);
    }
}
