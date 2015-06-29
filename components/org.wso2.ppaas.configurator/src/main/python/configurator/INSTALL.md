 #
 # Licensed to the Apache Software Foundation (ASF) under one
 # or more contributor license agreements. See the NOTICE file
 # distributed with this work for additional information
 # regarding copyright ownership. The ASF licenses this file
 # to you under the Apache License, Version 2.0 (the
 # "License"); you may not use this file except in compliance
 # with the License. You may obtain a copy of the License at
 # 
 # http://www.apache.org/licenses/LICENSE-2.0
 # 
 # Unless required by applicable law or agreed to in writing,
 # software distributed under the License is distributed on an
 # "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 # KIND, either express or implied. See the License for the
 # specific language governing permissions and limitations
 # under the License.
 #

# Installing Python configurator

Please follow below steps to proceed with the installation:

1. Install python pip.
   ```
   sudo apt-get install python-pip
   ```

2. Install jinja2 module.
   ```
   pip install Jinja2
   ```
3. Copy template files to <configurator_home>/template-modules.

4. Change <configurator_home>/template-modules/<wso2_product_name>/module.ini as required for cluster.
  
5. Run <confiugrator_home>/configurator.py.
   ```
   ./configurator.py
   ```

