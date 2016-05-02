package models;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by bilibili on 16/4/28.
 */
@Entity
public class WorkingArea extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public String city;

    public String dist;

    @Column(unique = true)
    public int hashcode;

    public WorkingArea() {
        super();
        hashcode = (city + dist).hashCode();
    }

    /**
     * Generic query helper for entity Courier with id Long
     */
    public static Model.Finder<Long, WorkingArea> find = new Model.Finder<Long, WorkingArea>(Long.class, WorkingArea.class);

    public static Map<String,String> options() {
        LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
        for(WorkingArea c: WorkingArea.find.orderBy("city").findList()) {
            options.put(c.id.toString(), c.city + ' ' + c.dist);
        }
        return options;
    }
}