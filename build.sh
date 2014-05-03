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

      STATUS=$?
	if [ $STATUS -eq 0 ]; then
		echo " Built Successfully. You may copy the distribution to <private-paas-home>/packs location "
	else
		echo ""
		echo " Build Failure. Please try again"
	fi
	popd
        ;;
    c)
        echo " Build using maven "
        pushd source
        mvn clean install
    
     STATUS=$?
        if [ $STATUS -eq 0 ]; then
	    echo " Built Successfully. Now copying the distribution to <private-paas-home>/packs"
	    cp products/stratos/modules/distribution/target/*.zip ../packs/
	    cp products/cartridge-agent/modules/distribution/target/*.zip ../packs/
            cp products/load-balancer/modules/distribution/target/*.zip ../packs/
        else
		echo ""
                echo " Build Failure. Please try again"
        fi
	popd
        ;;
    *)
        help
        #exit 1
        ;;
  esac
done

