package controllers;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import configs.Constants;
import controllers.scheduler.*;
import models.Courier;
import models.Delivery;
import play.Application;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static play.data.Form.form;

/**
 * Manage a database of computers
 */
public class CourierController extends Controller {

    /**
     * This result directly redirect to application home.
     */
    public static Result GO_HOME = redirect(
        routes.CourierController.list(0, "name", "asc", "")
    );

    /**
     * Handle default path requests, redirect to computers list
     */
    public static Result index() {
        return GO_HOME;
    }

    /**
     * Display the paginated list of computers.
     *
     * @param page Current page number (starts from 0)
     * @param sortBy Column to be sorted
     * @param order Sort order (either asc or desc)
     * @param filter Filter applied on computer names
     */
    public static Result list(int page, String sortBy, String order, String filter) {
        return ok(
            courierlist.render(
                Courier.page(page, 10, sortBy, order, filter),
                sortBy, order, filter
            )
        );
    }

    /**
     * Display the 'edit form' of a existing Courier.
     *
     * @param id Id of the computer to edit
     */
    public static Result edit(Long id) {
        Form<Courier> courierForm = form(Courier.class).fill(
                Courier.find.byId(id)
        );
        return ok(
            editCourier.render(id, courierForm)
        );
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the computer to edit
     */
    public static Result update(Long id) {
        Form<Courier> courierForm = form(Courier.class).bindFromRequest();
        if(courierForm.hasErrors()) {
            return badRequest(editCourier.render(id, courierForm));
        }
        courierForm.get().update(id);
        flash("success", "Courier " + courierForm.get().courier_id + " has been updated");
        return GO_HOME;
    }

    /**
     * Display the 'new computer form'.
     */
    public static Result create() {
        Form<Courier> courierForm = form(Courier.class);
        return ok(
            createCourier.render(courierForm)
        );
    }

    /**
     * Handle the 'new computer form' submission
     */
    public static Result save() {
        Form<Courier> courierForm = form(Courier.class).bindFromRequest();
        if(courierForm.hasErrors()) {
            return badRequest(createCourier.render(courierForm));
        }
        courierForm.get().save();
        flash("success", "Courier " + courierForm.get().name + " has been created");
        return GO_HOME;
    }

    /**
     * Handle computer deletion
     */
    public static Result delete(Long id) {
        Courier.find.ref(id).delete();
        flash("success", "Courier has been deleted");
        return GO_HOME;
    }

    public static Result schedule(String id, String date) {
        List<Delivery> deliveries = Delivery.find.where().like("courier.courier_id", id).findList();

        Collections.sort(deliveries, (d1, d2) -> d1.arrive_time - d2.arrive_time);
        for (int i = 0; i < Constants.MAX_INFORM; ++i) {
            DeliveryController.inform(deliveries.get(i).order_id, String.valueOf(Constants.STATUS_READY));
        }

        List<ResultOrder> results = AntColonyVRPTW.schedule(deliveries, Delivery.find.all().size());
//        List<ResultOrder> results = AntColonyVRPTW.testSchedule();
        for (ResultOrder ro : results) {
            ro.setCourierID(id);
        }
        ResultWrapper resultWrapper = new ResultWrapper("success", id, results);
        resultWrapper.updateDeliveryDatabase();
        System.out.println(ok(Json.toJson(resultWrapper)));
        return ok(Json.toJson(resultWrapper));
    }

    public static Result fetch(String id, String date) {
        List<Delivery> deliveries = Delivery.find.where().like("courier.courier_id", id).findList();

        Collections.sort(deliveries, (d1, d2) -> d1.arrive_time - d2.arrive_time);
//        for (int i = 0; i < Constants.MAX_INFORM; ++i) {
//            DeliveryController.inform(deliveries.get(i).order_id, String.valueOf(Constants.STATUS_READY));
//        }

        ResultWrapper resultWrapper = new ResultWrapper("success", id, ResultWrapper.toResultOrderList(deliveries));
        System.out.println(ok(Json.toJson(resultWrapper)));
        return ok(Json.toJson(resultWrapper));
    }


    public static Result getLoc(String id) {
        Courier courier = Courier.find.where().eq("courier_id", id).findUnique();
        ObjectNode result = Json.newObject();
        if (courier != null) {
            result.put("status", "ok");
            result.put("lat", courier.current_lat);
            result.put("lon", courier.current_lon);
        } else {
            result.put("status", "failed");
        }
        return ok(result);
    }

    public static Result updateLoc(String id) {
        DynamicForm dynamicForm = Form.form().bindFromRequest();
        ObjectNode result = Json.newObject();
        String lat = dynamicForm.get("lat");
        String lon = dynamicForm.get("lon");
        System.out.println("updateLoc: " + lat + "," + lon);
        if(lat == null) {
            result.put("status", "failed");
            result.put("msg", "Missing parameter [lat]");
        } else if (lon == null) {
            result.put("status", "failed");
            result.put("msg", "Missing parameter [long]");
        } else {
            Courier courier = Courier.find.where().eq("courier_id", id).findUnique();
            if (courier != null) {
                courier.current_lat = Double.valueOf(lat);
                courier.current_lon = Double.valueOf(lon);
                courier.save();
                result.put("status", "ok");
                result.put("msg", "sccuess");
            } else {
                result.put("status", "failed");
                result.put("msg", "courier not exist");
            }
        }
        return ok(result);

    }


    @SuppressWarnings("Duplicates")
    private static class ResultWrapper {
        String status;
        String courierID;
        List<ResultOrder> orders;

        public ResultWrapper(String status, String courierID, List<ResultOrder> orders) {
            this.status = status;
            this.courierID = courierID;
            this.orders = orders;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCourierID() {
            return courierID;
        }

        public void setCourierID(String courierID) {
            this.courierID = courierID;
        }

        public List<ResultOrder> getOrders() {
            return orders;
        }

        public void setOrders(List<ResultOrder> orders) {
            this.orders = orders;
        }

        public void updateDeliveryDatabase() {
            Courier courier = Courier.find.where().like("courier_id", courierID).findUnique();
            for (ResultOrder o : orders) {
//                System.out.println("orderid" + o.getOrderID());
//                for (Delivery d : Delivery.find.where().like("order_id", o.getOrderID()).findList()) {
//                    System.out.println(d.order_id + "," + d.address);
//                }
                Delivery delivery = Delivery.find.where().like("order_id", o.getOrderID()).findUnique();
                delivery.arrive_time = o.getArrive_time();
                delivery.leave_time = o.getLeave_time();
                delivery.wait_time = o.getWait_time();
                delivery.status = o.getStatus();
                delivery.msg = o.getFailure_reason();
                delivery.courier = courier;
                delivery.save();

                o.setName(delivery.name);
                o.setPhone(delivery.phone);
            }
        }

        public static List<ResultOrder> toResultOrderList(List<Delivery> deliveries) {
            List<ResultOrder> result = new ArrayList<>();
            for (Delivery d : deliveries) {
                ResultOrder ro = new ResultOrder();
                ro.setOrderID(d.order_id);
                ro.setPhone(d.phone);
                ro.setName(d.name);
                ro.setAddress(d.address);
                ro.setAppointment("" + d.appointment_time_begin + ',' + d.appointment_time_end);
                ro.setSign_need_time(d.sign_time);
                ro.setStatus(d.status);
                ro.setVip_level(1);
                ro.setArrive_time(d.arrive_time);
                ro.setWait_time(d.wait_time);
                ro.setLeave_time(d.leave_time);
                ro.setFailure_reason(d.msg);
                ro.setCourierID(d.courier.courier_id);
                if (d.real_time != null) {
                    ro.setReal_time(d.real_time);
                } else {
                    ro.setReal_time(0);
                }
                result.add(ro);
            }
            return result;
        }
    }
}

