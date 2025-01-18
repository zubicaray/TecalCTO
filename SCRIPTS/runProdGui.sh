
version=$(grep -oPm1 "(?<=<version>)[^<]+" pom.xml)
java -cp target/TecalCPO-$version.jar  org.tecal.ui.TecalGUI
