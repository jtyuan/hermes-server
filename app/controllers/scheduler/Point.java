package controllers.scheduler;

import java.util.Map;

public class Point {
    private int index;
    private String orderID;
    private String address;
    private Pair<Integer, Integer> appointment;
    private int VIP_level;
    private int sign_need_time;
    private int status;

    public Point(){
        this.index = -1;
        //空的构造函数
    }
    public Point(int index, String orderID, String address, Pair<Integer, Integer> appointment, int VIP_level,
                 int sign_need_time, int status) {
        // index 每天订单拿来，订单就除了有订单号，还有index号
        // 其余的都是单词意义；status是说订单状态：0未发短信，1已发短信，2已送达，3有冲突/4问题
        this.index = index;
        this.orderID = orderID;
        this.address = address;
        this.appointment = appointment; //格式：Pair<... , ...> 0,max
        this.VIP_level = VIP_level;
        this.sign_need_time = sign_need_time;
        this.status = status;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index){
        this.index = index;
    }
    public String getOrderID() {
        return orderID;
    }
    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public int getSignNeedTime() {
        return sign_need_time;
    }
    public void setSignNeedTime(int sign_need_time) {
        this.sign_need_time = sign_need_time;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) throws Exception {
        if (status >= 0 && status < 5)
            this.status = status;
        else
            throw new Exception("Point status sets wrong value!");
    }
    public int getLevel() {
        return VIP_level;
    }
    public void setLevel(int VIP_level) {
        this.VIP_level = VIP_level;
    }
    public Pair<Integer, Integer> getTimeWindow() {
        return appointment;
    }
    public void setTimeWindow(Pair<Integer, Integer> appointment) {
        this.appointment = appointment;
    }
}
