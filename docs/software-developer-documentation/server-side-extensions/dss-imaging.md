Imaging technology
==================================

## Introduction

imaging technology is an extension that allows to process raw scientific data stored in datasets into easy to analyse images. 

This technology is split into following parts:
- Imaging Service
- Imaging Gallery Viewer
- Imaging DataSet Viewer


## How to enable this technology

"imaging" core plugin, together with simple dataset type can be found here: https://sissource.ethz.ch/sispub/openbis/-/tree/master/core-plugin-openbis/dist/core-plugins/imaging/1?ref_type=heads

1. It needs to be downloaded in the installation's `servers/core-plugins` folder
2. 'imaging' needs to be enabled in the `servers/core-plugins/core-plugins.properties` 


## Data Model

The new imaging extension follows the current eln-lims data model.

This structure could initially seem to have a couple of additional levels that not everybody will actively use, but in practice is the most flexible since allows to use all openBIS linking features between Experiments, Experimental Steps and other Objects.

Space (Space): Used for rights management\
&nbsp;  Project (Project): Used for rights management\
&nbsp;&nbsp;  Collection (Collection): Allows Object Aggregation\
&nbsp;&nbsp;&nbsp;  Experiment (Object): Allows Objects linking\
&nbsp;&nbsp;&nbsp;&nbsp;  Exp. Step (Object): Allows Objects linking and DataSets\
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  DataSet (DataSet): Allows to attach data

Different DataSet Types can have different properties and metadata sections. A default template type called IMAGING_DATA is provided. Additionally, each lab can create their own types with different metadata - a core requirement for dataset type is to contain an internal property called $IMAGING_DATA_CONFIG.

### $IMAGING_DATA_CONFIG

`$IMAGING_DATA_CONFIG` is a special JSON property designed to store imaging data that can be used by both backend and frontend components of this technology.


## Imaging Service
This section describes how Imaging Service works and how it can be extended.

Imaging service is implemented using Custom Services technology for DSS (For more details see [Custom Datastore Server Services](./dss-services.md)). It is a special service that, when requested, runs special "adaptor" java class (specified in $IMAGING_DATA_CONFIG) which computes images based on associated dataset files and some input parameters.

### Adaptors
Currently, there are 3 types of adaptors that are implemented:
- [ImagingDataSetExampleAdaptor](https://sissource.ethz.ch/sispub/openbis/-/blob/master/core-plugin-openbis/dist/core-plugins/imaging/1/dss/services/imaging/lib/premise-sources/source/java/ch/ethz/sis/openbis/generic/server/dss/plugins/imaging/adaptor/ImagingDataSetExampleAdaptor.java?ref_type=heads) - an example adaptor written in Java, it produces a random image.
- [ImagingDataSetJythonAdaptor](https://sissource.ethz.ch/sispub/openbis/-/blob/master/core-plugin-openbis/dist/core-plugins/imaging/1/dss/services/imaging/lib/premise-sources/source/java/ch/ethz/sis/openbis/generic/server/dss/plugins/imaging/adaptor/ImagingDataSetJythonAdaptor.java?ref_type=heads) - an adaptor that makes use of Jython.
- [ImagingDataSetPythonAdaptor](https://sissource.ethz.ch/sispub/openbis/-/blob/master/core-plugin-openbis/dist/core-plugins/imaging/1/dss/services/imaging/lib/premise-sources/source/java/ch/ethz/sis/openbis/generic/server/dss/plugins/imaging/adaptor/ImagingDataSetPythonAdaptor.java?ref_type=heads) - abstract adaptor that allows to implement image computation logic as a python script. More can be read here: [Python adaptor] 

All of these adaptor have one thing in common: they implement [IImagingDataSetAdaptor](https://sissource.ethz.ch/sispub/openbis/-/blob/master/core-plugin-openbis/dist/core-plugins/imaging/1/dss/services/imaging/lib/premise-sources/source/java/ch/ethz/sis/openbis/generic/server/dss/plugins/imaging/adaptor/IImagingDataSetAdaptor.java?ref_type=heads) interface.

Writing a completely new adaptor requires:
1. Writing a Java class that implements IImagingDataSetAdaptor interface (by either interface realisation or extension).
2. Compiling java classes into a .jar file.
3. Including .jar library in  `servers/core-plugins/imaging/<version number>/dss/services/imaging/lib` folder.
4. Restarting Openbis.

#### Python adaptor
ImagingDataSetPythonAdaptor is an abstract class that contains logic for handling adaptor logic written in a python script. A concrete implementation may look like this:

```java
package ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.adaptor;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class NanonisSxmAdaptor extends ImagingDataSetPythonAdaptor
{
   private final String SXM_SCRIPT_PROPERTY = "nanonis-sxm"; //propertyPath

   public NanonisSxmAdaptor(Properties properties)
   {
      String scriptProperty = properties.getProperty(SXM_SCRIPT_PROPERTY, "");
      if (scriptProperty.trim().isEmpty())
      {
         throw new UserFailureException(
                 "There is no script path property called '" + SXM_SCRIPT_PROPERTY + "' defined for this adaptor!");
      }
      Path script = Paths.get(scriptProperty);
      if (!Files.exists(script))
      {
         throw new UserFailureException("Script file " + script + " does not exists!");
      }
      this.scriptPath = script.toString();
      this.pythonPath = properties.getProperty("python3-path", "python3");
   }

}

```
In this example 3 elements are defined: propertyPath, *scriptPath* and *pythonPath*

propertyPath - name of a property in `servers/core-plugins/imaging/<version number>/dss/services/imaging/plugin.properties`
*scriptPath* - path to a python script to be executed by this adaptor, this path is defined by propertyPath property.
*pythonPath* - path to a python environment to execute script, defined in `plugin.properties` file as `python3-path`. If such property is not found, a default python3 environment is used.


Link to existing adaptor: [Nanonis SXM adaptor](https://sissource.ethz.ch/sispub/openbis/-/blob/master/core-plugin-openbis/dist/core-plugins/imaging/1/dss/services/imaging/lib/premise-sources/source/java/ch/ethz/sis/openbis/generic/server/dss/plugins/imaging/adaptor/NanonisSxmAdaptor.java?ref_type=heads)

### Communication with the service
[imaging-test-data](https://sissource.ethz.ch/sispub/openbis/-/tree/master/core-plugin-openbis/dist/core-plugins/imaging-test-data?ref_type=heads) is a core plugin containing test data that can be used for the tests. It also contains special python script [imaging.py](https://sissource.ethz.ch/sispub/openbis/-/blob/master/core-plugin-openbis/dist/core-plugins/imaging-test-data/1/as/master-data/nanonis_example/imaging.py?ref_type=heads) containing helpful methods for creating, updating and exporting imaging data.



## Imaging DataSet Viewer
Work-in-progress

## Imaging Gallery Viewer
Work-in-progress





