if [[ "$1" != "" ]] then
    version=$1
    echo "choix de version: $version"
    java -cp ~/DEV/TecalCTO/SCRIPTS/TecalCPO-$version.jar  org.tecal.ui.TecalGUI
else
    version=$(grep -oPm1 "(?<=<version>)[^<]+" pom.xml)
    java -cp ~/DEV/TecalCTO/target/TecalCPO-$version.jar  org.tecal.ui.TecalGUI
fi


