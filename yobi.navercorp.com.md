## 요약

이 문서는 네이버 사내 공식 Yobi 인스턴스인 yobi.navercorp.com 의 운영 방법에 대해 안내한다.

## Yobi 재시작

    kill `cat /data/yobi/RUNNING_PID`
    _JAVA_OPTIONS="-Xmx12g -Xms4g -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:gc.log -XX:PermSize=256m -XX:MaxPermSize=256m" nohup /data/yobi/bin/yobi -Dyobi.home=/data/yobi-data -DapplyEvolutions.default=true -DapplyDownEvolutions.default=true -Dhttp.port=9000 3>&1 1>&2 2>&3 | tee -a /data/yobi/logs/stderr &

## Yobi 업그레이드

v0.8.0으로 업그레이드하려고 한다면:

    cd /data/yobi-src
    git checkout v0.8.0
    activator clean dist
    unzip target/universal/yobi-0.8.0.zip -d /data
    cd /data/yobi-0.8.0
    kill `cat /data/yobi/RUNNING_PID` # 여기서 Yobi 중단
    rm /data/yobi                     # 기존 Yobi를
    ln -s /data/yobi-0.8.0 /data/yobi # 새 Yobi로 교체
    _JAVA_OPTIONS="-Xmx12g -Xms4g -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:gc.log -XX:PermSize=256m -XX:MaxPermSize=256m" nohup /data/yobi/bin/yobi -Dyobi.home=/data/yobi-data -DapplyEvolutions.default=true -DapplyDownEvolutions.default=true -Dhttp.port=9000 3>&1 1>&2 2>&3 | tee -a /data/yobi/logs/stderr &

## Yobi Mailbox 기능 사용하기

Yobi의 mailbox 기능을 사용하려면 다음의 안내에 따라 IMAP 서버를 설치하고 Yobi에 imap 서버를 설정해야한다.

### 준비1: IMAP 서버 설치

ntree를 통해 MX 레코드 등록을 요청해둔다.

dovecot과 sendmail을 설치한다. sendmail이 아마도 sendmail-cf를 요구할 것이므로 sendmail-cf도 같이 설치한다.

    sudo yum install dovecot sendmail sendmail-cf

/etc/dovecot/dovecot.conf를 수정해서 imap을 사용할 수 있게한다. (imaps는 추가할 필요가 없다. 설정한다면 dovecot을 재시작할 때 필요없다고 경고를 낼 것이다)

    protocols = imap

/etc/mail/sendmail.mc 에서 아래 내용을 주석처리한다. (sendmail을 설치해야 존재함)

    dnl DAEMON_OPTIONS(`Port=smtp,Addr=127.0.0.1, Name=MTA')dnl

/etc/mail/local-host-names에 도메인명 추가한다.

    yobi.navercorp.com

/etc/dovecot/users 에 imap 사용자를 등록한다. 유닉스 사용자 irteam를 비밀번호 yobi123으로 해서 imap 사용자로 등록하려면:

    irteam:{plain}yobi123:500:500::/home1/irteam

/etc/dovecot/conf.d/10-auth.conf 에서 auth-passwordfile.conf.ext를 불러오도록 설정한다.

    # Add '#' to comment out the system user login for now:
    #!include auth-system.conf.ext

    # Remove '#' to use passwd-file:
    !include auth-passwdfile.conf.ext

/etc/dovecot/conf.d/10-mail.conf 에 mail_location과 mail_access_groups를 설정한다.

    mail_location = mbox:~/mail:INBOX=/var/mail/%u
    mail_access_groups = mail

/etc/mail/virtusertable 를 고쳐서 yobi@yobi.navercorp.com나 yobi+*@yobi.navercorp.com으로 들어오는 이메일을 irteam이 수신하도록 설정한다.

    yobi@yobi.navercorp.com      irteam
    yobi+*@yobi.navercorp.com    irteam

/etc/mail/access를 고쳐서 신뢰할 수 있는 메일서버로부터만 메일을 받도록
설정한다. 예를 들어 10.112.226.55와 125.209.209.9만 허용하려면 다음과 같이
설정하면 된다.

    Connect:10.112.226.55 OK
    Connect:125.209.209.9 OK

    # Since Sendmail does not support CIDR expansion list all ranges explicitly
    Connect:1 REJECT
    Connect:2 REJECT
    Connect:3 REJECT
    ... 중략 ...
    Connect:252 REJECT
    Connect:253 REJECT
    Connect:254 REJECT

dovecot과 sendmail을 시작한다. (이미 시작되어있다면 restart로 재시작한다)

    /sbin/service dovecot start
    /sbin/service sendmail start

### 준비 2: Yobi 설정

Yobi의 conf/application.conf를 고쳐서 imap 설정을 한다.

    imap.host = yobi.navercorp.com
    imap.user = irteam
    imap.password = yobi123
    imap.folder = inbox
    imap.address = "yobi@yobi.navercorp.com"

설정이 적용될 수 있도록 Yobi를 재시작한다.

### TroubleShooting

#### 다음과 같은 에러와 함께 로그인이 안된다면

    tag NO [AUTHENTICATIONFAILED] Authentication failed.

IMAP 설치방법을 보고 /etc/dovecot/conf.d/10-auth.conf에서 auth-passwordfile.conf.ext를 불러오도록 설정한다.

#### /etc/mail/mail.log에 다음과 같은 로그가 남으면서 실패한다면

    Feb  1 14:57:49 cyobi01 dovecot: imap(irteam): Error: user irteam: Initialization failed: mail_location not set and autodetection failed: Mail storage autodetection failed with home=/home1/irteam

IMAP 설치방법을 보고 /etc/dovecot/conf.d/10-mail.conf 에 mail_location을 설정한다.

#### java.io.IOException: 기호 연결의 단계가 너무 많음

stage 중에 다음과 같은 에러가 발생한다면,

java.io.IOException: 기호 연결의 단계가 너무 많음
    at java.io.UnixFileSystem.canonicalize0(Native Method)
    at java.io.UnixFileSystem.canonicalize(UnixFileSystem.java:172)
    at java.io.File.getCanonicalPath(File.java:589)
    at play.PlaySettings$$anonfun$defaultSettings$75$$anonfun$apply$5.apply(PlaySettings.scala:288)
    at play.PlaySettings$$anonfun$defaultSettings$75$$anonfun$apply$5.apply(PlaySettings.scala:287)
    at scala.collection.TraversableLike$$anonfun$map$1.apply(TraversableLike.scala:244)
    at scala.collection.TraversableLike$$anonfun$map$1.apply(TraversableLike.scala:244)
    at scala.collection.mutable.ResizableArray$class.foreach(ResizableArray.scala:59)
    at scala.collection.mutable.ArrayBuffer.foreach(ArrayBuffer.scala:47)
    at scala.collection.TraversableLike$class.map(TraversableLike.scala:244)
    at scala.collection.AbstractTraversable.map(Traversable.scala:105)
    at play.PlaySettings$$anonfun$defaultSettings$75.apply(PlaySettings.scala:286)
    at play.PlaySettings$$anonfun$defaultSettings$75.apply(PlaySettings.scala:283)
    at scala.Function1$$anonfun$compose$1.apply(Function1.scala:47)
    at sbt.$tilde$greater$$anonfun$$u2219$1.apply(TypeFunctions.scala:42)
    at sbt.std.Transform$$anon$4.work(System.scala:64)
    at sbt.Execute$$anonfun$submit$1$$anonfun$apply$1.apply(Execute.scala:237)
    at sbt.Execute$$anonfun$submit$1$$anonfun$apply$1.apply(Execute.scala:237)
    at sbt.ErrorHandling$.wideConvert(ErrorHandling.scala:18)
    at sbt.Execute.work(Execute.scala:244)
    at sbt.Execute$$anonfun$submit$1.apply(Execute.scala:237)
    at sbt.Execute$$anonfun$submit$1.apply(Execute.scala:237)
    at sbt.ConcurrentRestrictions$$anon$4$$anonfun$1.apply(ConcurrentRestrictions.scala:160)
    at sbt.CompletionService$$anon$2.call(CompletionService.scala:30)
    at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:334)
    at java.util.concurrent.FutureTask.run(FutureTask.java:166)
    at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:471)
    at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:334)
    at java.util.concurrent.FutureTask.run(FutureTask.java:166)
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
    at java.lang.Thread.run(Thread.java:724)

어디엔가 자신을 참조하는 symlink가 있을 것이다. 예를 들어
/data/yobi/conf/conf/... 같은 symlink가 생겨나있을 수 있다.

그것을 삭제하고 다시 해본다. (예: `rm /data/yobi/conf/conf`)

## 디렉토리 정보

* Apache: /home1/irteam/apps/apache
* Activator: /home1/irteam/apps/activator
* Yobi instance: /data/yobi

## 백업

backup.md를 보라.
