# Building openBIS

## Requirements
# JDK8 or JDK11

## Step By Step:
```
git clone https://sissource.ethz.ch/sispub/openbis.git
cd installation/
./gradlew clean
./gradlew build -x test
```
## Where the build is found?
```
./installation/targets/gradle/distributions/openBIS-installation-standard-technologies-SNAPSHOT-rXXXXXXXXXX.tar.gz
```

## Why we disable tests to make the build?
They increase the time to obtain a build plus some tests could have additional environment requirements.

## Why the core UI made using GWT is not build anymore?
It increases the time to obtain a build plus it requires JDK8, it will be removed on next release. For now it can be build following the next commands:
```
git clone https://sissource.ethz.ch/sispub/openbis.git
cd openbis_standard_technologies/
./gradlew clean
./gradlew buildCoreUIPackageUsingJDK8 -x test
```

# Developing openBIS

## Requirements
# Postgres 11
# IntelliJ IDEA CE

## Step By Step:
```
File -> New -> Project From Existing Sources
Select the gradle folder to load the gradle model
After the model is loaded execute the tasks:

openBISDevelopementEnvironmentASPrepare
openBISDevelopementEnvironmentASStart
openBISDevelopementEnvironmentDSSStart
```
