package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javassist.tools.web.BadHttpRequest;
import models.Courier;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Created by bilibili on 16/5/2.
 */
public class OAuthController extends Controller {


    // Called by auth server, for redirection
    @BodyParser.Of(BodyParser.Json.class)
    public static Result login() {
        DynamicForm dynamicForm = Form.form().bindFromRequest();
        ObjectNode result = Json.newObject();
//        JsonNode json = request().body().asJson();

        System.out.println("???");
        String courier_id = dynamicForm.get("courier_id");
        System.out.println(courier_id);
        String access_token = dynamicForm.get("access_token");
        String refresh_token = dynamicForm.get("refresh_token");
        String expires_in = dynamicForm.get("expires_in");
        if(access_token == null) {
//            return badRequest("Missing parameter [access_token]");
            result.put("result", "failed: missing [access_token]");
        } else {
            if (refresh_token == null) {
//                return badRequest("Missing parameter [refresh_token]");

            } else {
                Courier courier = Courier.find.where().like("courier_id", courier_id).findUnique();

                if (courier == null) {
                    result.put("result", 404);
                    return ok(result);
                }

                System.out.println(expires_in);
                courier.access_token = access_token;
                courier.refresh_token = refresh_token;
                courier.expires_by = System.currentTimeMillis()/1000 + Long.valueOf(expires_in);
                courier.save();

                result.put("result", 200);
            }
        }
        return ok(result);
    }

    // Called by client
    public static Result authentication(String id) {
        ObjectNode result = Json.newObject();
        JsonNode json = request().body().asJson();

        String access_token = json.findPath("access_token").textValue();
        if(access_token == null) {
//            return badRequest("Missing parameter [access_token]");
            result.put("result", "failed: missing [access_token]");
        } else {
            Courier courier = Courier.find.where().like("courier_id", String.valueOf(id)).findUnique();

            if (courier == null) {
                result.put("result", 404);
                return ok(result);
            }

            if (courier.expires_by >= System.currentTimeMillis()/1000) {
                if (courier.access_token.equals(access_token)) {
                    result.put("result", 200);
                } else {
                    result.put("result", 203);
                }
            } else {
                result.put("result", 503);
            }
        }
        return ok(result);
    }
}
