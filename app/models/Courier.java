package models;

import com.avaje.ebean.Page;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.Constraint;
import java.util.LinkedHashMap;
import java.util.Map;



/**
 * Courier entity managed by Ebean
 */
@Entity 
public class Courier extends Model {

    private static final long serialVersionUID = 1L;

	@Id
    public Long id;

    @Constraints.Required
    @Column(unique = true)
    public String courier_id;

    @Constraints.Required
    public String name;

    @ManyToOne
    public WorkingArea working_area;

    public String phone;

    public Double current_lat;

    public Double current_lon;

    public String access_token;

    public String refresh_token;

    public long expires_by;

    /**
     * Generic query helper for entity Courier with id Long
     */
    public static Model.Finder<Long, Courier> find = new Model.Finder<Long, Courier>(Long.class, Courier.class);

    public static Map<String,String> options() {
        LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
        for(Courier c: Courier.find.orderBy("courier_id").findList()) {
            options.put(c.id.toString(), "[" + c.courier_id + "]" + c.name);
        }
        return options;
    }

    /**
     * Return a page of courier
     *
     * @param page Page to display
     * @param pageSize Number of couriers per page
     * @param sortBy Courier property used for sorting
     * @param order Sort order (either or asc or desc)
     * @param filter Filter applied on the name column
     */
    public static Page<Courier> page(int page, int pageSize, String sortBy, String order, String filter) {
        return
                find.where()
                        .ilike("courier_id", "%" + filter + "%")
                        .orderBy(sortBy + " " + order)
                        .findPagingList(pageSize)
                        .getPage(page);
    }

}

