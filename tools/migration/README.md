
# PPaaS Artifact Migration Tool 4.0.0 to 4.1.x

This tool will enable users to import artifact JSON files from PPaaS 4.0.0 and convert them to artifact JSON files of PPaaS 4.1.0

## Instructions

1. Build the ppaas-artifact-migration tool by running the following command mvn clean install

2. Extract the generated distribution-4.1.1-SNAPSHOT.zip in distribution/target

3. Deploy the migration-api/4.0.0/migration.war in PPaaS 4.0.0

4. Run the stratos.sh in bin folder

5. Deploy the generated artifacts from output-artifacts folder in PPaaS 4.1.x

## Configurations

Default values, that have been used for the iaas provider details can be updated in distribution/contents/conf/config.properties file.
Base URLs of PPaaS 4.0.0 and PPaaS 4.1.X and login credentials should be updated in distribution/contents/conf/config.properties file.