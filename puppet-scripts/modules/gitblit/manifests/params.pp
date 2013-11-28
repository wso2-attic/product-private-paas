#
# Class sc::params
#
# This class manages sc parameters
#
# Parameters:
#
# Usage: Uncomment the variable and assign a value to override the example.pp value
#
#

class gitblit::params {

#    $domain               = 'example.com'
#    $package_repo         = "http://downloads.${domain}"
#    $local_package_dir    = '/mnt/packs'
#
#    $gitblit
    $gitblit_http_port	= "8280"
    $gitblit_https_port = "8443"
}

