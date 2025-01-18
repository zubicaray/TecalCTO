
version=$(grep -oPm1 "(?<=<version>)[^<]+" pom.xml)
java -Dcom.sun.management.jmxremote \
     -Dcom.sun.management.jmxremote.port=9010 \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Dcom.sun.management.jmxremote.ssl=false \
     -cp target/TecalCPO-$version.jar  org.tecal.ui.CPO_IHM
