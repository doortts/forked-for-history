개요
----

Yobi 프로젝트는 Jenkins를 이용해서 Continuous Intergration을 수행하고 있다.

Jenkins 서버 URL: http://ci.yobi.navercorp.com/jenkins/

Projects
--------

Jenkins 서버는 다음의 Project를 통해 Continuous Intergration을 수행한다.
Project에 대한 설정 변경은 서버에 접속할 수 있는 누구나 할 수 있다.

### PJT-C-Yobi-master

yobi.navercorp.com/dlab/hive의 master 브랜치에 새로운 커밋이 들어오면 빌드하고
유닛테스트를 실행한 뒤, 실패한 경우 nforge@navercorp.com에 알림 메일을
발송한다.

### PJT-C-Yobi-next

yobi.navercorp.com/dlab/hive의 next 브랜치에 새로운 커밋이 들어오면 빌드하고
유닛테스트와 findbugs를 실행한 뒤, 빌드 상태가 unstable 혹은 failed 인 경우
nforge@navercorp.com에 알림 메일을 발송한다.

### PJT-C-Yobi-pu

yobi.navercorp.com/dlab/hive의 pu 브랜치에 새로운 커밋이 들어오면 빌드하고
유닛테스트와 findbugs를 실행한다. 알림 메일은 발송하지 않는다.

### PJT-C-Yobi-topics

yobi.navercorp.com/dlab/hive에서, 다음 문단에서 기술할 특정 브랜치를 제외한
어떤 브랜치에 새로운 커밋이 들어오면 빌드하고 유닛테스트와 findbugs를 실행한
뒤, 빌드 상태가 unstable 혹은 failed 인 경우 nforge@navercorp.com에 알림 메일을
발송한다.

이 Project에서 제외되는 브랜치는 다음과 같다.

* master
* next
* pu
* internal
* 브랜치 이름에 "internal"로 시작하는 path segment가 포함된 모든 브랜치 (예:
  internal, interal123, internal-issue, internal/issue, docs/internal/issue 등)

FIXME: 빌드를 깨뜨린 사람에게만 메일이 발송되도록 하려 했지만 메일이 가지 않는
문제가 있어서 그만두었다. 빌드를 깬 사람(curlpit)에게만 메일을 발송하려고 하면
빌드 로그에서는 메일 수신자 리스트가 비어있어서 메일을 보내지 않는다고
기록된다. 새 브랜치가 만들어진 경우나, 브랜치를 리베이스한 경우에는 Jenkins가
빌드를 깨뜨린 사람을 알아내지 못하는 것 같다.

Jenkins 시작방법
----------------

irteam 계정으로 다음의 쉘 명령으로 tomcat과 apache를 시작하면, Jenkins가 시작된다.

    /home1/irteam/scripts/webapps.sh start

주의: sudo로 irteam 권한만 획득해서 실행하면(sudo -u irteam), Jenkins가
Project를 빌드할 때 Git을 실행할 권한이 없어 에러가 발생할 수 있다.

재시작하려면 start 대신 restart를, 멈추려면 stop 명령을 사용한다.

Jenkins 업그레이드
------------------

1. 최신버전의 Jenkins를 다운받아 /home1/irteam/deploy에 설치한다. (war라면 압축을 푼다)
2. 심볼릭 링크 /home1/irteam/deploy/jenkins 가 새로 설치한 Jenkins를 가리키도록 고친다.
3. Jenkins를 재시작한다.

서버
----

ncloud 가상서버를 사용하고 있다.

이름: dev-yobi-ci.ncl
스펙: 8vCPU, 16GB Mem
담당자: 이응준

서버에 대한 재시작/중단/스펙 변경 등의 관리 작업은
[ncloud 웹사이트](http://ncloud.nhncorp.com/)에서 할 수 있다.

### 동작

* Tomcat이 WAS로서 Jenkins를 구동하고 있다. 포트는 9000번을 사용한다.
* Apache가 80번 포트로 들어오는 모든 요청을 9000번 포트로 포워딩하는 프락시
  역할을 하고 있으며, URL의 path가 /인 요청을 /jenkins/로 리다이렉트하는 일도
  하고 있다.

### 각종 경로

* Apache: /home1/irteam/apps/apache
* Tomcat: /home1/irteam/apps/tomcat
* Jenkins: /home1/irteam/deploy/jenkins
    * Project가 수행되는 workspace: /home1/irteam/.jenkins/workspace
* Apache/Tomcat 로그: /home1/irteam/logs

Notes
-----

findbugs가 *.scala 파일은 무시하도록 설정했다. 왜냐하면 findbugs가 scala 코드에
대해 불필요한 warning을 발생시키기 때문이다. [1][2] 이 설정은 Jenkins가 아니라
Yobi 프로젝트에서 해도 되겠지만, 때때로 유용한 경고를 해 주기도 하기 때문에
그렇게 하지는 않았다.

[1]: https://github.com/Netflix/archaius/issues/85
[2]: http://stackoverflow.com/questions/22617713/whats-the-current-state-of-static-analysis-tools-for-scala
