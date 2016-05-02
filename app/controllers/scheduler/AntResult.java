package controllers.scheduler;
import java.util.ArrayList;

public class AntResult {
    private static final double ALPHA = 0.35;
    private static final double BETA = 0.1;
    private static final double GAMMA = 0.55;
    public ArrayList<DonePoint> done_path;
    public int time_line;
    public double dis_cost;
    public double level_and_time_punish;
    public ArrayList<ArrayList<Integer>> path_matrix;
    public double score;

    public AntResult(Ant ant) {
        this.time_line = ant.time_line;
        this.dis_cost = ant.dis_cost;
        this.level_and_time_punish = ant.level_and_time_punish;
        this.done_path = new ArrayList<DonePoint>(ant.done_path);
        this.path_matrix = new ArrayList<ArrayList<Integer>>(ant.path_matrix);
        this.score = this.calcScore();
    }

    public double calcScore() {
        double score = (Math.pow(this.time_line, ALPHA)) *
                (Math.pow(this.dis_cost, BETA)) * (Math.pow(this.level_and_time_punish, GAMMA));
        return score;
    }

    public double getScore() {
        return this.score;
    }
}