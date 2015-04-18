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

    @OneToMany(mappedBy = "visitedPage", cascade = CascadeType.ALL)
    public List<UserVisitedPage> userVisitedPages;

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

    public UserVisitedPage getUserVisitedPage(User user){
        for(UserVisitedPage page: this.userVisitedPages){
            if(page.user.equals(user)){
                return page;
            }
        }
        return null;
    }
}
