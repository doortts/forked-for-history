### 요약

[Yobi 프로젝트 Git 저장소]의 master, next, pu 브랜치의 최신 소스코드가
반영되어있는 Yobi 인스턴스를 각각 master.yobi.navercorp.com,
next.yobi.navercorp.com, pu.yobi.navercorp.com 에서 테스트해볼 수 있다.

브랜치별로 각각 도메인이 할당되어있지만, 테스트 서버는 test-n4.ncl 한대이다.

### 어떻게 하나의 테스트 서버에서 여러 인스턴스를 실행하는가

test-n4.ncl의 80번 포트로 HTTP 요청이 들어오면
아파치 웹 서버가 Host 헤더의 호스트명이 무엇이냐에 따라서
다음과 같이 포트포워딩한다.

* master.yobi.navercorp.com => localhost:9000
* next.yobi.navercorp.com => localhost:9001
* pu.yobi.navercorp.com => localhost:9002

위의 동작은 /home1/irteam/apps/apache/conf/httpd.conf 에 설정되어있다.

### 테스트 서버는 최신 소스코드를 어떻게 반영하고 있는가

3초에 한번씩 [Yobi 프로젝트 Git 저장소]에서 최신 소스코드를 가져와 각각의 Yobi
인스턴스에 반영한다.

이 일은 pullpoll.sh이라는 쉘 스크립트가 수행하고 있다. 만약 최신 소스코드가
반영되고 있지 않다면 pullpoll.sh 스크립트가 실행중인지 확인하고 아니라면 다음과
같이 실행한다.

    nohup /home/irteamsu/repos/pullpoll.sh &

### 각 Yobi 인스턴스의 실행

/home1/irteamsu/repos/yobi-<브랜치명>/restart.sh 를 실행하여 특정 Yobi
인스턴스를 시작 혹은 재시작 할 수 있다.

### 테스트할 대상을 추가하는 법

추가할 대상이 test 브랜치를 테스트하는 test.yobi.navercorp.com 이라면:

* test.yobi.navercorp.com 도메인 확보
* /home1/irteamsu/repos/yobi-test 에 Yobi 인스턴스를 설치
* 쉽게 Yobi 인스턴스를 재시작 할 수 있도록 재시작 스크립트를
  /home1/irteamsu/repos/yobi-test/restart.sh 로 작성
* Yobi 인스턴스 재시작
* 설치한 Yobi 인스턴스가 항상 최신 소스코드를 반영할 수 있도록
  /home1/irteamsu/repos/pullpoll.sh 를 수정
* /home1/irteamsu/repos/pullpoll.sh 를 kill하고 재시작
* 아파치가 로그를 남길 수 있도록 /data/logs/apache/<도메인이름> 을 추가
* /home1/irteam/apps/apache/conf/httpd.conf 에 가상 호스팅 설정 추가
* 아파치 재시작

### 테스트할 대상을 제거하는 법

제거할 대상이 test.yobi.navercorp.com 이라면:

* 아파치 중단
* /home1/irteamsu/repos/yobi-test에 설치된 Yobi 인스턴스 중단
* /home1/irteamsu/repos/yobi-test 삭제
* /home1/irteamsu/repos/pullpoll.sh 를 수정하여 불필요해진 업데이트 코드 삭제
* /home1/irteamsu/repos/pullpoll.sh 를 kill하고 재시작
* /home1/irteam/apps/apache/conf/httpd.conf 에서 test.yobi.navercorp.com에 대한
  가상 호스팅 설정 제거
* 아파치 시작
* 도메인 반납

### 디렉토리 정보

* Apache: /home1/irteam/apps/apache
* Yobi instances
    * /home1/irteamsu/repos/yobi-master
    * /home1/irteamsu/repos/yobi-next
    * /home1/irteamsu/repos/yobi-pu
* Logs
    * /data/logs/apache/master.yobi.navercorp.com
    * /data/logs/apache/next.yobi.navercorp.com
    * /data/logs/apache/pu.yobi.navercorp.com

[Yobi 프로젝트 Git 저장소]: http://yobi.navercorp.com/dlab/hive
