#
#
#

define stratos::push_stratos_sh ( $directory, $target, $service ) {
  file { "${target}/${name}":
    ensure  => present,
    owner   => $owner,
    group   => $group,
    mode    => '0755',
    content => template("${directory}/${service}/${name}.erb"),
  }
}
