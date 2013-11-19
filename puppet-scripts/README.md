PaaS Puppet Script
==============

Complete puppet script with stratos puppet module. 

Support only for Puppet 3.X.


Download mysql java connector (mysql-connector-java-5.1.26-bin.jar) and copy to this path :

    modules/stratos/files/commons/configs/repository/components/lib/

Create databases and users in a MySQL database.


    create database userstore;
    CREATE USER 'userstore'@'%' IDENTIFIED BY 'userstore';
    GRANT ALL PRIVILEGES ON userstore.* TO 'userstore'@'%' WITH GRANT OPTION;

    create database stratos_foundation;
    CREATE USER 'scdbuser'@'%' IDENTIFIED BY 'scdbpassword';
    GRANT ALL PRIVILEGES ON stratos_foundation.* TO 'scdbuser'@'%' WITH GRANT OPTION;
