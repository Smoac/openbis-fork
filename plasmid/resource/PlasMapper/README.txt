This directory contains:

- jetty.zip
    version 7.1.4 of jetty with configuration that allows easy deployment of Plasmapper (port 8082)
- PlasMapper_download.tar.gz 
		version 2.0 of PlasMapper downloaded from http://wishart.biology.ualberta.ca/PlasMapper.
- PlasMapper_dist.zip 
		compressed distribution directory created in step 4 of the following instructions
- example.gb
		exmple GB file that can be used for testing after installation is complete

*Installation instructions for version 2.0*

1. Unpack PlasMapper_download.tar.gz into your workspace. 
2. In order to use the project in eclipse perform the following steps:
- Create a new project in eclipse using PlasMapper as source directory
- Fix build path:
-- remove /tmp/tomcat/common/lib/servlet-api.jar
-- add /libraries/jetty/servlet-api-2.5.jar
- [optional] change settings (can be done later, deployment)
-- properties file: ca.ualberta.xdong.plasMapper.annotate.plasMapConfiguration_en_CA.properties
-- change '/home/tomcat/webapps/PlasMapper' to the path where PlasMapper will be deploed
-- 'plasMapRoot' can lead to an external directory, where PlasMapper will create tmp directory
   for its output
3. in build.xml change values of properties: 
- <property name="installdir" value="dist"/>
- <property name="servletjar" value="../libraries/jetty/servlet-api-2.5.jar"/>
4. run ant install
5. copy dist/webapps/PlasMapper to webapps of the application server 
(make sure plasMapConfiguration_en_CA.properties are properly configured)

Additionally there needs to be blastall installed on the machine running PlasMapper. Latest
version can be downloaded from ftp://ftp.ncbi.nih.gov/blast/executables/release/LATEST.

Go to <server root>/PlasMapper/ after deployment to check that everything is working. You should
be able to generate a PNG for 'example.gb' file from this directory:
- select the GB file
- change the Image Size from 850x750 to 1200x1000
- click on Graphic Map button (below DNA sequence text area)