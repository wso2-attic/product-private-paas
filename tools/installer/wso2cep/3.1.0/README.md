## WSO2 Complex Event Processor 3.1.0 Cartridge

### Profiles

   - Default
   - Worker
   - Manager

### Compatibility

WSO2 Private PaaS 4.1.0 or higher

### Special Notes

- Server Shutdown timeout can be changed adding a new property as follows under property section in a cartridge definition.

name : CONFIG_PARAM_SERVER_SHUTDOWN_TIMEOUT 
value : 120 

Default value is 120 seconds. Value should be specified in seconds.

- Create database tables before deploying the application
