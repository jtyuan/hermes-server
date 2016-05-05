package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

/**
 * Delivery entity managed by Ebean
 */
@Entity 
public class Delivery extends Model {

    private static final long serialVersionUID = 1L;

	@Id
    public Long id;

    @Constraints.Required
    @Column(unique = true)
    public String order_id;

    @Constraints.Required
    public String name;

    public String phone;

    @Formats.DateTime(pattern="yyyy-MM-dd")
    public Date appointment_date;

    public Integer appointment_time_begin;

    public Integer appointment_time_end;

    public Integer sign_time;

    public Integer status;

    public Integer arrive_time;

    public Integer wait_time;

    public Integer leave_time;

    public Long real_time;

    public String msg;

    @Constraints.Required
    public String address;
    
    @ManyToOne
    public Courier courier;
    
    /**
     * Generic query helper for entity Delivery with id Long
     */
    public static Finder<Long, Delivery> find = new Finder<Long, Delivery>(Long.class, Delivery.class);
    
    /**
     * Return a page of delivery task
     *
     * @param page Page to display
     * @param pageSize Number of computers per page
     * @param sortBy Delivery property used for sorting
     * @param order Sort order (either or asc or desc)
     * @param filter Filter applied on the name column
     */
    public static Page<Delivery> page(int page, int pageSize, String sortBy, String order, String filter) {
        return 
            find.where()
                .ilike("order_id", "%" + filter + "%")
                .orderBy(sortBy + " " + order)
                .fetch("courier")
                .findPagingList(pageSize)
                .setFetchAhead(false)
                .getPage(page);
    }
    
}

