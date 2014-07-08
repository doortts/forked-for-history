#!/bin/zsh
#
# Delete branches in the remote repository which is merged into master and next
# branch in your local repository.

remote_repo=http://yobi.navercorp.com/dlab/hive

for commit branch in `git ls-remote $remote_repo | grep -E '[a-fA-F0-9]+\s+refs/heads/'`
do
    echo $branch | cut -c 12- | read branch
    if echo master maint next pu internal | grep -qw $branch; then
        continue
    fi
    if [ `git branch master next --contains=$commit | wc -l` -eq 2 ]; then
        git push $remote_repo :$branch
    fi
done
