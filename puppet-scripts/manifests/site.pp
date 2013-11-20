node base {

    $domain          = 'example.com'
    $package_repo         = "http://downloads.${domain}"

# IP addresses 

    $puppetmaster_ip = "10.33.14.2"
    $downloads_ip    = "10.33.14.2"
    $svn_ip          = "10.33.14.2"
    $sc_ip           = "10.33.14.16"
    $cc_ip           = "10.33.14.21"
    $agent_ip        = "10.33.14.22"
    $elb_ip          = "10.33.14.20"
    $mb_ip           = "10.33.14.11"
    $git_ip          = "10.33.14.27"
    $cassandra_ip    = "10.33.14.23"
    $bam_ip          = "10.33.14.25"
    $mysql_ip        = "10.33.14.28"


    file {
        "/etc/hosts":
        owner   => root,
        group   => root,
        mode    => 775,
        content => template("hosts.erb"),
    }

}


node /node002.*/ inherits base {

    class { 'broker':
        version            => '2.1.0',
        offset             => 5,
        maintenance_mode   => 'refresh',
        owner              => 'root',
        group              => 'root',
        target             => '/mnt',
    }
    
}

node /node003.*/ inherits base {

    class {'stratos::sc':
        version          => '3.0.0-incubating',
        maintenance_mode => 'refresh',
        auto_scaler      => 'false',
        auto_failover    => false,
        owner            => 'root',
        group            => 'root',
        target           => '/mnt',
    }
}

node /node004.*/ inherits base {

    class {'stratos::cc':
        version          => '3.0.0-incubating',
        maintenance_mode => 'refresh',
        auto_scaler      => 'false',
        auto_failover    => false,
        owner            => 'root',
        group            => 'root',
        target           => '/mnt',
    }
}


node /node005.*/ inherits base {

    class {'stratos::elb':
        version          => '3.0.0-incubating',
        maintenance_mode => 'refresh',
        auto_scaler      => 'false',
        auto_failover    => false,
        target           => '/mnt',
    }
}
