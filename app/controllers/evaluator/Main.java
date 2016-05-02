package controllers.evaluator;

public class Main {
	public static void main(String[] args) {
		testInsert();
	}
	
	
	public static void testRequest() {
		MultiThreadWeb msw = new MultiThreadWeb();
		
		String[] addrList = new String[] {
				"乌鲁木齐市.天山区.和平路街道南门国际城G4楼3单元",
				"乌鲁木齐市.天山区.解放北路街道天山路80号信达公司家属院",
				"乌鲁木齐市.天山区.东门街道东环路98号建国南路北社区（鸿雁宾馆旁）301室",
				"乌鲁木齐市.天山区.人民路316号新疆虹联公司",
				"乌鲁木齐市.天山区人民路193号",
				"乌鲁木齐市.天山区.幸福路街道人民路南门国际置地B4座二单元201室",
				"乌鲁木齐市.天山区人民路395号中国人民银行国库处",
				"乌鲁木齐市.天山区.解放南路街道人民路314号南门中亚玉器精品交易中心",
		};
		
		
		msw.request(addrList, 0);
		
		for (int i = 0; i < addrList.length; i++) {
			for (int j = 0; j < addrList.length; j++) {
				System.out.print(msw.TimeMatrix[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
		for (int i = 0; i < addrList.length; i++) {
			for (int j = 0; j < addrList.length; j++) {
				System.out.print(msw.DistMatrix[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	public static void testInsert() {
		MultiThreadWeb msw = new MultiThreadWeb();
		
		String[] addrList = new String[] {
				"乌鲁木齐市.天山区.和平路街道南门国际城G4楼3单元",
				"乌鲁木齐市.天山区.解放北路街道天山路80号信达公司家属院",
				"乌鲁木齐市.天山区.东门街道东环路98号建国南路北社区（鸿雁宾馆旁）301室",
				"乌鲁木齐市.天山区.人民路316号新疆虹联公司",
				"乌鲁木齐市.天山区人民路193号",
				"乌鲁木齐市.天山区.幸福路街道人民路南门国际置地B4座二单元201室",
				"乌鲁木齐市.天山区.解放南路街道人民路314号南门中亚玉器精品交易中心",
		};
		
		String iAddr = "乌鲁木齐市.天山区人民路395号中国人民银行国库处";
		msw.insertRequest(addrList, iAddr, 2);
		
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < addrList.length; j++) {
				System.out.print(msw.TimeMatrix[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < addrList.length; j++) {
				System.out.print(msw.DistMatrix[i][j] + " ");
			}
			System.out.println();
		}
		
	}
	
}
