package controllers.insertion;

import controllers.scheduler.DonePoint;
import controllers.scheduler.Point;

import java.util.ArrayList;
import java.util.HashMap;

public class InsertionRecommend {
    private ArrayList<Point> points;    //跟already_schedule顺序一致的points
    private ArrayList<Double> time_u_come;
    private ArrayList<Double> time_i_go;
    private ArrayList<DonePoint> already_schedule;
    private int insertion_point_index;
    private ArrayList<HashMap<String, Integer>> available_pos_list;
    private int inaccuracy;
    private int positions_num;

    public InsertionRecommend(ArrayList<Point> points, ArrayList<ArrayList<Double>> time_matrix,
                              ArrayList<DonePoint> already_schedule, int insertion_point_index){
        this.points = points;
        this.time_u_come = time_matrix.get(0);
        this.time_i_go = time_matrix.get(1);
        this.already_schedule = already_schedule;
        this.insertion_point_index = insertion_point_index;  //始终都应该是最后一个
        this.available_pos_list = new ArrayList<>(); //可插入的位置列表，里面每一项是位置和其插在这里带来的 push forward 值
        this.inaccuracy = 20*60;    //允许的新加时间窗大小
        this.positions_num = this.already_schedule.size()-1; //中间非队尾处可插入的位置数，也即队尾点的index
    }

    public ArrayList<HashMap<String, Integer>> recommend(){
        for(int pos = 0; pos <= this.positions_num; pos++){
//      对每个可插入的位置都要测试一下，因为我按前点标记插入，所以最后一个插入点的标记应该还要-1
            HashMap<String, Integer> available_insert_pos = this.calc_available_pos(pos, this.positions_num, this.insertion_point_index);
            if(available_insert_pos.get("insert_pos") > 0){
                this.available_pos_list.add(available_insert_pos);
            }
        }
        return this.available_pos_list;
    }

    public HashMap<String, Integer> calc_available_pos(int insert_pos, int last_pos, int insertion_point_index){
        //insert_pos是： 试着插到 already_schedule中 insert_pos 和 insert_pos+1 之间
        HashMap<String, Integer> insert_info = new HashMap<>(); //初始化
        insert_info.put("insert_pos", -1);

        if(insert_pos == last_pos){
            //插在队尾肯定可以
            insert_info.put("insert_pos", insert_pos);
            insert_info.put("push_forward", 0);
            int time_window_a = this.calc_earliest_time(insert_info);
            insert_info.put("new_time_window_a", time_window_a);
            insert_info.put("new_time_window_b", time_window_a + this.inaccuracy);
        }else{
            DonePoint point_i = this.already_schedule.get(insert_pos);
            DonePoint point_j = this.already_schedule.get(insert_pos+1);
            int arrive_j_time_old = point_j.getArrive_time();
            //如果插入，j点新的到达时间是 i点的离开的时间 + i->u时间 + u点服务时间 + u->j时间
            double arrive_j_time_new = point_i.getLeave_time() + this.time_u_come.get(insert_pos);
            arrive_j_time_new += this.points.get(insertion_point_index).getSignNeedTime() + this.time_i_go.get(insert_pos+1);
            //计算因为插入这个点带来的最开始的前推值
            int push_forward = Math.max(0, (int)arrive_j_time_new - arrive_j_time_old);
            //检测这个点是否ok
            if(this.push_forward_test(insert_pos, last_pos, push_forward)){
                //不可以插入的话,insert_info里只有初始的一个-1
                insert_info.put("insert_pos", insert_pos);
                insert_info.put("push_forward", push_forward);
                //push_forward方法往后面的点推，查看是否有矛盾，只能找到最早可行的时间点，也当然可以判断是否可以插入
                int time_window_a = this.calc_earliest_time(insert_info);
                //还要找到最晚可行的时间点。another自创算法
                int time_window_b = this.calc_latest_time(insert_info);
                insert_info.put("new_time_window_a", time_window_a);
                insert_info.put("new_time_window_b", time_window_b);
            }
        }
        return insert_info;
    }

    public boolean push_forward_test(int insert_pos, int last_pos, int push_forward_value){
        int push_forward = push_forward_value;
        for(int k = insert_pos; k < last_pos; k++){
            //判断这个点k插入，其后一个点的到达时间会不会大于时间窗后限，而影响后一个点的满足性
            if(this.already_schedule.get(k+1).getArrive_time() + push_forward > this.points.get(k+1).getTimeWindow().getValue()){
                return false;   //#只要有一个不满足就return false
            }else{
                //如果满足，计算经过这个点以后，前推值的变化
                push_forward = Math.max(0, push_forward - this.already_schedule.get(k+1).getWait_time());
            }
        }
        return true;    //没有一个点不满足,就满足
    }

    public int calc_earliest_time(HashMap<String, Integer> insert_info){
        //前一点离开时间+到插入点时间,即插入点的起始时间窗
        int insert_pos = insert_info.get("insert_pos");
        int a_leave = this.already_schedule.get(insert_pos).getLeave_time();
        double a_to_insertion = this.time_u_come.get(insert_pos);
        return (int)(a_leave + a_to_insertion);
    }

    public int calc_latest_time(HashMap<String, Integer> insert_info){
        //这个算法是从最后一个点开始倒推可松弛时间，然后计算出插入点的后一个点的最晚到达时间，即计算出插入点的最晚到达时间
        int insert_pos = insert_info.get("insert_pos");
        //先计算队尾处的最晚开始时间和可推迟时间
        int tail_index = this.positions_num;
        int latest_start_time = this.points.get(tail_index).getTimeWindow().getValue();
        int relax_time = latest_start_time - this.already_schedule.get(tail_index).getArrive_time();
        for(int i = 1; i < this.positions_num - insert_pos; i++){
            //#倒着往前循环，因为倒数第一个在上一行算过了，从倒数第二个开始；并且一直算到insert_pos+1处
            //#先算出虚拟最晚离开，可以得到虚拟最晚开始，再跟时间窗右边界比较，得到真实的最晚开始，与现实到达时间相减，得到新的relax_time往前一个点传
            int index = this.positions_num - i;
            int guess_latest_leave_time = this.already_schedule.get(index).getLeave_time() + relax_time;
            int guess_latest_start_time = guess_latest_leave_time - this.points.get(index).getSignNeedTime();
            latest_start_time = Math.min(guess_latest_start_time, this.points.get(index).getTimeWindow().getValue());
            relax_time = latest_start_time - this.already_schedule.get(index).getArrive_time();
        }
        //#现在就拿到了insert_pos+1这个点的最晚开始时间，该值减去插入点到该点所需时间即插入点最晚离开时间，再减去插入点需要的服务时间即得到插入点最晚开始时间
        //#points的最后一个是待插入的点，比already长一个，所以最后一个是tail+1
        return (int) (latest_start_time - this.time_i_go.get(insert_pos+1) - this.points.get(tail_index+1).getSignNeedTime());
    }

    public ArrayList<DonePoint> insert(int insert_pos){
        //要知道该点准备插在哪里
        ArrayList<DonePoint> tmp = new ArrayList<>();   //空的,作为失败的回复
        HashMap<String, Integer> insert_info = new HashMap<>(); //初始化
        insert_info.put("insert_pos", -1);
        insert_info = this.calc_available_pos(insert_pos, this.positions_num, this.insertion_point_index);
        if(insert_info.get("insert_pos")>-1){
            //可以插入
            return this.modify_already_schedule(insert_info, this.positions_num);
        }else{
            return tmp;
        }
    }
    public ArrayList<DonePoint> modify_already_schedule(HashMap<String, Integer> insert_info, int last_pos){
        //    #调整插入该点后already_schedule的结果
        int insert_pos = insert_info.get("insert_pos");
        int push_forward = insert_info.get("push_forward");
        int wait_time = 0;
        int new_time_window_a = insert_info.get("new_time_window_a");
        int arrive_time = new_time_window_a;
        //开始改already_schedule数组的值,最后再插入
        for(int k = insert_pos; k < last_pos; k++){
            DonePoint schedule_point = this.already_schedule.get(k+1);
            Point point = this.points.get(k+1);
            wait_time = schedule_point.getWait_time();
            if(schedule_point.getArrive_time() + push_forward >= point.getTimeWindow().getKey()){
                //已经进了时间窗
                schedule_point.setArrive_time(schedule_point.getArrive_time() + push_forward);
                schedule_point.setWait_time(0);
                schedule_point.setLeave_time(schedule_point.getArrive_time() + point.getSignNeedTime());
            }else{
                //没进时间窗
                schedule_point.setArrive_time(schedule_point.getArrive_time() + push_forward);
                schedule_point.setWait_time(schedule_point.getWait_time() - push_forward);
            }
            push_forward = Math.max(0, push_forward - wait_time);
        }
        //该改的改完了,对于already schedule来说,point index是没有意义的了,所以设什么都无所谓
        this.already_schedule.add(insert_pos+1, new DonePoint(-1, this.points.get(this.insertion_point_index).getOrderID(),
                arrive_time, 0, arrive_time+this.points.get(this.insertion_point_index).getSignNeedTime()));
        return this.already_schedule;
    }
}
