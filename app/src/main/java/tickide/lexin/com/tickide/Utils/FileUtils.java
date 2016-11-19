package tickide.lexin.com.tickide.Utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import tickide.lexin.com.tickide.BLE.UploadProgram;

/**
 * Created by xushun on 2016/11/14.
 */

public class FileUtils {

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e+"");
        }
    }
    // 生成文件
    public static File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
    // 将字符串写入到文本文件中
    public static void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath+fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }else {
                file.delete();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }
    public static byte[] getMsg(String msg, int typeFlag) {

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
    public static byte[] readProgram() {
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


    public  static  String to2String(int input) {
        String twoSit = "";
        if (input < 16) {
            twoSit = ("0" + String.valueOf(Integer.toHexString(input & 0xff)));
        } else {
            twoSit = String.valueOf(Integer.toHexString(input & 0xff));
        }
        return twoSit;
    }

}
