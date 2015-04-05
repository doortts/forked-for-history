package models;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class VisitedPage extends Model{

    private static final long serialVersionUID = 4525565617284206707L;
    private static Finder<Long, VisitedPage> finder = new Finder<>(Long.class, VisitedPage.class);

    @Id
    public Long id;

    @Required @Column(unique=true)
    public String path;

    @Required
    public String title;
    public Long lastCommentAddedTime;

    @ManyToMany
    @JoinTable(name = "visited_page_user",
            joinColumns = @JoinColumn(name = "visited_page_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    public List<User> users;

    @Transient
    public static final Map<String, VisitedPage> globalPageMap = new HashMap<>();

    public VisitedPage(String path, String title, Long lastCommentAddedTime){
        this.path = path;
        this.title = title;
        this.lastCommentAddedTime = lastCommentAddedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VisitedPage that = (VisitedPage) o;
        return path.equalsIgnoreCase(that.path);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    public static VisitedPage findPageByPath(String path){
        return finder.where().eq("path", path).findUnique();
    }

    public static VisitedPage getPageFromGlobalCache(String path){
       return globalPageMap.get(path); // secondary history cache
    }
}
