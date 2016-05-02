package controllers.evaluator;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;

class DistTimeGetter extends Thread {
	static double[][] TimeMatrix;		// 时间矩阵
	static double[][] DistMatrix;		// 距离矩阵
	
	ArrayList<Integer> origins;			// 起点序列
	ArrayList<Integer> destinations;	// 终点序列
	int lenOrigin;						// 起点个数
	int lenDestination;					// 终点个数
	String urlString;					// 请求的url
	
	// 初始化时间距离矩阵
	public static void init(String[] addrList) {
		int addrLen = addrList.length;
		TimeMatrix = new double[addrLen][addrLen];
		DistMatrix = new double[addrLen][addrLen];
	}
	
	public static void initInsert(String[] addrList) {
		int addrLen = addrList.length;
		TimeMatrix = new double[2][addrLen];
		DistMatrix = new double[2][addrLen];
	}
	
	public DistTimeGetter(String urlString, ArrayList<Integer> origins, ArrayList<Integer> destinations) {
		this.urlString = urlString;
		this.origins = origins;
		this.destinations = destinations;
		this.lenOrigin = origins.size();
		this.lenDestination = destinations.size();
	}
	
	// type = 0: requestAddrs作为目的地
	// type = 1: requestAddrs作为起始地(但仍将其赋值给destinations)
	public DistTimeGetter(String urlString, ArrayList<Integer> requestAddrs, int type) {
		this.urlString = urlString;
		this.destinations = requestAddrs;
		this.lenDestination = requestAddrs.size();
		if (type == 0) {
			// 所有的值都写在第一行
			this.origins = new ArrayList<Integer>();
			this.origins.add(0);
		}
		if (type == 1) {
			// 所有的值都写在第二行
			this.origins = new ArrayList<Integer>();
			this.origins.add(1);
		}
		this.lenOrigin = 1;
	}
	
	public void run() {
		String result = MultiThreadWeb.getFromUrl(urlString);
		JSONObject jo = JSON.parseObject(result);
		if (jo != null) {
			JSONObject jr = jo.getJSONObject("result");
			if (jr != null) {
				JSONArray ja = jr.getJSONArray("elements");
				try {
					int status = jo.getInteger("status");
					if (status != 0) {
						System.out.println(urlString);
						System.out.println("Error: " + status);
					}
					int k = 0;
					for (int i = 0; i < lenOrigin; i++) {
						for (int j = 0; j < lenDestination; j++) {
							double distance = ja.getJSONObject(k).getJSONObject("distance").getDouble("value");
							double duration = ja.getJSONObject(k).getJSONObject("duration").getDouble("value");
							TimeMatrix[origins.get(i)][destinations.get(j)] = duration;
							DistMatrix[origins.get(i)][destinations.get(j)] = distance;
							k++;
						}
					}


				} catch(Exception e) {
					e.printStackTrace();
				}

			}
		}
		

		
	}
}
