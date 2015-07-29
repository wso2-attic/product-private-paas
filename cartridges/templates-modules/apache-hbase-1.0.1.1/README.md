HBase 1.0.1.1 Template for the Configurator
-------------------------------------------------------------------------------------

This template supports following configurations

1. Starting HBase master in distributed mode
2. Starting Hbase region servers

Separate Hadoop and Zookeeper instances are required to run this HBase docker
image.

Following are the configuration parameters that is used by the template.
You can configure following in the ***module.ini*** file.

#### Read from environment variables :


    READ_FROM_ENVIRONMENT = false
 

-------------------------------------------------------------------------------------

#### Set the path of product directory :

    CARBON_HOME = < HBASE_HOME >

---

#### Hadoop instance for HDFS file system : 

    CONFIG_PARAM_HDFS_HOST = localhost

* Used in - < HBASE_HOME >/conf/hbase-site.xml

---

#### Master node of HBase instance (to be used with region servers) : 

    CONFIG_PARAM_HBASE_MASTER = localhost

* Used in - < HBASE_HOME >/conf/hbase-site.xml
            /etc/hosts

---

#### Zookeeper instance : 

    CONFIG_PARAM_ZOOKEEPER_HOST = localhost

* Used in - < HBASE_HOME >/conf/hbase-site.xml

---

#### Hostname of HBase master node (to be used with region servers) : 

    CONFIG_PARAM_HBASE_MASTER_HOSTNAME = hmaster

* Used in - /etc/hosts

---


