# /bin/bash
cd keycloak
cd register-keycloak-to-myaccount
mvn clean package && mvn install 
cd ../login-keycloak-to-myaccount
mvn clean package && mvn install 