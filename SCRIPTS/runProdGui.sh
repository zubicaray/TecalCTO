if [[ "$1" != "" ]] then
    version=$1
    echo "choix de version: $version"
    java -cp ~/DEV/TecalCPO/SCRIPTS/TecalCPO-$version.jar  org.tecal.ui.TecalGUI
else
    version=$(grep -oPm1 "(?<=<version>)[^<]+" pom.xml)
    java -cp ~/DEV/TecalCPO/target/TecalCPO-$version.jar  org.tecal.ui.TecalGUI
fi


