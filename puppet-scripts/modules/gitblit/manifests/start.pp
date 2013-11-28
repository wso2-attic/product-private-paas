# Starts the service once the deployment is successful.

define gitblit::start ( $target, $owner ) {
  exec { "strating_${name}":
    user    => $owner,
    path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    unless  => "test -f ${target}/gitblit.lck",
    command => "touch ${target}/gitblit.lck; /bin/bash ${target}/gitblit.sh > /dev/null 2>&1",
    creates => "${target}/repository/wso2carbon.log",
  }
}

