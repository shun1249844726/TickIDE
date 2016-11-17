package tickide.lexin.com.tickide.BLE;

import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import tickide.lexin.com.tickide.BLE.tools.Tools;
import tickide.lexin.com.tickide.R;
import tickide.lexin.com.tickide.Utils.CustomToast;
import tickide.lexin.com.tickide.Views.WaveView;

/**
 * Created by xushun on 2016/11/9.
 */

public class UploadProgram extends AppCompatActivity {
    private BluetoothGattCharacteristic mBluetoothGattCharacteristic;
    // 设置广播监听
    private BroadcastReceiver bluetoothReceiver;
    private int read_fmt_int = 1; // 接收数据格式
    WaveView downloadVave;
    private Button uploadBtn;
    private int responseOk = 0;
    private int sendHexFlag = 0;
    int size = 0;
    int address = 0;
    int programIndex = 0;
    byte[] program;
    private int proper = 0; // 通道权限


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_program);
        downloadVave = (WaveView) findViewById(R.id.wave_view);
        uploadBtn = (Button) findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Tools.mBLEService.isConnected()) {
                    Toast.makeText(UploadProgram.this, "未连接设备", Toast.LENGTH_SHORT).show();
                } else {
                    responseOk = 0;
                    sendHexFlag = 0;
                    size = 0;
                    address = 0;
                    programIndex = 0;
                    new uploadthread().start();
                }
            }
        });
        program = readProgram();
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

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.upload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.debug_mode) {
            if (Tools.mBLEService.isConnected()) {
                Intent intent1 = new Intent(UploadProgram.this, TalkActivity.class);
                intent1.putExtra("one", 3);
                intent1.putExtra("two", 5);
                startActivity(intent1);
            } else {
//                Intent intent = new Intent(UploadProgram.this, ScanActivity.class);
//                startActivityForResult(intent, 0);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
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
        bluetoothReceiver = new BroadcastReceiver();

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
                CustomToast.showToast(UploadProgram.this, "已经连接", Toast.LENGTH_SHORT);
            }
            if (BLEService.ACTION_STATE_DISCONNECTED.equals(action)) {
                Tools.mBLEService.disConectBle();
                CustomToast.showToast(UploadProgram.this, "已断开连接", Toast.LENGTH_SHORT);
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
                        String addressPackage = "";
                        addressPackage = "55" + to2String(laddress) + to2String(haddress) + "20";
                        sendMessage(getMsg(addressPackage, 1));
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
                    System.out.println("program.length:"+program.length + "    programindex   "+ programIndex);
                    downloadVave.setProgress(programIndex * 100 / program.length);
                    break;
                case 11:
                    System.out.println("program index: " + programIndex);
                    System.out.println("leaving programming mode");
                    sendMessage(getMsg("5120", 1));
                    downloadVave.setProgress(100);
                    Toast.makeText(UploadProgram.this, "下载成功", Toast.LENGTH_SHORT).show();
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
    private byte[] getMsg(String msg, int typeFlag) {

        byte[] write_msg_byte = null;
        byte[] tmp_byte = null;
        if (0 == msg.length())
            return null;
        switch (typeFlag) {
            case 0:
                write_msg_byte = msg.getBytes();
                break;
            case 1:
                if (msg.length() == 1) {
                    msg = "0" + msg;
                }
                tmp_byte = msg.getBytes();
                write_msg_byte = new byte[tmp_byte.length / 2 + tmp_byte.length % 2];
                for (int i = 0; i < tmp_byte.length; i++) {
                    if ((tmp_byte[i] <= '9') && (tmp_byte[i] >= '0')) {
                        if (0 == (i % 2))
                            write_msg_byte[i / 2] = (byte) (((tmp_byte[i] - '0') * 16) & 0xFF);
                        else
                            write_msg_byte[i / 2] |= (byte) ((tmp_byte[i] - '0') & 0xFF);
                    } else {
                        if (0 == i % 2)
                            write_msg_byte[i / 2] = (byte) (((tmp_byte[i] - 'a' + 10) * 16) & 0xFF);
                        else
                            write_msg_byte[i / 2] |= (byte) ((tmp_byte[i] - 'a' + 10) & 0xFF);
                    }
                }
                break;
            default:
                break;
        }
        return write_msg_byte;
    }
    private static byte[] readProgram() {
        FileInputStream fis = null;
        File file = new File("/mnt/sdcard/firmware.hex");

        // every line, except last one, has has 45 bytes (including \r\n)
        int programLines = (int) Math.ceil(file.length() / 45.0);
        // every line has 32 bytes of program data (excluding checksums, addresses, etc.)
        int unusedBytes = 45 - 32;
        // calculate program length according to program lines and unused bytes
        int programLength = (int) file.length() - (programLines * unusedBytes);
        // the actualy program data is half the size, as the hex file represents hex data in individual chars
        programLength /= 2;
        // create a byte array with the program length
        byte[] program = new byte[programLength];

        try {
            // open the file stream
            Log.d("d", "opening hex file");
            fis = new FileInputStream(file);
            Log.d("d", "Total program size (in bytes) : " + programLength);
            Log.d("d", "Total file size to read (in bytes) : " + fis.available());
            int content;
            int lineIndex = 0;
            int lineNumber = 1;
            int programIndex_read = 0;
            char[] line = new char[45];
            // read the file byte by byte
            while ((content = fis.read()) != -1) {
                // append byte to the line
                line[lineIndex++] = (char) content;
                // when the line is complete
                if (content == 10) {
                    // take only the actual program data form the line
                    for (int index = 9; index < lineIndex - 4; index += 2) {
                        // convert hexadecimals represented as chars into bytes
                        program[programIndex_read++] = Integer.decode("0x" + line[index] + line[index + 1]).byteValue();
                    }
                    // start a new line
                    lineIndex = 0;
                }
            }
        } catch (IOException e) {
            Log.d("d", "reading hex failed: " + e.getMessage());
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return program;
    }
    private void sleepsec(int mils) {
        try {
            uploadthread.sleep(mils);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String to2String(int input) {
        String temp = "";
        if (input < 10) {
            temp = ("0" + String.valueOf(Integer.toHexString(input & 0xff)));
        } else {
            temp = String.valueOf(Integer.toHexString(input & 0xff));
        }
        return temp;
    }
}
