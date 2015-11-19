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
(function(ns){
    angular.module('yobi.migration', [])
        .constant("config", {
            "YOBI_SERVER": "http://127.0.0.1:9000",
            "GITHUB_API_BASE_URL": "https://oss.navercorp.com/api/v3",
            "IMPORT_API_ACCEPT_HEADER": "application/vnd.github.golden-comet-preview+json",
            "BOT_TOKEN": "34cf61c529904948472c7a8895d1a7c52c188d0a"
        })
        .controller('MigrationController', MigrationController)
        .service('migrationService', migrationService)
        .config(['$httpProvider', function($httpProvider) {
            $httpProvider.defaults.useXDomain = true;
            delete $httpProvider.defaults.headers.common['X-Requested-With'];
        }]);

    ///////////////////////////////////////////////
    //// controller
    ///////////////////////////////////////////////
    function MigrationController($http, $log, config, migrationService) {
        /* jshint validthis: true */
        var vm = this;
        vm.project = {};
        vm.destinationProjects = [];
        vm.importResult = {};

        vm.getProject = getProject;
        vm.redirect = function () {
            window.location = "https://oss.navercorp.com/login/oauth/authorize?client_id=bcb6c85038c14c0b8ade&scope=user,repo,admin:org";
        };
        vm.getDestinationProjects = getDestinationProjects;
        vm.importIssues = importIssues;


        activate();

        ///////////////////////////
        function getDestinationProjects(){
            migrationService.getDestinationProjects().then(function(response){
                vm.destinationProjects = response;
            });
        }

        function importIssues(owner, projectName){
            migrationService.importIssues(owner, projectName).then(function(result){
                $log.info("result", result);
                vm.importResult = result;
            });
        }

        function getProject(owner, projectName){
            migrationService.getProject(owner, projectName).then(function (data) {
                vm.project = data;
                vm.source = data.projectName;
                return vm.project;
            });
        }

        function activate() {
            // ToDo: health check
        }

    }

    ///////////////////////////////////////////////
    //// service
    ///////////////////////////////////////////////

    function migrationService($http, $log, config) {
        var importResult = {
            count: 0,
            errorData: []
        };
        return {
            getProject: getProject,
            getIssues: getIssues,
            getDestinationProjects: getDestinationProjects,
            importIssues: importIssues
        };

        //////////////////////////////
        function getProject(owner, projectName) {
            return $http({
                method: 'GET',
                url: config.YOBI_SERVER + '/migration/' + owner + '/projects/' + projectName
            }).then(function successCallback(response) {
                $log.log(response.data);
                return response.data;
            }, function errorCallback(response) {
                $log.error('error');
            });
        }

        function getIssues(owner, projectName) {
            return $http({
                method: 'GET',
                url: config.YOBI_SERVER  + '/migration/' + owner + '/projects/' + projectName + '/issues'
            }).then(function successCallback(response) {
                $log.info(response.data);
                return response.data;
            }, function errorCallback(response) {
                $log.error(response);
            });
        }

        function importIssues(owner, projectName){
            var BASE_API = config.GITHUB_API_BASE_URL;
            var fullRepoName = owner + '/' + projectName;

            return $http({
                method: 'GET',
                url: "/migration/dlab/projects/yobi2ux/issues"
            }).then(function success(response) {
                $log.info("from", response);
                response.data.forEach(function(data){
                    mappingForImport(data);

                    $http({
                        method: 'POST',
                        url: BASE_API + '/repos/' + fullRepoName + '/import/issues',
                        "headers": {
                            "Authorization": "token " + config.BOT_TOKEN,
                            "Accept": "application/vnd.github.golden-comet-preview+json"
                        },
                        data: JSON.stringify(data)
                    }).then(function success(response) {
                        importResult.count++;
                        if(response.status !== 202){
                            importResult.errorData.push(response);
                        }
                        $log.debug("imported", response);
                    }, function error(response) {
                        importResult.errorData.push(response);
                        $log.error("a", response);
                    });
                });
                return importResult;
            }, function error(response) {
                $log.error("b", response);
            });

            function mappingForImport(data){
                // issue
                data.issue.assignee = 'sw-chae';
                //data.issue.milestone = 1;
                if("body" in data.issue && !data.issue.body){
                    var NO_CONTENTS = "-- no contents --";
                    $log.log(NO_CONTENTS);
                    data.issue.body = NO_CONTENTS;
                }
            }
        }

        function getDestinationProjects(){
            var BASE_URL = config.GITHUB_API_BASE_URL;
            var page = 1;
            var perpage = 30;
            var projects = [];
            var MAX_PAGE_FOR_GATHERING = 10;

            return gatheringLists();

            /////////////////////////////////////
            function gatheringLists(){
                return $http({
                    method: 'GET',
                    url: BASE_URL + '/user/repos?per_page=' + perpage + '&page=' + page,
                    "headers": {
                        "Authorization": "token " + config.BOT_TOKEN
                    }
                }).then(success, error);
            }
            function success(response){
                projects = projects.concat(response.data);
                if(response.data.length === perpage){
                    page++;
                    if( page < MAX_PAGE_FOR_GATHERING ) return gatheringLists();
                }
                return projects;
            }
            function error(response){
                $log.error("c", response);
                throw response.status + " : " + response.data;
            }
        }
    }
})("yobi.Migration");
