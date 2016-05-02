package controllers.scheduler;

import com.alibaba.fastjson.JSONArray;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import play.Application;
import play.Play;

@SuppressWarnings("Duplicates")
public class AntColonyVRPTW {

    public static ArrayList<ResultOrder> testSchedule() {
        final Application app = Play.application();

        String json_str = "";
        //从文件中读入json串
        File jsonFile = app.getFile("public/data/100points.json");
        try {
            FileReader jsonFileReader= new FileReader(jsonFile);
            BufferedReader buffer = new BufferedReader(jsonFileReader);
            try {
                json_str = buffer.readLine();
                System.out.println(json_str);
                buffer.close();
                jsonFileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //解析json把point生成一下
        ArrayList<controllers.scheduler.JsonOrder> JsonOrderArray = new ArrayList<>(JSONArray.parseArray(json_str, JsonOrder.class));
        String[] addresses = new String[JsonOrderArray.size()];
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < 50; ++i) {
            JsonOrder order = JsonOrderArray.get(i);
//            System.out.println(order.getSignTime());
            Point p = new Point(i, order.getOrderID(), order.getAddress(), new Pair<>(order.getAppointment().get(0), order.getAppointment().get(1)),
                    1, order.getSignTime(), 0);
            //order.getVip_level()
            points.add(p);
            addresses[i] = p.getAddress();
        }


        //去百度要
//        MultiThreadWeb dis_time_req = new MultiThreadWeb();
//        dis_time_req.request(addresses, 0); //开始要,后面会阻塞


        double[][] dist_mat_arr = new double[JsonOrderArray.size()][JsonOrderArray.size()];
        double[][] time_mat_arr = new double[JsonOrderArray.size()][JsonOrderArray.size()];


        ObjectMapper om = new ObjectMapper();

        File distFile = app.getFile("public/data/dist_mat.json");
        File timeFile = app.getFile("public/data/time_mat.json");
        try {
            FileReader distReader= new FileReader(distFile);
            BufferedReader distBuf = new BufferedReader(distReader);
            try {
                json_str = distBuf.readLine();
                dist_mat_arr = om.readValue(json_str, double[][].class);
//                System.out.println(json_str);
                distBuf.close();
                distReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            FileReader timeReader= new FileReader(timeFile);
            BufferedReader timeBuf = new BufferedReader(timeReader);
            try {
                json_str = timeBuf.readLine();
                time_mat_arr = om.readValue(json_str, double[][].class);
//                System.out.println(json_str);
                timeBuf.close();
                timeReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        ArrayList<ArrayList<Double>> dist_mat = new ArrayList<>();
        ArrayList<ArrayList<Double>> time_mat = new ArrayList<>();
        for (int i = 0; i < points.size(); ++i) {
            ArrayList<Double> dist_tmp = new ArrayList<>();
            ArrayList<Double> time_tmp = new ArrayList<>();
            for (int j = 0; j < points.size(); ++j) {
                dist_tmp.add(dist_mat_arr[i][j]);
                time_tmp.add(time_mat_arr[i][j]);
            }
            dist_mat.add(dist_tmp);
            time_mat.add(time_tmp);
        }
        ScheduleResult result = new AntColonyVRPTW().runSchedule(points, dist_mat, time_mat);
        return parse(result, JsonOrderArray);
    }

    private static ArrayList<ResultOrder> parse(ScheduleResult result, ArrayList<JsonOrder> orders) {
        ArrayList<ResultOrder> list = new ArrayList<>();
        for (DonePoint dp : result.getAlreadySchedule()) {
            ResultOrder order = new ResultOrder();
            order.setOrderID(dp.getOrderID());
            for (JsonOrder o : orders) {
                if (o.getOrderID().equals(dp.getOrderID())) {
                    order.setAddress(o.getAddress());
                    order.setAppointment("" + o.getAppointment().get(0) + "," + o.getAppointment().get(1));
                    order.setSign_need_time(o.getSignTime());
                    orders.remove(o);
                    break;
                }
            }
            order.setStatus(0); // Uninformed
            order.setVip_level(1);
            order.setArrive_time(dp.getArrive_time());
            order.setWait_time(dp.getWait_time());
            order.setLeave_time(dp.getLeave_time());
            order.setFailure_reason("规划成功");
            list.add(order);
        }
        for (String id : result.getConflictOrdersID()) {
            ResultOrder order = new ResultOrder();
            order.setOrderID(id);
            for (JsonOrder o : orders) {
                if (o.getOrderID().equals(id)) {
                    order.setAddress(o.getAddress());
                    order.setAppointment("" + o.getAppointment().get(0) + "," + o.getAppointment().get(1));
                    order.setSign_need_time(o.getSignTime());
                    orders.remove(o);
                    break;
                }
            }
            order.setStatus(3); // Failed
            order.setVip_level(1);
            order.setArrive_time(0);
            order.setWait_time(0);
            order.setLeave_time(0);
            order.setFailure_reason("规划冲突");
            list.add(order);
        }
        return list;
    }

    public ScheduleResult runSchedule(ArrayList<Point> points, ArrayList<ArrayList<Double>> distance_matrix,
                              ArrayList<ArrayList<Double>> time_matrix) {

        long start = System.currentTimeMillis();

        Graph VRPTW_graph = new Graph(points, points.size(), distance_matrix, time_matrix);
        AntColony ant_colony = new AntColony(90, 95, VRPTW_graph);
        AntResult best_ant = ant_colony.start();
        ArrayList<DonePoint> best_path = new ArrayList<DonePoint>(best_ant.done_path);
        double best_score = best_ant.getScore();
        Pair<ArrayList<Integer>, ArrayList<Integer>> conflicts = findConflict(best_path, VRPTW_graph);

        long end = System.currentTimeMillis();
        long interval = end - start;

        ArrayList<String> conflict_OrdersID = new ArrayList<>();
        for(int i = 0; i < conflicts.getValue().size(); ++i){
            conflict_OrdersID.add(points.get(conflicts.getValue().get(i)).getOrderID());
            for (DonePoint p : best_path) {
                if (p.getOrderID().equals(conflict_OrdersID.get(i))) {
                    best_path.remove(p);
                    break;
                }
            }
        }

//        System.out.println("Algorithm: " + wrapper.getTime()/1000.0);
//        System.out.println("Conflicts: " + wrapper.getconflict_ordersID());
        return new ScheduleResult(conflict_OrdersID, best_path);
    }

    public Pair<ArrayList<Integer>, ArrayList<Integer>> findConflict(ArrayList<DonePoint> best_path, Graph graph) {
        //在bestpath中的index   ,     该index对应的point
        ArrayList<Point> points = graph.points;
        ArrayList<Integer> conflict_index = new ArrayList<Integer>();
        ArrayList<Integer> conflict_orders = new ArrayList<Integer>();
        for (int i = 0; i < best_path.size(); ++i) {
            if (best_path.get(i).getArrive_time() - points.get(best_path.get(i).getPoint_index()).getTimeWindow().getValue() > 0) {
                conflict_index.add(i);
                conflict_orders.add(best_path.get(i).getPoint_index());
            }
        }
        Collections.sort(conflict_index);
        Collections.sort(conflict_orders);
        return new Pair<>(conflict_index, conflict_orders);
    }

    public class ScheduleResult {
//        long time;
//        double best_score;
        ArrayList<String> conflictOrdersID;
        ArrayList<DonePoint> alreadySchedule;

        public ScheduleResult(ArrayList<String> conflict_ordersID, ArrayList<DonePoint> already_schedule) {
            this.conflictOrdersID = conflict_ordersID;
            this.alreadySchedule = already_schedule;
        }

        public ArrayList<String> getConflictOrdersID() {
            return conflictOrdersID;
        }

        public void setConflictOrdersID(ArrayList<String> conflict_ordersID) {
            this.conflictOrdersID = conflict_ordersID;
        }

        public ArrayList<DonePoint> getAlreadySchedule() {
            return alreadySchedule;
        }

        public void setAlreadySchedule(ArrayList<DonePoint> already_schedule) {
            this.alreadySchedule = already_schedule;
        }
    }
}

