
#PPaaS Artifact Conversion Tool 4.0.0 to 4.1.0

This tool will enable users to import artifact JSON files from PPaaS 4.0.0 and convert them to artifact JSON files of PPaaS 4.1.0

## Instructions

1. Build the ppaas-artifact-converter tool by running the following command mvn clean install

2. Extract the generated ppaasArtifact-1.0-SNAPSHOT.zip

3. Run the stratos.sh in bin folder

4. Deploy the generated artifacts from output-artifacts folder in PPaaS 4.1.0

## Configurations

Default values, that have been used for the port mappings of the cartridges can be updated in conf/config.properties file.
If specific port mappings are needed for each cartridge, it is needed to update the generated artifact jsons of cartridges in output-artifacts folder.
