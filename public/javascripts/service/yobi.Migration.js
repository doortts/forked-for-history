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
        .constant('SERVER_NAME', "http://127.0.0.1:9000")
        .controller('MigrationController', MigrationController)
        .service('migrationService', migrationService)
        .config(['$httpProvider', function($httpProvider) {
            $httpProvider.defaults.useXDomain = true;
            delete $httpProvider.defaults.headers.common['X-Requested-With'];
        }]);

    function MigrationController($http, migrationService) {
        /* jshint validthis: true */
        var vm = this;
        vm.project = {};
        vm.botToken = "f69f60b1a6cd2c3320bd3b19eb4052e528148f4f";
        vm.getProject = getProject;
        vm.redirect = function () {
            window.location = "https://oss.navercorp.com/login/oauth/authorize?client_id=bcb6c85038c14c0b8ade&scope=user,repo,admin:org";
        };
        vm.getToken = getToken;
        vm.postIssue = postIssue;

        activate();

        console.log('vm.code:', vm.code);

        ///////////////////////////
        function getToken(name){
            var BASE_URL = 'https://oss.navercorp.com/api/v3';
            console.log("token: " , vm.token);
            $http({
                method: 'GET',
                url: BASE_URL + '/search/repositories?q=+user:sw-chae',
                "headers": {
                    "Authorization": "token " + vm.token
                }
            }).then(function successCallback(response) {
                console.log(response);
                vm.targetProjects = response.data.items;
            }, function errorCallback(response) {
                console.log(response);
            });
        }

        function postIssue(){
            var BASE_API = 'https://oss.navercorp.com/api/v3';
            var issue = {
                title: "새로운 로고당 - " + Date.now(),
                body: "이슈가 이상합니다",
                labels: ["중요도 - 심각"],
                assignee: "sw-chae"
            };

            $http({
                method: 'POST',
                url: BASE_API + '/repos/sw-chae/sample/issues',
                "headers": {
                    "Authorization": "token " + vm.botToken
                },
                data: JSON.stringify(issue)
            }).then(function successCallback(response) {
                console.log(response);
            }, function errorCallback(response) {
                console.log(response);
            });
        }

        function getProject(owner, projectName){
            migrationService.getProject(owner, projectName).then(function (data) {
                vm.project = data;
                return vm.project;
            });
        }
        function activate() {
            // ToDo: health check
        }

    }

    function migrationService($http, SERVER_NAME) {
        return {
            getProject: getProject
        };

        //////////////////////////////
        function getProject(owner, projectName) {
            return $http({
                method: 'GET',
                url: SERVER_NAME + '/migration/' + owner + '/projects/' + projectName
            }).then(function successCallback(response) {
                console.log(response.data);
                return response.data;
            }, function errorCallback(response) {
                console.log('error');
            });
        }
    }
})("yobi.Migration");
