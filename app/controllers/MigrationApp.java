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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static play.libs.Json.toJson;
import static play.mvc.Http.Context.Implicit.request;
import static play.mvc.Results.ok;

@AnonymousCheck
public class MigrationApp {

    public static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final String YOBI_SERVER = "http://yobi.navercorp.com/";

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
                    .setHeader("Accept", "application/json,application/x-www-form-urlencoded,text/html,*/*")
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
                    return ok(home.render("Migration", sortProjectsByOwnerAndName(targetProjects), code, token));
                }
            });
        } else {
            return Promise.promise(new F.Function0<Result>() {
                @Override
                public Result apply() throws Throwable {
                    return ok(home.render("Migration", sortProjectsByOwnerAndName(targetProjects), null, null));
                }
            });
        }

    }

    private static List<Project> sortProjectsByOwnerAndName(Set<Project> targetProjects) {
        Comparator<Project> comparator = Comparator.comparing(project -> project.owner);
        comparator = comparator.thenComparing(Comparator.comparing(project -> project.name));
        List<Project> list = new ArrayList<>(targetProjects);
        Collections.sort(list, comparator);
        return list;
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
        result.put("members", toJson(members.toArray()));
        result.put("issueCount", Issue.countIssuesBy(project.id, new SearchCondition()));
        result.put("boardCount", Posting.countPostings(project));
        return ok(result);
    }

    public static Result exportLabels(String owner, String projectName){
        Project project = Project.findByOwnerAndProjectName(owner, projectName);

        List<ObjectNode> labels = new ArrayList<>();
        for (IssueLabel label : project.issueLabels) {
            ObjectNode node = Json.newObject();
            node.put("id", label.id);
            node.put("name", label.name);
            node.put("categoryId", label.category.id);
            node.put("categoryName", label.category.name);
            labels.add(node);
        }

        ObjectNode exportData = Json.newObject();
        exportData.put("labels", toJson(labels));

        return ok(exportData);
    }

    public static Result exportMilestones(String owner, String projectName){
        Project project = Project.findByOwnerAndProjectName(owner, projectName);

        List<ObjectNode> milestones = project.milestones.stream()
                .map(MigrationApp::composeMilestoneJson).collect(Collectors.toList());

        ObjectNode exportData = Json.newObject();
        exportData.put("milestones", toJson(milestones));

        return ok(exportData);
    }

    public static Result exportIssues(String owner, String projectName){
        Project project = Project.findByOwnerAndProjectName(owner, projectName);

        List<ObjectNode> issues = project.issues.stream()
                .map((issue) -> MigrationApp.composeIssueJson(issue)).collect(Collectors.toList());

        ObjectNode exportData = Json.newObject();
        exportData.put("issues", toJson(issues));

        return ok(exportData);
    }

    private static ObjectNode composeMilestoneJson(Milestone m) {
        ObjectNode node = Json.newObject();
        node.put("id", m.id);
        node.put("title", m.title);
        node.put("state", m.state.state());
        node.put("description", m.contents);
        Optional.ofNullable(m.dueDate).ifPresent(dueDate -> node.put("due_on", DF.format(m.dueDate)));

        ObjectNode milestoneJson = Json.newObject();
        milestoneJson.put("milestone", node);
        return milestoneJson;
    }

    private static String addOriginalAuthorName(String text, String authorLoginId, String authorName, String type, String link){
        return "`@" + authorLoginId + " (" + authorName + ") 님이 작성한 '" + type + "'입니다.` [[link]]("
        + link + ") \n\n" + text;
    }

    private static ObjectNode composeIssueJson(Issue issue) {
        ObjectNode node = Json.newObject();

        String orgLink = YOBI_SERVER + issue.project.owner + "/" + issue.project.name + "/issue/" + issue.getNumber();
        node.put("title", issue.title);
        Optional.ofNullable(issue.body).ifPresent(body -> node.put("body",
                addOriginalAuthorName(body, issue.authorLoginId, issue.authorName, "이슈", orgLink)));
        node.put("created_at", DF.format(issue.createdDate));
        Optional.ofNullable(issue.assignee).ifPresent(assignee -> node.put("assignee", assignee.user.loginId));
        Optional.ofNullable(issue.milestone).ifPresent(milestone -> node.put("milestone", milestone.title));
        Optional.ofNullable(issue.milestone).ifPresent(milestone -> node.put("milestoneId", milestone.id));
        node.put("closed", issue.isClosed());

//        List<String> labels = issue.labels.stream().map(label -> label.category.name + " - " + label.name).collect(Collectors.toList());
//        node.put("labels", toJson(labels));

        ObjectNode issueJson = Json.newObject();
        List<ObjectNode> comments = new ArrayList<>();
        for (IssueComment comment : issue.comments) {
            ObjectNode commentNode = Json.newObject();
            commentNode.put("created_at", DF.format(comment.createdDate));
            commentNode.put("body",
                    addOriginalAuthorName(comment.contents, comment.authorLoginId, comment.authorName, "코멘트", orgLink + "#comment-" + comment.id));
            comments.add(commentNode);
        }
        issueJson.put("issue", node);
        issueJson.put("comments", toJson(comments));
        return issueJson;
    }
}
