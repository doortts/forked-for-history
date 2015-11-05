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
        .service('migrationService', migrationService);

    function MigrationController(migrationService) {
        /* jshint validthis: true */
        var vm = this;
        vm.project = {};
        vm.getProject = getProject;

        activate();

        ///////////////////////////
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
