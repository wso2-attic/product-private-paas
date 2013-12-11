PaaS Puppet Manifest
==============

Complete puppet manifest with stratos puppet module. 

* Note :-

    + This only support Debian based distributions with Puppet 3.X only.
        
        Use puppetlabs repositories to install puppet packages: 
        http://docs.puppetlabs.com/guides/puppetlabs_package_repositories.html
    
    + Run all as __root__.
    
    + Require minimal two 8GB RAM instances or four 4GB RAM Instances to install Apache Stratos packages.
    
    + Create a Apache Stratos cartridge and upload it to your IaaS: 
        https://cwiki.apache.org/confluence/display/STRATOS/Creating+a+Cartridge


    ```
                                                                                -------------------       
                                                                               |                   |
                    -------------------     -------------------             -------------------    |
                   |                   |   |                   |           |                   |   |
                   |   HTTPD           |   |   ELB             |           |                   |   |
                   |                   |   |                   |           |   Your            |   |
                   |   PuppetMaster    |   |   CC              |           |   System          |   |
                   |                   |   |                   |           |                   |   |
                   |   SC              |   |   Agent           |           |                   |   |
                   |                   |   |                   |           |                   |   |
                   |   MB              |   |   MySQL           |           |                   |   |
                   |                   |   |                   |           |                   |   |
                   |   GitBlit         |   |                   |           |                   |   |
                -------------------------------------------------------------------------------------------
               |                                                   | Your cartridge    |                   |
               |   OpenStack / EC2                                 |                   |                   |
               |                                                    -------------------                    |
                -------------------------------------------------------------------------------------------


    ```

### On puppet master node 

1.) Set a static IP for the puppetmaster host

    # vim /etc/network/interfaces
     
    & modify accordingly 
    auto eth0
    iface eth0 inet static
        address <your ip eg: 10.0.0.20>
        netmask <your netmask eg: 255.255.255.0>
        gateway <your gateway eg: 10.0.0.1>

2.) Change the hostname to 'puppetmaster.<your domain>' 

    # echo 'puppetmaster.<your domain>' >/etc/hostname
    # hostname puppetmaster.<your domain>

3.) Install puppet master

    # apt-get install -y puppetmaster mysql-client 

4.) Copy the puppet manifests to '/etc/puppet/' on puppetmaster

    # git clone https://github.com/wso2/private-paas.git
    # cp -rf private-paas/puppet-scripts/*  /etc/puppet/

5.) Update puppet.conf puppet server

    # sed -i 's/server=puppetmaster.example.com/server=puppetmaster.<your domain>/' /etc/puppet/puppet.conf 
    
6.) Update autosign.conf

    # echo '*.<your domain>' >/etc/puppet/autosign.conf

7.) Download mysql java connector (mysql-connector-java-x.x.xx-bin.jar) and copy to this path :

    # cp mysql-connector-* /etc/puppet/modules/stratos/files/commons/configs/repository/components/lib/

8.) Restart puppetmaster

    # service puppetmaster restart

9.) Install apache webserver

    # apt-get install -y apache2
    
10.) Download packs into apache webservers '/var/www/' or '/var/www/html/' directory.

    Required packages.
    + apache-stratos-agent-3.0.0-incubating.zip
    + apache-stratos-cc-3.0.0-incubating.zip
    + apache-stratos-cli-3.0.0-incubating.zip
    + apache-stratos-elb-3.0.0-incubating.zip
    + apache-stratos-sc-3.0.0-incubating.zip
        via http://mirror.symnds.com/software/Apache/incubator/stratos/3.0.0-incubating/
        
    + jdk-XXXX-linux-x64.tar.gz
        via http://www.oracle.com/technetwork/java/javase/downloads/index.html
        
    + apache-maven-3.0.5-bin.tar.gz
        via http://maven.apache.org/download.cgi
     
    + wso2mb.zip
        via http://wso2.com/products/message-broker/
        
    + mysql-connector-java-x.x.xx-bin.jar
        via http://dev.mysql.com/downloads/connector/j/
        

### On MySQL server

1.) Install MySQL server

    # apt-get install mysql-server -y
     
    # sed -i 's/^bind-address/#bind-address/' /etc/mysql/my.cnf
    # /etc/init.d/mysql restart

2.) Remove ''@'localhost' and ''@'<mysql server hostname>' users form the MySQL database.

    mysql> use information_schema;
    mysql> select GRANTEE from USER_PRIVILEGES where GRANTEE like '%\'\'%';
     
    mysql> drop user <''@'XXXXXXXX'>;

3.) Copy following SQL scripts from '/etc/puppet/modules/stratos/files/commons/resources/' location 
    to mysql server

    registry.sql
    userstore.sql
    billing-mysql.sql
    stratos_foundation.sql
    
4.) Create databases and users in a MySQL database.

    Log in to mysql database as root user 
     
    # mysql -u root -p -h <your mysql host>
     
    mysql> source /<your path to SQL scripts>/registry.sql; 
    mysql> source /<your path to SQL scripts>/userstore.sql;
    mysql> source /<your path to SQL scripts>/userstore.sql;
    mysql> source /<your path to SQL scripts>/stratos_foundation.sql;
     
    mysql> CREATE USER 'registry'@'%' IDENTIFIED BY 'registry';
    mysql> GRANT ALL PRIVILEGES ON registry.* TO 'registry'@'%' WITH GRANT OPTION;
     
    mysql> CREATE USER 'userstore'@'%' IDENTIFIED BY 'userstore';
    mysql> GRANT ALL PRIVILEGES ON userstore.* TO 'userstore'@'%' WITH GRANT OPTION;
     
    mysql> CREATE USER 'billing'@'%' IDENTIFIED BY 'billing';
    mysql> GRANT ALL PRIVILEGES ON billing.* TO 'billing'@'%' WITH GRANT OPTION;
     
    mysql> CREATE USER 'scdbuser'@'%' IDENTIFIED BY 'scdbpassword';
    mysql> GRANT ALL PRIVILEGES ON stratos_foundation.* TO 'scdbuser'@'%' WITH GRANT OPTION;

### On other nodes

1.) install java (Oracle JDK) for all users including root, add java to path variable and verify by;

    # java -version
    # echo $JAVA_HOME

2.) Install dependency pachages

    # apt-get install -y wget unzip lsof sysstat telnet git less tree

3.) Assigne a proper hostname to the node.

    # echo 'node<xxx>.<your domain>' >/etc/hostname
    # hostname node<xxx>.<your domain>

4.) Update '/etc/hosts' file to resolve puppetmaster's IP address.

    # echo '127.0.0.1 localhost' >/etc/hosts
    # echo '127.0.0.1 <fqdn hostname eg : node001.example.com>'

5.) Install puppet agent on host nodes.

    # apt-get install -y puppet

6.) Add puppet servce URL in '/etc/puppet/puppet.conf' file

    # sed -i '2i server=puppetmaster.<your domain>' /etc/puppet/puppet.conf

7.) Update parameter and node settings on puppetmasters 'site.pp' according to your deployment.

    # vim /etc/puppet/manifests/site.pp 

8.) Run puppet agent on each host.

    Check for the MySQL connectivity.
     
    # puppet agent -vt

