#
#
#
#

define broker::clean ( $mode, $target ) {
  if $mode == 'refresh' {
    exec{
      "Remove_lock_file_${name}":
        path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
        onlyif  => "test -f ${target}/wso2carbon.lck",
        command => "rm ${target}/wso2carbon.lck";

      "Stop_process_${name}":
        path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
        command => "kill -9 `cat ${target}/wso2carbon.pid` ; /bin/echo Killed",
        require => Exec["Remove_lock_file_${name}"];
    }
  }
  elsif $mode == 'new' {
    exec { "Stop_process_and_remove_CARBON_HOME_${name}":
        path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
        command => "kill -9 `cat ${target}/wso2carbon.pid` ; rm -rf ${target}";
    }
  }
  elsif $mode == 'zero' {
    exec { "Stop_process_remove_CARBON_HOME_and_pack_${name}":
        path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
        command => "kill -9 `cat ${target}/wso2carbon.pid` ; rm -rf ${target} ; rm -f ${broker::local_package_dir}/wso2${broker::service_code}-${broker::version}.zip";
    }
  }
}
