#
# Apply the templates
define stratos::push_sc_templates ( $directory, $target, $service) {
  file { "${target}/repository/${name}":
    ensure  => present,
    owner   => $owner,
    group   => $group,
    mode    => '0755',
    content => template("${directory}/${service}/${name}.erb"),
  }
}

