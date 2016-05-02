package controllers.scheduler;

/**
 * Created by Qiao on 4/9/16.
 */
public class DonePoint{
    private int point_index;
    private String orderID;
    private int arrive_time;
    private int wait_time;
    private int leave_time;

    public DonePoint(){

    }
    public DonePoint(int point_index, String orderID, int arrive_time, int wait_time, int leave_time){
        this.point_index = point_index;
        this.orderID = orderID;
        this.arrive_time = arrive_time;
        this.wait_time = wait_time;
        this.leave_time = leave_time;
    }
    public String toString(){
        return  "orderID: " + this.orderID + ", arrive_time: " + this.arrive_time
                + ", wait_time: " + this.wait_time + ", leave_time: " + this.leave_time;
    }

    public int getPoint_index() {
        return point_index;
    }

    public void setPoint_index(int point_index) {
        this.point_index = point_index;
    }

    public int getArrive_time() {
        return arrive_time;
    }

    public void setArrive_time(int arrive_time) {
        this.arrive_time = arrive_time;
    }

    public int getWait_time() {
        return wait_time;
    }

    public void setWait_time(int wait_time) {
        this.wait_time = wait_time;
    }

    public int getLeave_time() {
        return leave_time;
    }

    public void setLeave_time(int leave_time) {
        this.leave_time = leave_time;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }
}
