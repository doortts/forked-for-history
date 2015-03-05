#!/bin/sh
export JAVA_HOME=/home1/irteam/apps/jdk
export NCFS_HOME=/data/CLI/lib


RC_PATH=/data/backup
SRC_TARGET=full_backup

NCFS_CMD=/data/CLI/ncfscmd.sh
NCFS_CONTAINER=labs_yobi_backup
NCFS_PATH=labs_db
DATE_TODAY=20140830
BACKUP_LOG=/data/CLI/logs/full_backup/$DATE_TODAY.log

$NCFS_CMD get ncfs://$NCFS_CONTAINER/$NCFS_PATH/yobi.$DATE_TODAY.tar.gz $RC_PATH/$SRC_TARGET >> $BACKUP_LOG
