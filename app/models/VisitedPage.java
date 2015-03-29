package models;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

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

    @ManyToMany
    @JoinTable(name = "visited_page_user",
            joinColumns = @JoinColumn(name = "visited_page_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    public List<User> users;

    private VisitedPage(String path, String title){
        this.path = path;
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VisitedPage that = (VisitedPage) o;

        if (!path.equalsIgnoreCase(that.path)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    public void removeVisitedPageByPath(String path){
        List<VisitedPage> pages = finder.where().eq("visitedpage.path", path).findList();
        for(VisitedPage page: pages){
            page.delete();
        }
    }

    public static VisitedPage getPage(String path, String title){
        VisitedPage page = finder.where().eq("path", path).findUnique();
        if(page == null){
            page = new VisitedPage(path, title);
        }
        return page;
    }
}
