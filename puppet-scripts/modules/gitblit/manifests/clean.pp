#
#
#
#

define gitblit::clean ( $mode, $target ) {
  if $mode == 'refresh' {
    exec{
      "Remove_lock_file_${name}":
        path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
        onlyif  => "test -f ${target}/gitblit.lck",
        command => "rm ${target}/gitblit.lck";

      "Stop_process_${name}":
        path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
        command => "kill -9 `cat ${target}/gitblit.pid` ; /bin/echo Killed",
        require => Exec["Remove_lock_file_${name}"];
    }
    notify { "Gitblit_msg":
	message => "kill -9 `cat ${target}/gitblit.pid` ; /bin/echo Killed"
	}
  }
  elsif $mode == 'new' {
    exec { "Stop_process_and_remove_CARBON_HOME_${name}":
        path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
        command => "kill -9 `cat ${target}/gitblit.pid` ; rm -rf ${target}";
    }
  }
  elsif $mode == 'zero' {
    exec { "Stop_process_remove_CARBON_HOME_and_pack_${name}":
        path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
        command => "kill -9 `cat ${target}/gitblit.pid` ; rm -rf ${target} ; rm -f ${gitblit::local_package_dir}/${gitblit::service_code}-${gitblit::version}.zip";
    }
  }
}
