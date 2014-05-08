/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/ 


--
-- Database: `StratosStats`
--
CREATE DATABASE IF NOT EXISTS `StratosStats` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `StratosStats`;

-- --------------------------------------------------------

--
-- Table structure for table `StratosCData`
--

CREATE TABLE IF NOT EXISTS `StratosCData` (
  `clusterId` varchar(200) NOT NULL DEFAULT '',
  `memberId` varchar(200) NOT NULL DEFAULT '',
  `status` varchar(50) NOT NULL DEFAULT '',
  `cartridgeType` varchar(50) DEFAULT NULL,
  `timesta` varchar(20) DEFAULT NULL,
  `hostName` varchar(100) DEFAULT NULL,
  `hypervisor` varchar(100) DEFAULT NULL,
  `iaas` varchar(100) DEFAULT NULL,
  `imageId` varchar(200) DEFAULT NULL,
  `is64BitOs` tinyint(1) DEFAULT NULL,
  `isMultiTenent` tinyint(1) DEFAULT NULL,
  `loginPort` int(11) DEFAULT NULL,
  `networkId` varchar(100) DEFAULT NULL,
  `osArch` varchar(100) DEFAULT NULL,
  `osVersion` varchar(100) DEFAULT NULL,
  `partitionId` varchar(100) DEFAULT NULL,
  `privateIPAddress` varchar(50) DEFAULT NULL,
  `publicIPAddress` varchar(50) DEFAULT NULL,
  `ram` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`clusterId`,`memberId`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `StratosTData`
--

CREATE TABLE IF NOT EXISTS `StratosTData` (
  `tenentId` varchar(200) NOT NULL DEFAULT '',
  `clusterId` varchar(200) NOT NULL DEFAULT '',
  `action` varchar(50) NOT NULL DEFAULT '',
  `timesta` varchar(20) NOT NULL DEFAULT '',
  `deploymentPolicy` varchar(200) DEFAULT NULL,
  `adminUser` varchar(100) DEFAULT NULL,
  `autoScalePolicy` varchar(200) DEFAULT NULL,
  `cartridgeAlias` varchar(100) DEFAULT NULL,
  `hostName` varchar(200) DEFAULT NULL,
  `isMultiTenent` tinyint(1) DEFAULT NULL,
  `repositoryUrl` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`tenentId`,`clusterId`,`action`,`timesta`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

