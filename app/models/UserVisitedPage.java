package models;

import play.db.ebean.Model;

import javax.persistence.*;

import static play.data.validation.Constraints.*;

@Entity
public class UserVisitedPage extends Model {

    private static final long serialVersionUID = 1627478585879491228L;

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
}
