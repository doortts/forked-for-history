package models;

import controllers.UserApp;
import org.apache.commons.collections.CollectionUtils;
import play.cache.Cache;

import java.util.List;

public class PageCache {
    static final int MAX_VISITED_PAGE_SIZE = 100;

    public static boolean existUserInUserCache() {
        return Cache.get(UserApp.currentUser().loginId) != null;
    }

    public static void updateGlobalCache(List<UserVisitedPage> cachedPages) {
        for(UserVisitedPage page: cachedPages){
            VisitedPage.globalPageMap.putIfAbsent(page.getPath(), page.visitedPage);
        }
    }

    public static boolean existsPageHistoryInDB() {
        return UserApp.currentUser().userVisitedPages.size() > 0;
    }

    @SuppressWarnings("unchecked")
    static List<UserVisitedPage> getUserVisitedPages() {
        return (List<UserVisitedPage>) Cache.get(UserApp.currentUser().loginId);
    }

    public static void removeVisitPageFromCacheByPath(String path){
        List <UserVisitedPage> pages = getUserVisitedPages();

        if(CollectionUtils.isEmpty(pages)){
            return;
        }

        UserVisitedPage userVisitedPage = null;
        for(UserVisitedPage page: pages){
            if(page.hasSamePath(path)){
                VisitedPage.globalPageMap.remove(path);
                userVisitedPage = page;
            }
        }
        pages.remove(userVisitedPage);
    }

    public static UserVisitedPage getCurrentPage(String path, String title){
        List <UserVisitedPage> cachedPages = getUserVisitedPages();

        if (cachedPages == null) {
            return null;
        }

        for(UserVisitedPage cachedPage: cachedPages){
            if(cachedPage.hasSamePath(path)){   // cache hit
                UserVisitedPage currentPageAtCache = cachedPage;

                cachedPages.remove(currentPageAtCache);    // for ordering
                currentPageAtCache.setTitle(title);        // title may be updated
                cachedPages.add(0, currentPageAtCache);    // for ordering
                return currentPageAtCache;
            }
        }

        return null;
    }

    public static void deleteOldPage() {
        List<UserVisitedPage> cachedPages = getUserVisitedPages();

        if(cachedPages == null){
            return;
        }

        if(cachedPages.size() > MAX_VISITED_PAGE_SIZE){
            cachedPages.remove(cachedPages.size()-1);
        }
    }
}
