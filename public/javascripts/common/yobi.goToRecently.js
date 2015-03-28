/**
 * Yobi, Project Hosting SW
 *
 * Copyright 2015 NAVER Corp.
 * http://yobi.io
 *
 * @Author Suwon Chae
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

// Following code blocks are written for 'go to recently' select2 box feature
$(function(){
    var SELECT2_CONFIG = {
        "container-css-class" : "fullsize",
        formatResult: format,
        formatSelection: format,
        matcher: fuzzyMatcher
    };

    // add pjax event at GoTo-Recently button
    $("#goto-recently-container").pjax('a[data-pjax]', '#goto-recently-container', {push: false, timeout: 8000});

    $(document).keypress(function addShortcutKeyToGoToButton(event) {
        if (isShortcutKeyPressed(event)) {
            var target = $("#goto-link");
            target.trigger("click");
            preventAdditionalClick(target);
        }
    });

    $("#goto-recently-container").on('pjax:send', function(){
        var target = $("#goto-link");
        showLoading(target);
        preventAdditionalClick(target);
        NProgress.start()
    });

    $("#goto-recently-container").on('pjax:end', function(){
        $("#visitedPage").select2();    //prevent timing bug at chrome

        addShortcutAndUIEffectAtGoToRecently();
        addEventAtGoToRecentlySelectBox();
        addHideEventAtGoToDummyButton

        setTimeout(function(){  //prevent timing bug at chrome
            $("#visitedPage").select2(SELECT2_CONFIG);
            $('#visitedPage').select2("open");
        }, 1);
        NProgress.done();
    });

    $(document).on('pjax:timeout', function(e){
        $yobi.notify("Timeout! server is busy?", 5000);
        e.preventDefault();
    });

    function preventAdditionalClick(target) {
        target.on("click", function () {
            return false;
        });
    }

    function showLoading(target) {
        target.html("<span style='color: #51AACC'>loading... <i class='yobicon-loading'></i></span>");
        target.animate({width: "400px"});
    }

    function format(itemObject){
        var element = $(itemObject.element);
        var author = _extractAuthorOrOwner(element);
        var avatarURL = element.data("avatarUrl");

        if(_hasProjectAvatar(avatarURL)){
            return $yobi.tmpl($("#tplVisitedPageWithAvatar").text(), {
                "name"      : itemObject.text,
                "url"       : _extractProjectNameAndNo(element),
                "author"    : author,
                "avatarURL" : avatarURL
            });
        } else {
            return $yobi.tmpl($("#tplVisitedPage").text(), {
                "name"      : itemObject.text,
                "url"       : _extractProjectNameAndNo(element),
                "author"    : author
            });
        }

        function _extractAuthorOrOwner(itemElement) {
            var authorName = itemElement.data("author");
            if (authorName) {
                authorName = authorName.substring(0, authorName.lastIndexOf("@")); //abandon loginId from title
            }
            return authorName || itemElement.data("owner"); //user owner for author if author doesn't provide
        }

        function _extractProjectNameAndNo(element){ //parse project name if title is path
            var title = element.attr("title");
            if(title){
                var parsed = title.split("/");
                if (parsed[3] && _getPrefixForNoType(parsed[3])) {
                    return parsed[2] + _getPrefixForNoType(parsed[3]) + parsed[4] || title; //add # for issue
                }
            }
            return title;
        }

        function _hasProjectAvatar(avatarURL) {
            var DEFAULT_PROJECT_LOGO = "project_default_logo.png";
            var DEFAULT_ORGANIZATION_LOG = "group_default.png";
            return avatarURL
                && avatarURL.indexOf(DEFAULT_PROJECT_LOGO) == -1
                && avatarURL.indexOf(DEFAULT_ORGANIZATION_LOG) == -1
        }
    }

    function _getPrefixForNoType(type){
        var target = type.toLowerCase();
        if(target === "issue") {
            return " #";
        } else if (target === "post"){
            return " ";
        } else if (target === "pullrequest"){
            return " %";
        }
        return undefined;
    }

    var resultMap = {};  // for memoization
    var prevDepth = 0;       // index for memoization depth according to search text length

    function fuzzyMatcher(search, text, itemElement) {
        // remove garbage tooltip (tooltip bug)
        setTimeout(function(){
            $('.tooltip.right.in').remove();
        }, 10);
        var currentDepth = search.length;

        if(prevDepth > currentDepth){ // depth down. erase search text
            resultMap[prevDepth] = [];
            prevDepth = currentDepth;
        }

        if(prevDepth + 1 === currentDepth){ // at the first deeper state
            resultMap[currentDepth] = [];   // ready for new depth of memo
            prevDepth = currentDepth;
        }

        // preparations
        (function includePathStringAtSearch(){
            var path = itemElement.data("path");
            var parsedPath;
            var prefixForPage = "#/";
            if(path){
                parsedPath = path.split("/");
                text = parsedPath[2] + text + _getPrefixForNoType(parsedPath[3]) + parsedPath[4];    // projectName + no
                text = prefixForPage + text.replace(/\//g, ""); // remove slashs
            }
        }());

        (function includeAuthStringAtSearch(){
            var author = itemElement.data("author");
            var mentionPrefix = "@";
            if(author){
                text += mentionPrefix + author;
            }
        }());

        search = search.toUpperCase();
        text = text.toUpperCase();

        if(currentDepth > 1 && resultMap[currentDepth-1].indexOf(text) == -1){ // filtering start at 2th depth
            return false;
        }

        var j = -1; // remembers position of last found character

        // consider each search character one at a time
        for (var i = 0; i < search.length; i++) {
            var l = search[i];
            if (l == ' ') continue;     // ignore spaces

            j = text.indexOf(l, j+1);   // search for character & update position
            if (j == -1) {
                return false;
            }  // if it's not found, exclude this item
        }

        if (currentDepth !== 0){  // ignore when search text doesn't exist
            resultMap[currentDepth].push(text);
        }
        return true;
    }

    function addShortcutAndUIEffectAtGoToRecently() {
        // to prevent css width calculation bug when auto calculated width has point value

        if( $("#visitedPage").length ) {
            // to prevent flickering
            $("#visitedPage").css("visibility", "visible");
            $("#s2id_visitedPage").css("visibility", "visible");

            _openSelectBoxWithShortcutKey();
        }

        //in case of mouse click
        $('#visitedPage').on("select2-highlight, select2-opening", function(){
            $("ul.gnb-nav").hide();
            $("#s2id_visitedPage").show();
            _patchForWindows();
        });

        //resize select2 div to default width
        $('#visitedPage').on("select2-close", function(){
            // to bypass select2 malfunction that select2 focus never blurred automatically
            // when drop-down was closed.
            $("ul.gnb-nav").show(200);
            setTimeout(function(){
                $('.select2-container-active').removeClass('select2-container-active');
                $(':focus').blur();
                $("#visitedPage").hide();
                $("#s2id_visitedPage").hide();
                $("#goto-link-dummy").show();
                addHideEventAtGoToDummyButton();
            }, 1);
        });

        function _openSelectBoxWithShortcutKey() {
            $(document).keypress(function (event) {
                if (isShortcutKeyPressed(event)) {
                    $("ul.gnb-nav").hide();
                    $("#goto-link-dummy").hide();
                    setTimeout(function () {
                        $('#visitedPage').select(SELECT2_CONFIG);
                        $('#visitedPage').select2("open");
                    }, 1);
                }
            })
        }

        var _patchForWindows = function () {
            if(navigator.platform.toUpperCase() === "WIN32"){
                //select2 scrollbar is too thick in windows, so hide tooltip.
                //To prevent it, additional css is required.
                $(".select2-results").addClass("windowsWebkitScrollbar");
            }
        };
    }

    function addEventAtGoToRecentlySelectBox() {
        $("#visitedPage" ).on("change", function(choice){
            location.href= choice.val;
        });
    }

    // to prevent select2 box irregular style add dummy select box
    function addHideEventAtGoToDummyButton() {
        $("#goto-link-dummy").one("click", function(){
            $(this).hide();
            $("ul.gnb-nav").hide();
            setTimeout(function () {
                $('#visitedPage').select(SELECT2_CONFIG);
                $('#visitedPage').select2("open");
            }, 1);
        });
    }

    function isShortcutKeyPressed(event) {
        var activeElementName = $(document.activeElement).prop("tagName").toUpperCase();
        if(["INPUT","TEXTAREA"].indexOf(activeElementName) !== -1){ // avoid already somewhere input focused state
            return false;
        }
        return (event.which == 107 || event.which == 12623     // keycode => 107: k, 12623: ㅏ
            || event.which == 106 || event.which == 12627);     // keycode => 106: j, 12627: ㅓ
    }
});

