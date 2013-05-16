This short text explain how to do development, build and deploymanet of openBIS KNIME nodes.

Prerequisites for developing
============================

1. There are three Eclipse projects which belong together:
   openbis_knime, openbis_knime.feature and openbis_knime.update-site
   All three have to be checked out.
2. In addition the KNIME plug-in has to be installed.
   The update site is http://www.knime.org/update/2.7/
   
Developing and Testing
======================

Just develop as normal. 

For unit testing do not run a test class directly but execute the eclipse launch configuration 
'openbis knime all tests'. The reason for this is that the normal default class path doesn't work.

For manual integration test with KNIME do the following:
1. Open site.xml in project openbis_knime.update-site.
2. Click on category 'openBIS KNIME Nodes'.
3. Click on 'Add Feature...' and choose in the pop-up dialog the feature 
   ch.systemsx.cisd.openbis.knime
4. Click on the 'Build' button. This creates/update artifacts.jar, content.jar, 
   features/ and plugins/ folder of project openbis_knime.update-site
5. Install/update openBIS KNIME Nodes of the KNIME application from the local update site 
   file:/<absolut path to your Eclipse workspace>/openbis_knime.update-site
   Be sure that update site http://tech.knime.org/update/community-contributions/nightly/ is defined for
   the KNIME application.
   
Build and deploy a new version
==============================

1. Open plugin.xml and change the version on the overview tab. For example from
   13.04.02.qualifier -> 13.04.03.qualifier
2. Does the same for feature.xml in project openbis_knime.feature.
3. Open site.xml in project openbis_knime.update-site.
4. Click on category 'openBIS KNIME Nodes'.
5. Click on 'Add Feature...' and choose in the pop-up dialog the feature 
   ch.systemsx.cisd.openbis.knime
6. Click on the 'Build' button. This creates/update artifacts.jar, content.jar, 
   features/ and plugins/ folder of project openbis_knime.update-site
7. Copy these two JARs and folders to openbis@lascar:/links/groups/cisd/doc/openbis/knime-update-site

