package models;

import controllers.UserApp;
import org.joda.time.DateTime;
import play.db.ebean.Model;

import javax.persistence.*;

import java.util.List;

import static play.data.validation.Constraints.*;

@Entity
public class UserVisitedPage extends Model {

    private static final long serialVersionUID = 1627478585879491228L;
    private static Finder<Long, UserVisitedPage> finder = new Finder<>(Long.class, UserVisitedPage.class);

    @Id
    public Long id;

    @ManyToOne
    public User user;

    @ManyToOne
    public VisitedPage visitedPage;

    @Required
    public Long lastVisitedTime;

    public UserVisitedPage(User user, VisitedPage visitedPage, Long lastVisitedTime) {
        this.user = user;
        this.visitedPage = visitedPage;
        this.lastVisitedTime = lastVisitedTime;
    }

    public String getPath(){
        return this.visitedPage.path;
    }

    public String getTitle() {
        return this.visitedPage.title;
    }

    public void setTitle(String title) {
        this.visitedPage.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserVisitedPage that = (UserVisitedPage) o;

        if (user != null ? !user.equals(that.user) : that.user != null) return false;
        return !(visitedPage != null ? !visitedPage.equals(that.visitedPage) : that.visitedPage != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (visitedPage != null ? visitedPage.hashCode() : 0);
        return result;
    }

    @Override
    public void delete(){
        if(this.id != null){
            super.delete();
        }
    }

    public static List<UserVisitedPage> getOrderByVisitedDate(){
        return finder.where().eq("user.id", UserApp.currentUser().id).order("lastVisitedTime desc").findList();
    }

    public void updateUserVisitedPage(String title, Long lastCommentAddedTime) {
        lastVisitedTime = DateTime.now().getMillis();
        visitedPage.updatePage(title, lastCommentAddedTime);
    }

    /**
     * move current page to the top of recently visited page list
     */
    public void updateUserVisitedPagesOrder() {
        List<UserVisitedPage> userCache = User.getRecentlyVisitedPages();
        if (userCache.contains(this)){
            userCache.remove(this); //for ordering
        }
        userCache.add(0, this); //for ordering
    }

    public boolean hasSamePath(String path){
        return getPath().equalsIgnoreCase(path);
    }
}
