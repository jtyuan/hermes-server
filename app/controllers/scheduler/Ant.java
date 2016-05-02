package controllers.scheduler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class Ant implements Callable<AntResult> {
    private static final double P0 = 0.67;
    private static final double ALPHA = 0.25;
    private static final double BETA = 0.1 * (1 - ALPHA);
    private static final double GAMMA = 0.25 * (1 - ALPHA);
    private static final double THETA = 0.5 * (1 - ALPHA);
    private static final double ETA = 0.1 * (1 - ALPHA);
    private static final double IOTA = 0.05 * (1 - ALPHA);
    public double level_and_time_punish;
    public double dis_cost;
    public int time_line;
    public ArrayList<ArrayList<Integer>> path_matrix;
    public ArrayList<DonePoint> done_path;
    private HashMap<Integer, Integer> need_to_go;
    private AntColony ant_colony;
    private Graph graph;
    private ArrayList<Point> points;
    private int points_num;
    private int current_point_index;
    private int start_point_index;
    private int ID;

    public Ant(int ID, int start_point_index, AntColony ant_colony) {
        this.ID = ID;
        this.start_point_index = start_point_index;    //起点都是配送站点
        this.ant_colony = ant_colony;     //这里有graph, 更新规则， 评分规则等
        this.graph = this.ant_colony.graph;
        this.points = this.graph.points;
        this.points_num = this.graph.points_num;

        this.current_point_index = this.start_point_index;
        this.done_path = new ArrayList<DonePoint>();     //存到达地index和到达时间的组合 {point_index: ... , arrive_time: ...}
        int start_time = this.graph.getPoint(start_point_index).getTimeWindow().getKey();
        DonePoint tmp_map = new DonePoint(this.current_point_index, this.points.get(this.current_point_index).getOrderID(),
                            start_time, 0, start_time);
        this.done_path.add(tmp_map);

        this.need_to_go = new HashMap<Integer, Integer>();    //自然也是存的index，不是数组，因为不一定是顺序的，是dict方便存取
        for (int i = 0; i < this.points_num; ++i) {
            if (this.points.get(i).getIndex() != start_point_index) {
                this.need_to_go.put(this.points.get(i).getIndex(), this.points.get(i).getIndex());
            }
        }

        this.path_matrix = new ArrayList<ArrayList<Integer>>();   //记录走过的路的矩阵，用来更新信息素的，也得是老（原来）点数的矩阵，要不然不好做
        for (int i = 0; i < this.graph.origin_points_num; ++i) {
            ArrayList<Integer> tmp = new ArrayList<Integer>();
            for (int j = 0; j < this.graph.origin_points_num; ++j) {
                tmp.add(0);
            }
            this.path_matrix.add(tmp);
        }

        this.time_line = start_time;   //记录时间流逝，一路过来的时间，单位为秒
        this.dis_cost = 0;   //记录一路过来所走的路程的总和,也不要太大。。
        this.level_and_time_punish = 0;      //记录level*时间惩罚的总和
    }

    public void init(int ID, int start_point_index, AntColony ant_colony) {
        this.ID = ID;
        this.start_point_index = start_point_index;
        this.ant_colony = ant_colony;
        this.graph = this.ant_colony.graph;
        this.points = this.graph.points;
        this.points_num = this.graph.points_num;

        this.current_point_index = this.start_point_index;
        this.done_path.clear();
        int start_time = this.graph.getPoint(start_point_index).getTimeWindow().getKey();
        DonePoint tmp_map = new DonePoint(this.current_point_index, this.points.get(this.current_point_index).getOrderID(),
                                start_time, 0, start_time);
        this.done_path.add(tmp_map);

        this.need_to_go.clear();
        for (int i = 0; i < this.points_num; ++i) {
            if (this.points.get(i).getIndex() != start_point_index) {
                this.need_to_go.put(this.points.get(i).getIndex(), this.points.get(i).getIndex());
            }
        }

        this.path_matrix.clear();
        for (int i = 0; i < this.graph.origin_points_num; ++i) {
            ArrayList<Integer> tmp = new ArrayList<Integer>();
            for (int j = 0; j < this.graph.origin_points_num; ++j) {
                tmp.add(0);
            }
            this.path_matrix.add(tmp);
        }

        this.time_line = start_time;
        this.dis_cost = 0;
        this.level_and_time_punish = 0;
    }

    @Override
    public AntResult call() throws Exception {
        while (this.need_to_go.size() > 0) {
            int next_point_index = this.whereToGo();
            this.need_to_go.remove(next_point_index);
            int previous_point_index = this.current_point_index;
            this.current_point_index = next_point_index;
            Point current_point = this.graph.getPoint(this.current_point_index);
            // 三个指标更新
            this.dis_cost += this.graph.getDistance(previous_point_index, this.current_point_index);
            this.time_line += this.graph.getTime(previous_point_index, this.current_point_index);   //到达时间
            this.level_and_time_punish += (1 + Math.log10(current_point.getLevel())) *
                    this.timeWindowPunish(this.time_line, current_point.getTimeWindow().getKey(), current_point.getTimeWindow().getValue());
            // 记录行走过程
            this.path_matrix.get(previous_point_index).set(this.current_point_index, 1);
            // 有可能会提前到了然后等待时间窗的到来，这时候time_line还是到达时间， 把此次选择记录下来
            int arrive_time = this.time_line;
            int wait_time = this.getWaitTime(this.time_line, current_point.getTimeWindow().getKey());
            // 在到达点配送还需要时间，和等待时间一起加到time_line里
            this.time_line += wait_time + current_point.getSignNeedTime();

            DonePoint tmp_map = new DonePoint(this.current_point_index, this.points.get(this.current_point_index).getOrderID(),
                                arrive_time, wait_time, this.time_line);
            this.done_path.add(tmp_map);
//            System.out.println(JSON.toJSONString(this.done_path));
            // TODO: 3/24/16 局部的信息素更新  self.update_path_tau(previous_point_index,self.current_point_index)
        }
        AntResult result = new AntResult(this);
        this.init(this.ID, this.start_point_index, this.ant_colony);
        return result;
    }

    public int whereToGo() {
        double p = Math.random();
        ArrayList<Map<String, Double>> result = new ArrayList<Map<String, Double>>();
        Map<String, Double> choice;
        for (Integer value : need_to_go.values()) {
            result.add(this.pointScore(value));
        }
        if (p < P0) {
            choice = this.maxPoint(result);
        } else {
            choice = this.roulette(result);
        }
        return (int) Math.floor(choice.get("index"));
    }

    public Map<String, Double> maxPoint(ArrayList<Map<String, Double>> points_list) {
        double max_score = -1;
        ArrayList<Map<String, Double>> max_list = new ArrayList<Map<String, Double>>();
//        System.out.print("\n");
        for (int i = 0; i < points_list.size(); ++i) {
//            System.out.print(points_list.get(i).get("score"));
            if (points_list.get(i).get("score") > max_score) {
                max_score = points_list.get(i).get("score");
                max_list.clear();
                max_list.add(points_list.get(i));
            } else if (points_list.get(i).get("score") == max_score) {
                max_list.add(points_list.get(i));
            }
        }
//        System.out.println(points_list.size() + "," + max_list.size());
        return max_list.get((int) (Math.random() * max_list.size()));
    }

    public Map<String, Double> roulette(ArrayList<Map<String, Double>> points_list) {
        double sum = 0;
        int points_num = points_list.size();
        for (int i = 0; i < points_num; ++i) {
            sum += points_list.get(i).get("score");
        }

        ArrayList<Map<String, Double>> points_ratio = new ArrayList<Map<String, Double>>(points_list);
        for (int i = 0; i < points_num; ++i) {
            points_ratio.get(i).put("score", points_ratio.get(i).get("score") / sum);
        }
        double inner_sum = 0;
        for (int i = 0; i < points_num; ++i) {
//            System.out.println("p" + i + " " + points_list.get(i).get("score"));
            inner_sum += points_ratio.get(i).get("score");
            points_ratio.get(i).put("score", inner_sum);
        }
        double lucky_random = Math.random() * inner_sum;
//        System.out.println("lucky_random" + lucky_random);
        if (lucky_random < points_ratio.get(0).get("score"))
            return points_ratio.get(0);
        for (int i = 1; i < points_num; ++i) {
            if (lucky_random >= points_ratio.get(i - 1).get("score") && lucky_random < points_ratio.get(i).get("score")) {
                return points_ratio.get(i);
            }
        }
        return null;
    }

    public Map<String, Double> pointScore(int next_point_index) {
        double score = this.pathScore(this.current_point_index, next_point_index);
        Map<String, Double> result = new HashMap<String, Double>();
        result.put("index", (double) next_point_index);
        result.put("score", score);
        return result;
    }

    public double pathScore(int cur_point_index, int next_point_index) {
        double tau = this.graph.getTau(cur_point_index, next_point_index);
        double dis = Math.max(1, this.graph.getDistance(cur_point_index, next_point_index));
        int time_need = (int) Math.max(1, this.graph.getTime(cur_point_index, next_point_index));
        Point next_point = this.graph.getPoint(next_point_index);
        int arrive_time = this.time_line + time_need;
        int level = next_point.getLevel();
        int time_punish = this.timeWindowPunish(arrive_time, next_point.getTimeWindow().getKey(), next_point.getTimeWindow().getValue());
        int window_width = next_point.getTimeWindow().getValue() - next_point.getTimeWindow().getKey();
//        System.out.println("0:" + tau + ", " + dis);
//        System.out.println("1:" + (Math.pow(tau, ALPHA))  + ", " +  (Math.pow(10.0 / dis, BETA))  + ", " +  (Math.pow(10.0 / time_need, GAMMA))
//                + ", " +  (Math.pow(10.0 / time_punish, THETA))  + ", " +  (Math.pow(level / 50.0, ETA))  + ", " +  (Math.pow(10.0 / window_width, IOTA)));
        double score = (Math.pow(tau, ALPHA)) * (Math.pow(10.0 / dis, BETA)) * (Math.pow(10.0 / time_need, GAMMA))
                * (Math.pow(10.0 / time_punish, THETA)) * (Math.pow(level / 50.0, ETA)) * (Math.pow(10.0 / window_width, IOTA));
//        System.out.println("2:" + score);
        return score;
    }

    public int timeWindowPunish(int arrive_time, int time_window_a, int time_window_b) {
        int early_time_allowed = 40 * 60;
        int early_punish_grow = 3;
        int max_early_punish = 20000;

        int late_time_allowed = 0;
        int late_punish_grow = 12;
        int max_late_punish = 125000;

        int punishment = 1;

        if (arrive_time > time_window_b) {
            if (arrive_time - time_window_b > late_time_allowed) {
                punishment += max_late_punish;
            } else {
                punishment += (arrive_time - time_window_b) * late_punish_grow;
            }
        } else if (arrive_time >= time_window_a && arrive_time <= time_window_b) {
            punishment += 0;
        } else {
            int earlier_time = Math.abs(arrive_time - time_window_a);
            if (earlier_time <= early_time_allowed) {
                punishment += early_punish_grow * earlier_time;
            } else {
                punishment += max_early_punish;
            }
        }
        return punishment;
    }

    public int getWaitTime(int arrive_time, int time_window_a) {
        if (arrive_time >= time_window_a)
            return 0;
        else return time_window_a - arrive_time;
    }

    public void updatePathTau() {
        return;
    }
}

