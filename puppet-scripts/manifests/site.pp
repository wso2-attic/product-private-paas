
node 'node001' {

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
