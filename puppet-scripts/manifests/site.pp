node base {

    $domain          = 'example.com'

# IP addresses 

    $puppetmaster_ip = "192.168.122.81"
    $downloads_ip    = "192.168.122.81"
    $svn_ip          = "192.168.122.81"
    $sc_ip           = "192.168.122.81"
    $cc_ip           = "192.168.122.81"
    $agent_ip        = "192.168.122.81"
    $elb_ip          = "192.168.122.81"
    $mb_ip           = "192.168.122.81"
    $git_ip          = "192.168.122.81"
    $cassandra_ip    = "192.168.122.81"
    $bam_ip          = "192.168.122.81"
    $mysql_ip        = "192.168.122.81"


    file {
        "/etc/hosts":
        owner   => root,
        group   => root,
        mode    => 775,
        content => template("hosts.erb"),
    }

}


node /node001.*/ inherits base {

    class {'stratos::sc':
    version          => '3.0.0-incubating',
    maintenance_mode => 'zero',
    auto_scaler      => 'false',
    auto_failover    => false,
    owner            => 'root',
    group            => 'root',
    target           => '/mnt',
    }
}
