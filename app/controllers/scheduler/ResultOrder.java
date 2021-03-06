package controllers.scheduler;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by bilibili on 16/4/19.
 */
public class ResultOrder implements Comparator<ResultOrder> {

    private String orderID;
    private String courierID;
    private String name;
    private String phone;
    private String address;
    private String appointment;
    private int sign_need_time;
    private int status;
    private int vip_level;
    private int arrive_time;
    private int wait_time;
    private int leave_time;
    private long real_time;
    private String failure_reason;

    public String getCourierID() {
        return courierID;
    }

    public void setCourierID(String courierID) {
        this.courierID = courierID;
    }

    public long getReal_time() {
        return real_time;
    }

    public void setReal_time(long real_time) {
        this.real_time = real_time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getAppointment() {
        return appointment;
    }

    public void setAppointment(String appointment) {
        this.appointment = appointment;
    }

    public int getSign_need_time() {
        return sign_need_time;
    }

    public void setSign_need_time(int sign_need_time) {
        this.sign_need_time = sign_need_time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getVip_level() {
        return vip_level;
    }

    public void setVip_level(int vip_level) {
        this.vip_level = vip_level;
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

    public String getFailure_reason() {
        return failure_reason;
    }

    public void setFailure_reason(String failure_reason) {
        this.failure_reason = failure_reason;
    }

    @Override
    public int compare(ResultOrder o1, ResultOrder o2) {
        return (o1.arrive_time - o2.arrive_time >= 0) ? 1 : -1;
    }
}
