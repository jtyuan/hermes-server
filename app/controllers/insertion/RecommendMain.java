package controllers.insertion;

import controllers.scheduler.JsonOrder;
import controllers.scheduler.Pair;
import controllers.scheduler.Point;
import controllers.scheduler.DonePoint;
import controllers.evaluator.MultiThreadWeb;
import com.alibaba.fastjson.JSONArray;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class RecommendMain {
//    public static void main(String[] args) {
//        String json_order_str = "";
//        //从文件中读入json point 串
//        File jsonFile = new File("src/controllers.schedule/data.json");
//        try {
//            FileReader jsonFileReader= new FileReader(jsonFile);
//            BufferedReader buffer = new BufferedReader(jsonFileReader);
//            try {
//                json_order_str = buffer.readLine();
//                System.out.println(json_order_str);
//                buffer.close();
//                jsonFileReader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        //解析json把point生成一下
//        ArrayList<JsonOrder> JsonOrderArray = new ArrayList<>(JSONArray.parseArray(json_order_str, JsonOrder.class));
//        ArrayList<Point> points = new ArrayList<>();
//        for (int i = 0; i < JsonOrderArray.size(); ++i) {
//            JsonOrder order = JsonOrderArray.get(i);
////            System.out.println(order.getSignTime());
//            Point p = new Point(i, order.getOrderID(), order.getAddress(), new Pair<>(order.getAppointment().get(0), order.getAppointment().get(1)),
//                    1, order.getSignTime(), 0);
//            //order.getVip_level()
//            points.add(p);
//        }
//
//        //读取json schedule串
//        String json_schedule_str = "";
//        jsonFile = new File("src/Insertion_Algorithm/schedule.json");
//        try {
//            FileReader jsonFileReader= new FileReader(jsonFile);
//            BufferedReader buffer = new BufferedReader(jsonFileReader);
//            try {
//                json_schedule_str = buffer.readLine();
//                System.out.println(json_schedule_str);
//                buffer.close();
//                jsonFileReader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        //解析json把already_schedule生成一下
//        ArrayList<DonePoint> ScheduleArray = new ArrayList<>(JSONArray.parseArray(json_schedule_str, DonePoint.class));
//
//
//        ArrayList<Point> schedule_points = new ArrayList<>();
//        Point will_insert = new Point();    //还是空的
//        String will_insert_orderID = "order_3";    //待插入的那个订单的order id
//        int will_insert_index = 0;
//        int changed = 0;    //因为要把准备插入的放在队尾, 所以points的index根据i会有一点变化,
//        for(int i = 0; i < ScheduleArray.size(); i++){
//            Point schedule_point = getPointByID(ScheduleArray.get(i).getOrderID(), points); //肯定能找到的应该
//            if(if_the_insert_one(will_insert_orderID, schedule_point)){
//                //这个就是要准备插入的那个,把他在already中删掉,先记下来,循环完了删掉
//                will_insert = schedule_point;
//                will_insert_index = i;
//                changed = 1;
//            }else{
//                //不是要被插的那个
//                if(if_the_conflict(ScheduleArray.get(i).getArrive_time(), schedule_point)){
//                    //如果是冲突列表里的,要改原本的appointment,变成arrive_time的+-15
//                    int arrive_time = ScheduleArray.get(i).getArrive_time();
//                    schedule_point.setTimeWindow(new Pair<>(arrive_time-15*60, arrive_time+15*60));
//                }
//                schedule_point.setIndex(i-changed);
//                schedule_points.add(schedule_point);    //改index,插入
//            }
//        }
//
//        //准备百度的输入
//        String[] addresses = new String[schedule_points.size()];
//        for(int i = 0; i < schedule_points.size(); i++){
//            addresses[i] = schedule_points.get(i).getAddress();
//        }
//        String iaddress = will_insert.getAddress();
//
//        //去百度要
//        MultiThreadWeb time_req = new MultiThreadWeb();
//        time_req.insertRequest(addresses, iaddress, 0); //开始要,后面会阻塞
//
//        ArrayList<ArrayList<Double>> time_mat = new ArrayList<>();
//        for (int i = 0; i < schedule_points.size(); ++i) {
//            ArrayList<Double> dist_tmp = new ArrayList<>();
//            ArrayList<Double> time_tmp = new ArrayList<>();
//            for (int j = 0; j < schedule_points.size(); ++j) {
//                time_tmp.add(time_req.TimeMatrix[i][j]);
//            }
//            time_mat.add(time_tmp);
//        }
//
//        will_insert.setIndex(ScheduleArray.size()-1);   //要被插的这个点插在最后面
//        schedule_points.add(will_insert);   //最后再把他插进来,为了接口
//        ScheduleArray.remove(will_insert_index);    //Schedule里把他删掉
//
//        //以上,输入全部搞定 schedule_points是要传的points
//        //要插入的那个在schedule_points的最后
//        InsertionRecommend ins_rec = new InsertionRecommend(schedule_points, time_mat, ScheduleArray, schedule_points.size()-1);
//        ArrayList<HashMap<String, Integer>> rec_result = ins_rec.recommend();
//        for(HashMap<String, Integer> rec : rec_result){
//            System.out.println(rec);
//        }
//        //挑第一个插入来测试
//        HashMap<String, Integer> insert_info = rec_result.get(0);
//        System.out.println("选这个地方插入:");
//        System.out.println(insert_info);
//        ScheduleArray = ins_rec.insert(insert_info.get("insert_pos"));
//        for(DonePoint p : ScheduleArray){
//            System.out.println(p);
//        }
//    }
    public static Point getPointByID(String orderID, ArrayList<Point> points){
        Point point_to_get = new Point();
        for(int i = 0; i < points.size(); i++){
            if(points.get(i).getOrderID().equals(orderID)){
                point_to_get = points.get(i);
                break;
            }
        }
        return point_to_get;
    }
    public static boolean if_the_conflict(int arrive_time, Point order){
        //arrive_time如果比时间窗晚,说明是冲突的
        if(arrive_time > order.getTimeWindow().getValue()){
            return true;
        }else{
            return false;
        }
    }
    public static boolean if_the_insert_one(String orderID, Point cmp_point){
        if(cmp_point.getOrderID().equals(orderID)){
            return true;
        }else{
            return false;
        }
    }
}
