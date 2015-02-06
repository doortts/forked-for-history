#!/bin/sh
export JAVA_HOME=/home1/irteam/apps/jdk
export NCFS_HOME=/data/CLI/lib

DAYS=15
SRC_PATH=/data
SRC_TARGET=yobi
BACKUP_PATH=/data/backup
BACKUP_TYPE=full_backup
NCFS_CMD=/data/CLI/ncfscmd.sh
NCFS_CONTAINER=labs_yobi_backup
NCFS_PATH=labs_yobi

DATE_NEW=`/bin/date +%Y%m%d`
DATE_OLD=`/bin/date --d "now -$DAYS days" +%Y%m%d`
BACKUP_NEW="yobi.$DATE_NEW.tar.gz"
BACKUP_OLD="yobi.$DATE_OLD.tar.gz"
BACKUP_LOG=/data/CLI/logs/full_backup/$DATE_NEW.log

tar zcvf  $BACKUP_PATH/$BACKUP_TYPE/$BACKUP_NEW $SRC_PATH/$SRC_TARGET >> $BACKUP_LOG
$NCFS_CMD put $BACKUP_PATH/$BACKUP_TYPE/$BACKUP_NEW ncfs://$NCFS_CONTAINER/$NCFS_PATH >> $BACKUP_LOG
$NCFS_CMD del nfcs://$NCFS_CONTAINER/$NCFS_PATH/$BACKUP_OLD >> $BACKUP_LOG
rm $BACKUP_PATH/$BACKUP_TYPE/$BACKUP_NEW
