/**
 * Yobi, Project Hosting SW
 *
 * Copyright 2015 NAVER Corp.
 * http://yobi.io
 *
 * @author Suwon Chae
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package controllers;

import controllers.annotation.AnonymousCheck;
import models.Project;
import models.ProjectUser;
import models.User;
import play.mvc.Result;
import views.html.migration.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static play.mvc.Results.ok;

@AnonymousCheck
public class MigrationApp {
    @AnonymousCheck(requiresLogin = true, displaysFlashMessage = true)
    public static Result migration(){
        List<Project> targetProjects = new ArrayList<>();
        User worker = UserApp.currentUser();
        for(ProjectUser projectUser: worker.projectUser){
            if( ProjectUser.isAllowedToSettings(worker.loginId, projectUser.project)){
                targetProjects.add(projectUser.project);
            }
        }
        Collections.sort(targetProjects, (p1, p2) -> p1.owner.compareTo(p2.owner));
        return ok(home.render("Migration", targetProjects));
    }
}
