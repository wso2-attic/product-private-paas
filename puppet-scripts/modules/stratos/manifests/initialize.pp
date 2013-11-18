#
#
#
#
# Initializing the deployment

define stratos::initialize (
  $repo,
  $version,
  $service,
  $local_dir,
  $target,
  $mode,
  $owner,
) {

  exec {
    "creating_target_for_${name}":
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      command   => "mkdir -p ${target}";

    "creating_local_package_repo_for_${name}":
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      unless    => "test -d ${local_dir}",
      command   => "mkdir -p ${local_dir}";

    "downloading_apache-stratos-${service}-${version}.zip_for_${name}":
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      cwd       => $local_dir,
      unless    => "test -f ${local_dir}/apache-stratos-${service}-${version}.zip",
      command   => "wget -q ${repo}/apache-stratos-${service}-${version}.zip",
      logoutput => 'on_failure',
      creates   => "${local_dir}/apache-stratos-${service}-${version}.zip",
      timeout   => 0,
      require   => Exec["creating_local_package_repo_for_${name}",
                        "creating_target_for_${name}"];

    "extracting_apache-stratos-${service}-${version}.zip_for_${name}":
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      cwd       => $target,
      unless    => "test -d ${target}/apache-stratos-${service}-${version}/repository",
      command   => "unzip ${local_dir}/apache-stratos-${service}-${version}.zip",
      logoutput => 'on_failure',
      creates   => "${target}/apache-stratos-${service}-${version}/repository",
      timeout   => 0,
      require   => Exec["downloading_apache-stratos-${service}-${version}.zip_for_${name}"];

    "setting_permission_for_${name}":
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      cwd       => $target,
      command   => "chown -R ${owner}:${owner} ${target}/apache-stratos-${service}-${version} ;
                    chmod -R 755 ${target}/apache-stratos-${service}-${version}",
      logoutput => 'on_failure',
      timeout   => 0,
      require   => Exec["extracting_apache-stratos-${service}-${version}.zip_for_${name}"];
  }
}
