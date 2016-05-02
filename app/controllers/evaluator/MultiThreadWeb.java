package controllers.evaluator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MultiThreadWeb {
	
	static String[] ak = new String[]{"tMOqfmvxDxbKYXB4G8bYj51G", "lY16o65BDZlSXUeQAjpfnYey", "LxqSHGb5bNjakvYiHOaUOQwu", "LsguN0Opa0eGr5EBZCzGKuxk", "DQD0vsEGANxneFh7ZQdDRwYH"};
	static int akl = ak.length;
	
	final int concurrency = 100;
	
	public double[][] TimeMatrix;
	public double[][] DistMatrix;
	
	/* vehicle: 0 --- 电动自行车
	 *          1 --- 步行
	 *          2 --- 电动三轮车
	 */         
	public void request(String[] addrList, int vehicle) {
		double begin = System.currentTimeMillis();
		getGpsList(addrList);										// 对地址进行解析，获取相应的经纬度
		double end = System.currentTimeMillis();
		double timeGeocoding = end - begin;
		System.out.println("Geocoding: " + timeGeocoding / 1000);
		
		begin = System.currentTimeMillis();
		getDistTime(addrList);										// 根据地址解析得到的经纬度，获取时间和路程信息
		end = System.currentTimeMillis();
		double timeRequest = end - begin;
		System.out.println("Request: " + timeRequest / 1000);
		
		System.out.println("Total: " + (timeGeocoding + timeRequest) / 1000);
		
		
		// 根据不同的交通工具类型，对结果进行不同程度的缩放
		int addrLen = addrList.length;
		this.TimeMatrix = new double[addrLen][addrLen];
		this.DistMatrix = new double[addrLen][addrLen];
		
		for (int i = 0; i < addrLen; i++) {
			for (int j = 0; j < addrLen; j++) {
				if (i == j) {
					this.TimeMatrix[i][j] = 0;
					this.DistMatrix[i][j] = 0;
					continue;
				}
				this.DistMatrix[i][j] = DistTimeGetter.DistMatrix[i][j];
				if (vehicle == 0) {
					this.TimeMatrix[i][j] = DistTimeGetter.TimeMatrix[i][j] / 5;
				}
				if (vehicle == 1) {
					this.TimeMatrix[i][j] = DistTimeGetter.TimeMatrix[i][j] / 1.5;
				}
				if (vehicle == 2) {
					this.TimeMatrix[i][j] = DistTimeGetter.TimeMatrix[i][j] / 4;
				}
			}
		}
	}


    public void insertRequest(String[] addrList, String iaddr, int vehicle) {
		// 将地址数组和要插入的新地址组成一个新的数组
		String[] nAddrList = new String[addrList.length + 1];
		for (int i = 0; i < addrList.length; i++) {
			nAddrList[i] = addrList[i];
		}
		nAddrList[addrList.length] = iaddr;
		getGpsList(nAddrList);				// 解析所有的地址
		
		getInsertDistTime(addrList, iaddr);
		
		// 根据不同的交通工具类型，对结果进行不同程度的缩放
		int addrLen = addrList.length;
		this.TimeMatrix = new double[addrLen][addrLen];
		this.DistMatrix = new double[addrLen][addrLen];
		
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < addrLen; j++) {
				this.DistMatrix[i][j] = DistTimeGetter.DistMatrix[i][j];
				if (vehicle == 0) {
					this.TimeMatrix[i][j] = DistTimeGetter.TimeMatrix[i][j] / 5;
				}
				if (vehicle == 1) {
					this.TimeMatrix[i][j] = DistTimeGetter.TimeMatrix[i][j] / 1.5;
				}
				if (vehicle == 2) {
					this.TimeMatrix[i][j] = DistTimeGetter.TimeMatrix[i][j] / 4;
				}
			}
		}
	}
	
	// 对地址进行解析，获取相应的经纬度
	// 经纬度数组保存在GpsGetter.GpsList中
	private void getGpsList(String[] addrList) {
		// 对GpsList进行初始化
		GpsGetter.init(addrList);
		// 创建线程，执行地址解析的任务
		GpsGetter ggThreads[] = new GpsGetter[addrList.length];
		for(int i = 0; i < addrList.length; i++) {
			ggThreads[i] = new GpsGetter(addrList[i], i);
		}
		for(int i = 0; i < addrList.length; i++) {
			ggThreads[i].start();
		}
		for(int i = 0; i < addrList.length; i++) {
			try {
				ggThreads[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	// 根据地址解析得到的经纬度，获取时间和路程信息
	// 结果分别保存在DistTimeGetter.TimeMatrix和DistTimeGetter.DistMatrix中
	private void getDistTime(String[] addrList) {
		DistTimeGetter.init(addrList);
		int addrLen = addrList.length;
		
		ArrayList<DistTimeGetter> dtgThreadPool = new ArrayList<DistTimeGetter>();
		
		for (int i = 0; i <= (addrLen - 1)/5; i++) {
			// 构造起始点数组
			ArrayList<Integer> origins = new ArrayList<Integer>();
			for (int k = i * 5; k < i * 5 + 5; k++) {
				if (k >= addrLen) {
					break;
				}
				origins.add(k);
			}
			
			for (int j = 0; j <= (addrLen - 1)/5; j++) {
				// 构造终点数组
				ArrayList<Integer> destinations = new ArrayList<Integer>();
				for (int k = j * 5; k < j * 5 + 5; k++) {
					if (k >= addrLen) {
						break;
					}
					destinations.add(k);
				}
				
				// 根据起始点和终点构造url
				int lenOrigin = origins.size();
				int lenDestination = destinations.size();
				String urlString = "http://api.map.baidu.com/direction/v1/routematrix?mode=walking&output=json&origins=";
				for (int k = 0; k < lenOrigin; k++) {
					urlString += GpsGetter.GpsList[origins.get(k)][0] + "," + GpsGetter.GpsList[origins.get(k)][1];
					if (k != lenOrigin - 1) {
						urlString += "|";
					}
				}
				urlString += "&destinations=";
				for (int k = 0; k < lenDestination; k++) {
					urlString += GpsGetter.GpsList[destinations.get(k)][0] + "," + GpsGetter.GpsList[destinations.get(k)][1];
					if (k != lenDestination - 1) {
						urlString += "|";
					}
				}
				urlString += "&ak=" + ak[(i * 5 + j) % akl];
				
				// 创建线程，等待执行
				DistTimeGetter dtgThread = new DistTimeGetter(urlString, origins, destinations);
				dtgThreadPool.add(dtgThread);
				
			}
			
		}
		
		// 并发启动线程，限制最大并发数
		
		int lenThread = dtgThreadPool.size();
		for (int i = 0; i < lenThread; i += concurrency) {
			for (int j = i; j < i + concurrency && j < lenThread; j++) {
				dtgThreadPool.get(j).start();
			}
			for (int j = i; j < i + concurrency && j < lenThread; j++) {
				try {
					dtgThreadPool.get(j).join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	private void getInsertDistTime(String[] addrList, String iaddr) {
		DistTimeGetter.initInsert(addrList);
		int addrLen = addrList.length;
		
		ArrayList<DistTimeGetter> dtgThreadPool = new ArrayList<DistTimeGetter>();
		
		for (int i = 0; i <= (addrLen - 1) / 5; i++) {
			// 每次选取五个地址与待插入地址分别构成起点和终点
			ArrayList<Integer> requestAddrs = new ArrayList<Integer>();
			for (int j = i * 5; j < i * 5 + 5; j++) {
				if (j >= addrLen) {
					break;
				}
				requestAddrs.add(j);
			}
			
			int rAddrsLen = requestAddrs.size();
			String urlString = "http://api.map.baidu.com/direction/v1/routematrix?mode=walking&output=json&origins=";
			
			// 以插入点为起始点的url
			String urlString1 = urlString + GpsGetter.GpsList[addrLen][0] + "," + GpsGetter.GpsList[addrLen][1];
			urlString1 += "&destinations=";
			for (int j = 0; j < rAddrsLen; j++) {
				urlString1 += GpsGetter.GpsList[requestAddrs.get(j)][0] + "," + GpsGetter.GpsList[requestAddrs.get(j)][1];
				if (j != rAddrsLen - 1) {
					urlString1 += "|";
				}
			}
			urlString1 += "&ak=" + ak[4 - ((i * 5) % akl)];
			DistTimeGetter dtgThread1 = new DistTimeGetter(urlString1, requestAddrs, 0);
			dtgThreadPool.add(dtgThread1);
			
			// 以插入点为终点的url
			String urlString2 = urlString;
			for (int j = 0; j < rAddrsLen; j++) {
				urlString2 += GpsGetter.GpsList[requestAddrs.get(j)][0] + "," + GpsGetter.GpsList[requestAddrs.get(j)][1];
				if (j != rAddrsLen - 1) {
					urlString2 += "|";
				}
			}
			urlString2 += "&destinations=" + GpsGetter.GpsList[addrLen][0] + "," + GpsGetter.GpsList[addrLen][1];
			urlString2 += "&ak=" + ak[(i * 5) % akl];
			DistTimeGetter dtgThread2 = new DistTimeGetter(urlString2, requestAddrs, 1);
			dtgThreadPool.add(dtgThread2);
		}
		
		// 开始并发执行
		int lenThread = dtgThreadPool.size();
		for (int i = 0; i < lenThread; i += concurrency) {
			for (int j = i; j < i + concurrency && j < lenThread; j++) {
				dtgThreadPool.get(j).start();
			}
			for (int j = i; j < i + concurrency && j < lenThread; j++) {
				try {
					dtgThreadPool.get(j).join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// 根据url发送get请求，获取返回的结果
	public static String getFromUrl(String urlString) {
		String result = "";
		BufferedReader in = null;
		try {
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			connection.connect();
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		
		return result;
	}
	

}




