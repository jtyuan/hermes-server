package controllers.scheduler;
import java.util.ArrayList;

public class Graph {

    public ArrayList<Point> points;
    public int points_num;
    public int origin_points_num;
    public double initiated_tau;
    private ArrayList<ArrayList<Double>> dis_matrix;
    private ArrayList<ArrayList<Double>> time_matrix;
    private ArrayList<ArrayList<Double>> tau_matrix;
    private ArrayList<ArrayList<Double>> delta_matrix;


    public Graph(ArrayList<Point> points_list, int origin_points_num,
                 ArrayList<ArrayList<Double>> dis_matrix, ArrayList<ArrayList<Double>> time_matrix) {
        // origin_points_num 是说：有可能接下来蚁群跑的点会变少，但要知道最原始给的时间、距离矩阵的维度，保证tau等与其一致
        // points_list是所有订单点的数组，每一项具体什么形式还可以再考虑
        this.points_num = points_list.size(); //起点
        this.origin_points_num = origin_points_num;
        this.points = new ArrayList<Point>(points_list);

        this.dis_matrix = new ArrayList<ArrayList<Double>>(dis_matrix);
        this.time_matrix = new ArrayList<ArrayList<Double>>(time_matrix);

        this.tau_matrix = new ArrayList<ArrayList<Double>>(); //信息素矩阵,可以来个初始值
        this.initTau();

        this.delta_matrix = new ArrayList<ArrayList<Double>>(); //每轮所有蚂蚁会对所有路线贡献一定的信息素，这个矩阵来记录某一轮每条路线本轮获得的信息素
        this.resetDeltaMatrix();

//        this.lock=Lock() #同时只能有一只蚂蚁更新地图 // TODO 地图上的锁
    }

    public void initTau() {
        this.initiated_tau = 1;
        for (int i = 0; i < this.origin_points_num; ++i) {
            ArrayList<Double> tmp = new ArrayList<Double>();
            for (int j = 0; j < this.origin_points_num; ++j) {
                tmp.add(this.initiated_tau);
            }
            this.tau_matrix.add(tmp);
        }
    }

    public void initTauWithFirstBest(double init_tau) {
        this.initiated_tau = init_tau;
        for (int i = 0; i < this.origin_points_num; ++i) {
            for (int j = 0; j < this.origin_points_num; ++j) {
                this.tau_matrix.get(i).set(j, init_tau);
            }
        }
    }

    public Point getPoint(int point_index) {
        for (int i = 0; i < this.points_num; ++i) {
            if (this.points.get(i).getIndex() == point_index)
                return this.points.get(i);
        }
        return null;
    }

    public double getDistance(int start, int end) {
        return this.dis_matrix.get(start).get(end);
    }

    public double getTime(int start, int end) {
        return this.time_matrix.get(start).get(end);
    }

    public void resetDeltaMatrix() {
        for (int i = 0; i < this.origin_points_num; ++i) {
            ArrayList<Double> tmp = new ArrayList<Double>();
            for (int j = 0; j < this.origin_points_num; ++j) {
                tmp.add(0.0);
            }
            this.delta_matrix.add(tmp);
        }
    }

    public double getDelta(int start, int end) {
        return this.delta_matrix.get(start).get(end);
    }

    public void accumulateDelta(int start, int end, double new_delta) {
        double tmp_delta = this.delta_matrix.get(start).get(end);
        this.delta_matrix.get(start).set(end, tmp_delta + new_delta);
    }

    public double getTau(int start, int end) {
        return this.tau_matrix.get(start).get(end);
    }

    public void updateTau(int start, int end, double new_tau) {
        this.tau_matrix.get(start).set(end, new_tau);
    }

}