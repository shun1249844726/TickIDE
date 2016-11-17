package tickide.lexin.com.tickide.BLE.mtblesdk;

import android.bluetooth.BluetoothDevice;

public class MTBeacon {
	private BluetoothDevice device;
	private int averagerssi = 0;
	private int rssi;
	private byte[] scanRecord;
	private int search_count = 0;

	private int major;
	private int minor;
	private int Txpower;
	private String UUID;

	private int Ibeancon_Offset = 0; // 标准偏移
	private int MT_inf_offset = 0; // 馒头信息偏移

	public MTBeacon(BluetoothDevice device, int rssi, byte[] scanRecord) {
		this.device = device;
		this.rssi = rssi;
		this.scanRecord = scanRecord;
		this.averagerssi = rssi;

//		GetOffset(this.scanRecord); // 获取偏移信息
	}

	// 获取设备
	public BluetoothDevice GetDevice() {
		return device;
	}

	// 防抖
	public int CheckSearchcount() {
		search_count++;
		return search_count;
	}

	// 更新信息
	public boolean ReflashInf(BluetoothDevice device, int rssi,
							  byte[] scanRecord) {
		this.device = device;
		this.rssi = rssi;
		this.scanRecord = scanRecord;

		averagerssi = (averagerssi + rssi) / 2;

		search_count = 0; // 防抖

//		GetOffset(this.scanRecord);

		return true;
	}

	// major、minor、TXpower、UUID、电量信息
	public int GetMajor() {
		return major;
	}

	public int GetMinor() {
		return minor;
	}

	public int GetTxpower() {
		return Txpower;
	}

	public String GetUUID() {
		return UUID;
	}

	public int GetEnergy(){
		return scanRecord[MT_inf_offset+3];
	}

	// 获取rssi值
	public int GetCurrentRssi() {
		return rssi;
	}

	// 获取rssi防抖值(平均值)
	public int GetAveragerssi() {
		return averagerssi;
	}

	// 获取当前距离
	public double GetCurrentDistance() {
		return CalculateDistance(rssi);
	}

	// 获取平均距离
	public double GetAveragerDistance() {
		return CalculateDistance(averagerssi);
	}

	/************************* 辅助函数 *******************************/
	// 计算距离
	private double CalculateDistance(int rssi) {
		double distance = 0;
		double ratio = rssi * 1.0 / Txpower; // 计算距离
		if (ratio < 1.0) {
			distance = Math.pow(ratio, 10);
		} else {
			distance = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
		}

		return distance;
	}

	// 获取标准偏移量
	private void GetOffset(byte[] scanRecord) {
		for (int i = 0; i < scanRecord.length;) {
			if ((scanRecord[i] == 26) && (scanRecord[i + 1] == -1)
					&& (scanRecord[i + 2] == 76) && (scanRecord[i + 3] == 0)
					&& (scanRecord[i + 4] == 2) && (scanRecord[i + 5] == 21)) {
				Ibeancon_Offset = i;

				major = ((0xFF & scanRecord[i + 22]) * 256 + (0xFF & scanRecord[i + 23]));
				minor = ((0xFF & scanRecord[i + 24]) * 256 + (0xFF & scanRecord[i + 25]));
				Txpower = scanRecord[i + 26];

				UUID = "";
				for (int j = i + 6; j < i + 22; j++) { // uuid
					String hex = Integer.toHexString(scanRecord[j] & 0xFF);
					if (hex.length() == 1) {
						hex = '0' + hex;
					}
					if ((j == (i + 10)) || (j == (i + 12)) || (j == (i + 14))
							|| (j == (i + 16)))
						UUID += '-';
					UUID += hex;
				}
				UUID = UUID.toUpperCase();

			}

			if ((scanRecord[i] == 3) && (scanRecord[i + 1] == -86)) {
				MT_inf_offset = i;
			}

			i += (scanRecord[i] + 1);
			if ((i >= (scanRecord.length)) || (0x00 == scanRecord[i])) {
				break;
			}
		}

	}

}
