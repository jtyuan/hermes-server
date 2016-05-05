package controllers;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import configs.Constants;
import controllers.evaluator.MultiThreadWeb;
import controllers.scheduler.*;
import play.Application;
import play.Play;
import play.data.format.Formats;
import play.libs.Json;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;

import views.html.*;

import models.*;

import java.io.*;
import java.util.*;

/**
 * Manage a database of computers
 */
public class DeliveryController extends Controller {
    
    /**
     * This result directly redirect to application home.
     */
    public static Result GO_HOME = redirect(
        routes.DeliveryController.list(0, "name", "asc", "")
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
            list.render(
                Delivery.page(page, 10, sortBy, order, filter),
                sortBy, order, filter
            )
        );
    }
    
    /**
     * Display the 'edit form' of a existing Delivery.
     *
     * @param id Id of the delivery to edit
     */
    public static Result edit(Long id) {
        Form<Delivery> deliveryForm = form(Delivery.class).fill(
            Delivery.find.byId(id)
        );
        return ok(
            editForm.render(id, deliveryForm)
        );
    }
    
    /**
     * Handle the 'edit form' submission 
     *
     * @param id Id of the delivery to edit
     */
    public static Result update(Long id) {
        Form<Delivery> deliveryForm = form(Delivery.class).bindFromRequest();
        if(deliveryForm.hasErrors()) {
            return badRequest(editForm.render(id, deliveryForm));
        }
        deliveryForm.get().update(id);
        flash("success", "Delivery " + deliveryForm.get().order_id + " has been updated");
        return GO_HOME;
    }
    
    /**
     * Display the 'new computer form'.
     */
    public static Result create() {
        Form<Delivery> deliveryForm = form(Delivery.class);
        return ok(
            createForm.render(deliveryForm)
        );
    }
    
    /**
     * Handle the 'new computer form' submission 
     */
    public static Result save() {
        Form<Delivery> deliveryForm = form(Delivery.class).bindFromRequest();
        if(deliveryForm.hasErrors()) {
            return badRequest(createForm.render(deliveryForm));
        }
        deliveryForm.get().save();
        flash("success", "Delivery " + deliveryForm.get().order_id + " has been created");
        return GO_HOME;
    }
    
    /**
     * Handle computer deletion
     */
    public static Result delete(Long id) {
        Delivery.find.ref(id).delete();
        flash("success", "Delivery has been deleted");
        return GO_HOME;
    }

    @SuppressWarnings("Duplicates")
    public static Result init() {
        if (Play.isDev()) {
            final Application app = Play.application();

            String json_str = "";
            //从文件中读入json串
            File jsonFile = app.getFile("public/data/100points.json");
            try {
                FileReader jsonFileReader = new FileReader(jsonFile);
                BufferedReader buffer = new BufferedReader(jsonFileReader);
                try {
                    json_str = buffer.readLine();
                    System.out.println(json_str);
                    buffer.close();
                    jsonFileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //解析json把point生成一下
            ArrayList<controllers.scheduler.JsonOrder> JsonOrderArray = new ArrayList<>(JSONArray.parseArray(json_str, JsonOrder.class));
            String[] addresses = new String[JsonOrderArray.size()];
            ArrayList<Point> points = new ArrayList<>();
//        for (int i = 0; i < JsonOrderArray.size(); ++i) {
            ArrayList<Delivery> dlist = new ArrayList<>();
            for (int i = 0; i < JsonOrderArray.size(); ++i) {
                Delivery delivery = new Delivery();
                JsonOrder order = JsonOrderArray.get(i);
//            System.out.println(order.getSignTime());
                Point p = new Point(i, order.getOrderID(), order.getAddress(), new Pair<>(order.getAppointment().get(0), order.getAppointment().get(1)),
                        1, order.getSignTime(), 0);
                //order.getVip_level()
                points.add(p);
                addresses[i] = p.getAddress();
                if (Delivery.find.where().like("order_id", order.getOrderID()).findList().size() == 0) {
                    delivery.order_id = order.getOrderID();
                    delivery.name = RandomValue.getChineseName();
                    delivery.phone = RandomValue.getTel();
                    delivery.address = order.getAddress();
                    delivery.sign_time = order.getSignTime();
                    delivery.status = Constants.STATUS_UNINFORMED;
                    delivery.appointment_time_begin = order.getAppointment().get(0);
                    delivery.appointment_time_end = order.getAppointment().get(1);
                    delivery.save();
                    dlist.add(delivery);
                }
            }
/*
            //去百度要
            MultiThreadWeb dis_time_req = new MultiThreadWeb();
            dis_time_req.request(addresses, 0); //开始要,后面会阻塞

            File dist_mat_file = app.getFile("public/data/dist_mat.json");
            File time_mat_file = app.getFile("public/data/time_mat.json");
            try {
                FileWriter distMatFileWriter = new FileWriter(dist_mat_file);
                BufferedWriter distBuffer = new BufferedWriter(distMatFileWriter);
                distBuffer.write(Json.toJson(dis_time_req.DistMatrix).toString());
                distBuffer.close();
                distMatFileWriter.close();


                FileWriter timeMatFileWriter = new FileWriter(time_mat_file);
                BufferedWriter timeBuffer = new BufferedWriter(timeMatFileWriter);
                timeBuffer.write(Json.toJson(dis_time_req.TimeMatrix).toString());
                timeBuffer.close();
                timeMatFileWriter.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return ok(Json.toJson(dlist));*/
        }
        return ok("Not in DEV mode");
    }

    public static Result reset() {
        if (Play.isDev()) {
            List<Delivery> deliveries = Delivery.find.where().gt("arrive_time", 0).findList();
            for (Delivery d : deliveries) {
                d.status = Constants.STATUS_UNINFORMED;
                d.save();
            }
            Collections.sort(deliveries, (d1, d2) -> d1.arrive_time - d2.arrive_time);
            for (int i = 0; i < Constants.MAX_INFORM; ++i) {
                Delivery delivery = deliveries.get(i);
                DeliveryController.inform(delivery.order_id, String.valueOf(Constants.STATUS_READY));
            }
            return ok("Done");
        }
        return ok("Not in DEV mode");
    }

    public static Result clear() {
        if (Play.isDev()) {
            List<Delivery> deliveries = Delivery.find.all();
            for (Delivery d : deliveries)
                d.delete();
            return ok("Done");
        }
        return ok("Not in DEV mode");
    }

    public static Result inform(String id, String s) {

        Delivery delivery = Delivery.find.where().like("order_id", id).findUnique();

        if (s.equals(String.valueOf(Constants.STATUS_UNINFORMED))) {
            delivery.status = Constants.STATUS_UNINFORMED;
            delivery.save();
        } else if (s.equals(String.valueOf(Constants.STATUS_READY))) {
            delivery.status = Constants.STATUS_READY;
            delivery.save();
            // TODO pushToUser(id);
        } else if (s.equals(String.valueOf(Constants.STATUS_COMPLETED))) {
            if (delivery.status != Constants.STATUS_COMPLETED) {
                delivery.status = Constants.STATUS_COMPLETED;
                delivery.msg = "配送成功";
                delivery.real_time = System.currentTimeMillis() / 1000;
                delivery.save();

                List<Delivery> deliveries = Delivery.find.where()
                        .eq("status", Constants.STATUS_UNINFORMED)
                        .gt("arrive_time", 0).findList();

                Collections.sort(deliveries,
                        (d1, d2) -> d1.arrive_time - d2.arrive_time);
                if (deliveries.size() > 0) {
                    inform(deliveries.get(0).order_id, String.valueOf(Constants.STATUS_READY));
                }
            }
        } else {
            if (delivery.status != Constants.STATUS_FAILED) {
                delivery.status = Constants.STATUS_FAILED;
                delivery.msg = s;
                delivery.real_time = System.currentTimeMillis() / 1000;
                delivery.save();

                List<Delivery> deliveries = Delivery.find.where()
                        .eq("status", Constants.STATUS_UNINFORMED)
                        .gt("arrive_time", 0).findList();

                Collections.sort(deliveries,
                        (d1, d2) -> d1.arrive_time - d2.arrive_time);
                if (deliveries.size() > 0) {
                    inform(deliveries.get(0).order_id, String.valueOf(Constants.STATUS_READY));
                }
            }
        }

        System.out.println(id + " informed with " + s);

        ObjectNode result = Json.newObject();
        result.put("status", "ok");
        result.put("msg", "订单[" + id + "]修改完成");
        result.put("informed_order", Json.toJson(delivery));

        return ok(result);
    }

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

    }
}
            
