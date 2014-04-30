#!/bin/bash

function help {
    echo ""
    echo "Build stratos from source and copy the distribution to the relevant location"
    echo ""
    echo "usage:"
    echo "Build"
    echo "	build.sh -b"
    echo ""
    echo "Build and copy"
    echo "	build.sh -c"
    echo ""

}

if [ "$#" -eq 0 ]; then
   help
fi


while getopts bct opts
do
  case $opts in
    b)  
        echo ""
        echo " Build using maven "
	pushd source
        mvn clean install 
	popd
        echo " Built Successfully. You may copy the distribution to <private-paas-home>/packs location "
        ;;
    c)
        echo " Build using maven "
        pushd source
        mvn clean install 
        popd
	echo " Built Successfully. Now copying the distribution to <private-paas-home>/packs"
        ;;
    t)
        echo "in t..."
        command="mvns cleans installs"
	echo "copy as well.."
        ;;
    *)
        help
        #exit 1
        ;;
  esac
done

