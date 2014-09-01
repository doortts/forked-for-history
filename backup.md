# yobi.navercorp.com(cyobi01.lab) 백업

yobi.navercorp.com(cyobi01.lab)는 Ncloud File Storage 를 이용하여 백업을 하고 있다.

## Ncloud File Storage

필요한 파일을 저장할 수 있는 네트웍 스토리지 공간 

### Ncloud File Storage 관련 용어

- 컨테이너: Ncloud File Storage 의 독립적인 스토리지 네임스페이스
- folder: 컨테이너 내의 directory 공간

### Ncloud File Storage 서비스 관련 참고링크
 
- 서비스 주소 : http://ncloud.nhncorp.com/mc/nss/container
- 관련 문의 : http://devcafe.nhncorp.com/ncloud
- 가이드 : http://devcafe.nhncorp.com/ncloud/board_8/1953943

## full backup 

full backup은 `/data/yobi` directory를 tar+gzip 을 사용하여 압축파일(예: yobi.20140831.tar.gz)로 만들고 `/data/backup/fullbackup directory`에 저장한다. 저장 된 압축파일은 Ncloud File Storage에 업로드 되며 업로드가 완료 되면 삭제 된다.

Ncloud File Storage 에는 최근 15일 간의 backup 만 유지 하고 있으며 15일 이전 일자 백업파일은 당일 백업 파일의 업로드가 완료되면 삭제 된다. 

full backup의 모든 작업은 cronjob 으로 스케쥴링 되며 매일 새벽 2시에 cronjob 에 등록된 스크립트를 실행하여 수행 된다. 

### cronjob

- cronjob 실행 계정:  irteam
- 실행 스크립트: `/data/CLI/backup.sh`
- 스크립트 실행 log: `/data/CLI/full_backup/` directory 에 일자 별로 저장. (예: 20140831.log)
- cronjob log 확인: `/data/CLI/logs/cronjob/fullbackup.log`
- cronjob 실행 시간: 매일 새벽 2시
- cronjob 확인: crontab -l

```
0 2 * * * /data/CLI/backup.sh >> /data/CLI/logs/cronjob/fullbackup.log 2>&1
```
  
### Ncloud File Storage 

- 컨테이너 명: labs_yobi_backup
- folder : labs_yobi

## db backup

db backup 은 `/data/yobi/nforge.h2.db` 파일을 `/data/backup/db_backup/` directory 로 복사후 복사된 파일을 Ncloud File Storage 에 업로드 한다.

Ncloud File Storage 에는 최근 2일 간의 backup 만 유지 하고 있으며 2일 이전 일자 백업파일은 최신 백업파일의 업로드가 완료된 후 삭제된다.

db backup 모든 작업은 cronjob 으로 스케쥴링 되며 매시 00분에 cronjob 에 등록된 스크립트를 실행하여 수행 된다. 

### cronjob

- cronjob 실행 계정:  irteam
- 실행 스크립트: `/data/CLI/backup_db.sh`
- 스크립트 실행 log: `/data/CLI/db_backup/` directory 에 일자별로 저장. (예: 20140831.log)
 - cronjob log 확인: `/data/CLI/logs/cronjob/dbbackup.log`
- cronjob 실행 시간: 매시 00분
- cronjob 확인: crontab -l

```
0 * * * * /data/CLI/backup_db.sh >> /data/CLI/logs/cronjob/dbbackup.log 2>&1
```

### Ncloud File Storage

- 컨테이너 명: labs_yobi_backup
- folder : labs_db

## recover 명령 

*.db 파일에 손상이 의심될때 실행하는 명령어

```
java -cp /home1/irteam/apps/play-2.1.0/repository/local/com.h2database/h2/1.3.168/jars/h2.jar org.h2.tools.Recover
```

