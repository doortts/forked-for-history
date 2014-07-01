# yobi.navercorp.com 백업

## 전체 백업

### yobi.navercorp.com 서버
- cron job으로 등록된 스크립트가 yobi 디렉터리를 압축하고 nc 명령을 위한 포트를 열어두어 백업서버가 압축파일을 받아가고 있음.
- cron job 스크립트 : `/home1/irteamsu/CLI/backup_server.sh` 
- cron job 확인(irteamsu 계정) : crontab -l 

### 백업 서버
- Ncloud VM (dev-yobi-slave.ncl)을 이용하여 백업 서버를 운영 중. 
- yobi.navercorp.com 서버에서 만든 yobi 디렉토리 압축파일을 백업서버가 nc 명령을 사용하여
  받아온 후 해당 파일을 Ncloud File Storage 서비스에 업로드 하는 것으로 일일 백업을 하고 있음.
- 해당 작업은 cron job에 등록된 스크립트가 주기적으로 실행하고 있음.  
- 백업 스크립트 : `/home1/irteamsu/CLI/backup_client.sh`  
- cron job 확인(irteamsu 계정) : crontab -l

### 백업서버에서 사용하는 Ncloud storage

Ncloud File Storage 란 필요한 파일을 저장할 수 있는 네트웍 스토리지 공간.

- 컨테이너(스토리지 네임스페이스) : yobi_backup
- 디렉토리(네임스페이스 내부에서 관리되는 디렉토리) : labs

### recover 명령 
*.db 파일에 손상이 의심될때 실행하는 명령어

```
java -cp /home1/irteamsu/dev/play-2.1.0/repository/local/com.h2database/h2/1.3.168/jars/h2.jar org.h2.tools.Recover
```

## DB 백업

### yobi.navercorp.com 서버
- nforge.h2.db 파일을 1시간마다 cron job으로 등록된 스크립트를 구동하여 백업 중
- cron job 스크립트 : `/home1/irteamsu/CLI/backup_db.sh`  
- 백업된 파일은 1시간이내의 파일만 로컬에 유지하고, 매 시간 백업된 파일은 Ncloud storage 서비스에 업로드 함

### DB 백업에서 사용하는 Ncloud storage

Ncloud File Storage 란 필요한 파일을 저장할 수 있는 네트웍 스토리지 공간.

- 컨테이너(스토리지 네임스페이스) : yobi_backup
- 디렉토리(네임스페이스 내부에서 관리되는 디렉토리) : labs_db

## Ncloud File Storage
- 서비스 주소 : http://ncloud.nhncorp.com/mc/nss/container
- 관련 문의 : http://devcafe.nhncorp.com/ncloud
- 가이드 : http://devcafe.nhncorp.com/ncloud/board_8/1953943

