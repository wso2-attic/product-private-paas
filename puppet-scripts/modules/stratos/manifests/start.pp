# Starts the service once the deployment is successful.

define stratos::start ( $target, $owner ) {
  exec { "strating_${name}":
    user    => $owner,
    path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    unless  => "test -f ${target}/wso2carbon.lck",
    command => "touch ${target}/wso2carbon.lck; ${target}/bin/stratos.sh > /dev/null 2>&1 &",
    creates => "${target}/repository/wso2carbon.log",
  }
}

