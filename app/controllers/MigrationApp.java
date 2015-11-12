/**
 * Yobi, Project Hosting SW
 * <p>
 * Copyright 2015 NAVER Corp.
 * http://yobi.io
 *
 * @author Suwon Chae
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.annotation.AnonymousCheck;
import models.*;
import models.support.SearchCondition;
import play.libs.F;
import play.libs.F.Promise;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Result;
import views.html.migration.home;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static play.mvc.Http.Context.Implicit.request;
import static play.mvc.Results.ok;

@AnonymousCheck
public class MigrationApp {
    @AnonymousCheck(requiresLogin = true, displaysFlashMessage = true)
    public static Promise<Result> migration() {
        Set<Project> targetProjects = new HashSet<>();
        User worker = UserApp.currentUser();
        for (OrganizationUser organizationUser : worker.organizationUsers) {
            if (OrganizationUser.isAdmin(organizationUser.organization, worker)) {
                targetProjects.addAll(organizationUser.organization.projects);
            }
        }
        for (ProjectUser projectUser : worker.projectUser) {
            if (ProjectUser.isAllowedToSettings(worker.loginId, projectUser.project)) {
                targetProjects.add(projectUser.project);
            }
        }
        String code = request().getQueryString("code");
        if(code != null ){
            final String CLIENT_ID = "bcb6c85038c14c0b8ade";
            final String CLIENT_SECRET = "e7a2b142550afe1d9262d2008afd2db331f4a0b2";
            final String ACCESS_TOKEN_URL = "https://oss.navercorp.com/login/oauth/access_token";

            Promise<String> resultPromise = WS.url(ACCESS_TOKEN_URL)
                .setContentType("application/x-www-form-urlencoded")
                .post("client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&code=" + code)
                .map( new F.Function<WSResponse, String>() {
                    public String apply(WSResponse response) throws Throwable {
                        play.Logger.debug(response.getBody());
                        String accessToken = "";
                        try {
                            Pattern p = Pattern.compile("access_token=([^&]+)");
                            Matcher m = p.matcher(response.getBody());
                            if(m.find() ){
                                accessToken = m.group(1);
                            }
                        } catch (PatternSyntaxException ex) {
                            play.Logger.error("Couldn't find access_token");
                        }
                        return accessToken;
                    }
                });
            return resultPromise.map(new F.Function<String, Result>() {
                public Result apply(String token) {
                    return ok(home.render("Migration",
                            targetProjects.stream().sorted((p1, p2) -> p1.owner.compareTo(p2.owner)).collect(Collectors.toList()), code, token));
                }
            });
        } else {
            return Promise.promise(new F.Function0<Result>() {
                @Override
                public Result apply() throws Throwable {
                    return ok(home.render("Migration",
                            targetProjects.stream().sorted((p1, p2) -> p1.owner.compareTo(p2.owner)).collect(Collectors.toList()), code, null));
                }
            });
        }

    }

    public static Result project(String owner, String projectName){
        Project project = Project.findByOwnerAndProjectName(owner, projectName);
        ObjectNode result = Json.newObject();
        result.put("owner", project.owner);
        result.put("projectName", project.name);
        List<ObjectNode> members = new ArrayList<>();
        for(ProjectUser projectUser: project.members()){
            ObjectNode member = Json.newObject();
            member.put("name", projectUser.user.name);
            member.put("loginId", projectUser.user.loginId);
            members.add(member);
        }
        result.put("members", Json.toJson(members.toArray()));
        result.put("issueCount", Issue.countIssuesBy(project.id, new SearchCondition()));
        result.put("boardCount", Posting.countPostings(project));
        return ok(result);
    }
}
