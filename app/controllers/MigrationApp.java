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
import play.libs.Json;
import play.mvc.Result;
import views.html.migration.home;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static play.mvc.Results.ok;

@AnonymousCheck
public class MigrationApp {
    @AnonymousCheck(requiresLogin = true, displaysFlashMessage = true)
    public static Result migration() {
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
        return ok(home.render("Migration",
                targetProjects.stream().sorted((p1, p2) -> p1.owner.compareTo(p2.owner)).collect(Collectors.toList())));
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
        return ok(result);
    }
}
