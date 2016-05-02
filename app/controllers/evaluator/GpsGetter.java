package controllers.evaluator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

class GpsGetter extends Thread {
	String addr;	// 待解析的地址
	int p;			// 地址列表中的位置
	static double[][] GpsList;
	
	// 初始化GpsList
	public static void init(String[] addrList) {
		int addrLen = addrList.length;
		GpsList = new double[addrLen][2];
	}
	
	public GpsGetter(String addr, int p) {
		this.addr = addr;
		this.p = p;
	}
	
	// 根据待解析的地址获取经纬度
	@Override
	public void run() {
		String urlString = "http://api.map.baidu.com/geocoder/v2/?address=" + addr + "&output=json&ak=" + MultiThreadWeb.ak[p % MultiThreadWeb.akl];
		String result = MultiThreadWeb.getFromUrl(urlString);
		JSONObject jo = JSON.parseObject(result);
		int status = jo.getInteger("status");
		if (status != 0) {
			System.out.println("position: " + addr);
			System.out.println("Can't find this address!");
		}
		double lat, lon;
		lat = jo.getJSONObject("result").getJSONObject("location").getDouble("lat");
		lon = jo.getJSONObject("result").getJSONObject("location").getDouble("lng");
		GpsList[p][0] = lat;
		GpsList[p][1] = lon;
	}
}