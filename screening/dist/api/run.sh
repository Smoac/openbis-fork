USER=$1
PASSWD=$2
URL=$3
java -Djavax.net.ssl.trustStore=openBIS.keystore -jar openbis_screening_api.jar $USER $PASSWD $URL
