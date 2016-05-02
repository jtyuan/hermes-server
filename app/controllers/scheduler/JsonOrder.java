package controllers.scheduler;
import java.util.ArrayList;

public class JsonOrder{
    private String orderID;
    private String address;
    private ArrayList<Integer> appointment;
//    private int vip_level;
    private int signTime;

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

    public ArrayList<Integer> getAppointment() {
        return appointment;
    }

    public void setAppointment(ArrayList<Integer> appointment) {
        this.appointment = appointment;
    }

    public int getSignTime() {
        return signTime;
    }

    public void setSignTime(int signTime) {
        this.signTime = signTime;
    }

//    public int getVip_level() {
//        return vip_level;
//    }
//
//    public void setVip_level(int vip_level) {
//        this.vip_level = vip_level;
//    }
}