# Die on any error:
set -e

source "./conf/setup.conf"

# Setting GitBlit
mkdir $gitblit_path
tar xzf $gitblit_pack_path -C $gitblit_path
cp -f ./config/gitblit/gitblit.properties $gitblit_path/data

pushd $gitblit_path

echo "Starting Gitblit Server ..." >> $LOG
nohup $JAVA_HOME/bin/java -jar gitblit.jar --baseFolder data &
echo "Gitblit server started" >> $LOG

popd
