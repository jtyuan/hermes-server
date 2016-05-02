package controllers.scheduler;
import java.util.ArrayList;
import java.util.concurrent.*;

public class AntColony {
    private static final double RHO = 0.65; //跑完一遍信息素减少的比例
    private static final int BEST_THETA = 10;
    private static double Q = 300; //用来计算信息素delta的常数,会变
    public Graph graph;
    private int elite_ants_num;
    private int iter_nums;
    private int ant_nums;
    private double max_tau;
    private double min_tau;
    private ArrayList<Ant> ants;
    private EliteAnts elite_ants;
    private AntResult best_ant;

    private ExecutorService executorService;
    private CompletionService<AntResult> completionService;

    public AntColony(int iter_nums, int ant_nums, Graph graph) {
        this.iter_nums = iter_nums;
        this.ant_nums = ant_nums;
        this.ants = new ArrayList<Ant>();  //数组里装Ant的对象
        this.graph = graph;

        //精英蚂蚁数量
        this.elite_ants_num = this.ant_nums / 8;  //整除8
        this.elite_ants = new EliteAnts(this.elite_ants_num);

        //全局最优蚂蚁
        this.best_ant = null;    //最优蚂蚁

        this.max_tau = -1;
        this.min_tau = -1;

        // init executor;
        int cpuMaxAvailableCoreNum = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(cpuMaxAvailableCoreNum);
        completionService = new ExecutorCompletionService<AntResult>(executorService);
    }


    public AntResult start() {
        this.createAnts(this.ant_nums);
        int iter_count = 0;
        while (iter_count < this.iter_nums) {
            this.elite_ants.init(this.elite_ants_num);
            for (int i = 0; i < this.ant_nums; ++i) {
                if (!executorService.isShutdown()) {
                    completionService.submit(this.ants.get(i));
                }
            }
//            this.condition.wait();
            this.collectAntReport();    //这里面有for循环去得到Future实例 并 take,

            this.updateBest(this.elite_ants.getBest());

//            System.out.println("calcing delta");
            this.calcDelta();
            this.globalUpdateTau();

            iter_count += 1;
        }

        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this.best_ant;
    }

    public void createAnts(int ant_nums) {
        for (int i = 0; i < ant_nums; ++i) {
            this.ants.add(new Ant(i, 0, this));
        }
    }

    public void collectAntReport() {
        for (int i = 0; i < this.ant_nums; ++i) {
            try {
                // take() will 阻塞, 直到该线程有结果返回
                AntResult result = completionService.take().get();
                this.elite_ants.insert(result);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateBest(AntResult new_good) {
        if (null != this.best_ant) {
            if (new_good.getScore() < this.best_ant.getScore()) {
                this.best_ant = new_good;
            }
        } else {
            this.best_ant = new_good;
        }
    }

    public void calcDelta() {
        //最优蚂蚁
        for (int i = 0; i < this.graph.origin_points_num; ++i) {
            for (int j = 0; j < this.graph.origin_points_num; ++j) {
                if (this.best_ant.path_matrix.get(i).get(j) == 1) {
                    if (this.max_tau == -1) {
                        AntColony.Q = this.best_ant.getScore() * 0.025;
                        this.graph.initTauWithFirstBest(20 * AntColony.Q / this.best_ant.getScore());   //0.5
                        this.max_tau = 10 * this.graph.initiated_tau;   //5
                        this.min_tau = this.max_tau / 100;  //0.05

                    }
                    this.graph.accumulateDelta(i, j, (AntColony.BEST_THETA) * AntColony.Q / this.best_ant.getScore());
                }
            }
        }
        //精英蚂蚁
        for (int k = 0; k < this.elite_ants_num; ++k) {
            AntResult this_ant = this.elite_ants.getAnt(k);
            for (int i = 0; i < this.graph.origin_points_num; ++i) {
                for (int j = 0; j < this.graph.origin_points_num; ++j) {
                    if (this_ant.path_matrix.get(i).get(j) == 1) {
                        this.graph.accumulateDelta(i, j, (this.elite_ants_num - k) * AntColony.Q / this_ant.getScore());
                    }
                }
            }
        }
    }

    public void globalUpdateTau() {
        double old_tau, new_tau, new_tau_tmp, delta;
        for (int i = 0; i < this.graph.origin_points_num; ++i) {
            for (int j = 0; j < this.graph.origin_points_num; ++j) {
                old_tau = this.graph.getTau(i, j);
                delta = this.graph.getDelta(i, j);
                new_tau_tmp = AntColony.RHO * old_tau + (1 - AntColony.RHO) * delta;
                if (new_tau_tmp > this.max_tau) {
                    new_tau = this.max_tau;
                } else if (new_tau_tmp < this.min_tau) {
                    new_tau = this.min_tau;
                } else {
                    new_tau = new_tau_tmp;
                }
                this.graph.updateTau(i, j, new_tau);
            }
        }
    }

    private class EliteAnts {

        private ArrayList<AntResult> ants_list;
        private int length;

        public EliteAnts(int num) {
            this.length = num;
            this.ants_list = new ArrayList<AntResult>();
        }

        public void init(int num) {
            this.length = num;
            if (this.ants_list != null) {
                this.ants_list.clear();
            } else {
                this.ants_list = new ArrayList<AntResult>();
            }
        }

        public void insert(AntResult new_ant_result) {
            int ins_pos = this.ants_list.size();
            for (int i = 0; i < this.ants_list.size(); ++i) {
                if (new_ant_result.getScore() < this.ants_list.get(i).getScore()) {
                    ins_pos = i;
                    break;
                }
            }
            this.ants_list.add(ins_pos, new_ant_result);
            while (this.ants_list.size() > this.length) {
                this.ants_list.remove(this.ants_list.size() - 1); // remove last
            }
        }

        public AntResult getAnt(int rank) {
            return this.ants_list.get(rank);
        }

        public AntResult getBest() {
            return this.ants_list.get(0);
        }
    }
}
