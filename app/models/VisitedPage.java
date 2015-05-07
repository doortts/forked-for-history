package models;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.libs.F;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static play.libs.F.*;

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
    public Long lastCommentAddedTime = 0L;

    @OneToMany(mappedBy = "visitedPage", cascade = CascadeType.ALL)
    public List<UserVisitedPage> userVisitedPages;

    @Transient
    public static final ConcurrentHashMap<String, VisitedPage> globalPageMap = new ConcurrentHashMap<>();

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

    public static void updateLastCommentAddedTime(final String path, final Long lastCommentAddedTime){
        VisitedPage globalCachedPage = VisitedPage.getPageFromGlobalCache(path);
        if(globalCachedPage != null){
            globalCachedPage.lastCommentAddedTime = lastCommentAddedTime;
        }
        Promise.promise(
                new F.Function0<Void>() {
                    public Void apply() throws IllegalAccessException, InstantiationException {
                        VisitedPage page = VisitedPage.findPageByPath(path);
                        if(page != null){
                            page.lastCommentAddedTime = lastCommentAddedTime;
                            try{
                                page.save();
                            } catch (Exception e){
                                play.Logger.error("cached page to db saving is fail: " + path);
                            }
                        }
                        return null;
                    }
                }
        );
    }

    public void updatePage(String title, Long lastCommentAddedTime) {
        this.lastCommentAddedTime = lastCommentAddedTime;
        this.title = title;
    }
}
