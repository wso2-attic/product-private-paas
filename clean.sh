#!/bin/bash

function help {
    echo ""
    echo "Clean the host machine where one or more of the Stratos2 servers are run."
    echo "usage:"
    echo "clean.sh -u <mysql username> -p <mysql password>"
    echo ""
}
while getopts u:p: opts
do
  case $opts in
    u)
        mysql_user=${OPTARG}
        ;;
    p)
        mysql_pass=${OPTARG}
        ;;
    *)
        help
        exit 1
        ;;
  esac
done

if [[  -f /etc/puppet/manifests/nodes.pp.orig ]]; then
    echo "Backing up the original nodes.pp file"
    cp -f /etc/puppet/manifests/nodes.pp.orig /etc/puppet/manifests/nodes.pp
fi

if [[  -f /etc/puppet/modules/appserver/manifests/params.pp.orig ]]; then
    echo "Backing up the original AS params.pp file"
    cp -f /etc/puppet/modules/appserver/manifests/params.pp.orig /etc/puppet/modules/appserver/manifests/params.pp
fi

if [[  -f /etc/puppet/modules/esb/manifests/params.pp.orig ]]; then
    echo "Backing up the original ESB params.pp file"
    cp -f /etc/puppet/modules/esb/manifests/params.pp.orig /etc/puppet/modules/esb/manifests/params.pp
fi

if [[  -f stratos-installer/conf/setup.conf.orig ]]; then
    echo "Backing up the original setup.conf file"
    cp -f stratos-installer/conf/setup.conf.orig stratos-installer/conf/setup.conf
fi

#mysql -u $mysql_user -p$mysql_pass -e "DROP DATABASE IF EXISTS registry;"

cd stratos-installer
/bin/bash clean.sh $1 $2
