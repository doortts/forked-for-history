개요
----

Yobi 프로젝트는 Jenkins를 이용해서 Continuous Intergration을 수행하고 있다.

Jenkins 서버 URL: http://ci.yobi.navercorp.com/jenkins/

Job
---

Jenkins 서버는 다음의 Job을 통해 Continuous Intergration을 수행한다. Job에 대한
설정 변경은 서버에 접속할 수 있는 누구나 할 수 있다.

### PJT-C-Yobi-master

yobi.navercorp.com/dlab/hive의 master 브랜치에 새로운 커밋이 들어오면 빌드하고
유닛테스트를 실행한 뒤, 실패한 경우 nforge@navercorp.com에 알림 메일을
발송한다.

### PJT-C-Yobi-next

yobi.navercorp.com/dlab/hive의 next 브랜치에 새로운 커밋이 들어오면 빌드하고
유닛테스트를 실행한 뒤, 실패한 경우 nforge@navercorp.com에 알림 메일을
발송한다.

### PJT-C-Yobi-all

yobi.navercorp.com/dlab/hive에서 master, next, internal을 제외한 어떤 브랜치에
새로운 커밋이 들어오면 빌드하고 유닛테스트를 실행한 뒤, 실패한 경우 빌드를
깨뜨린 사람(새로 들어온 커밋(들)의 저자)에게 메일을 발송한다.

FIXME: 이상하게 빌드를 깨뜨린 사람에게도 메일이 발송되지 않고 있는 것으로
보인다. 빌드 로그에서는 메일 수신자 리스트가 비어있어서 메일을 보내지 않는다고
기록된다. 새 브랜치가 만들어진 경우나, 브랜치를 리베이스한 경우에는 Jenkins가
빌드를 깨뜨린 사람을 알아내지 못하는 것 같다.

Jenkins 시작방법
----------------

irteam 계정으로 다음의 쉘 명령으로 tomcat과 apache를 시작하면, Jenkins가 시작된다.

    /home1/irteam/scripts/webapps.sh start

주의: sudo로 irteam 권한만 획득해서 실행하면(sudo -u irteam), Jenkins Job 수행
시 Git을 실행할 권한이 없어 에러가 발생할 수 있다.

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
