#
# Apply the templates
define bam::push_templates ( $directory, $target) {
  file { "${target}/repository/${name}":
    ensure  => present,
    owner   => $owner,
    group   => $group,
    mode    => '0755',
    content => template("${directory}/${name}.erb"),
  }
}

