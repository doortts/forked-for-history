#!/bin/sh
export JAVA_HOME=/home1/irteam/apps/jdk
export NCFS_HOME=/data/CLI/lib

DAYS=2
HOURS=1
SRC_PATH=/data/yobi
SRC_TARGET=nforge.h2.db
BACKUP_PATH=/data/backup
BACKUP_TYPE=db_backup
NCFS_CMD=/data/CLI/ncfscmd.sh
NCFS_CONTAINER=labs_yobi_backup
NCFS_PATH=labs_db

DATE_NEW=`/bin/date +%Y%m%d%H`
DATE_OLD=`/bin/date --d "now -$DAYS days" +%Y%m%d%H`
HOUR_OLD=`/bin/date --d "now -$HOURS hours" +%Y%m%d%H`
BACKUP_NEW="$SRC_TARGET.$DATE_NEW"
BACKUP_OLD="$SRC_TARGET.$DATE_OLD"
BACKUP_OLD_HOUR="$SRC_TARGET.$HOUR_OLD"
BACKUP_LOG=/home1/irteam/CLI/logs/db_backup/db.$DATE_NEW.log

cp $SRC_PATH/$SRC_TARGET $BACKUP_PATH/$BACKUP_TYPE/$BACKUP_NEW

$NCFS_CMD put $BACKUP_PATH/$BACKUP_TYPE/$BACKUP_NEW ncfs://$NCFS_CONTAINER/$NCFS_PATH >> $BACKUP_LOG
$NCFS_CMD del nfcs://$NCFS_CONTAINER/$NCFS_PATH/$BACKUP_OLD >> $BACKUP_LOG
rm $BACKUP_PATH/$BACKUP_TYPE/$BACKUP_OLD_HOUR
