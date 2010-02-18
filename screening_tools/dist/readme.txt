High Content Screening: how to import screen data into openBIS

I. Transform the library file to adhere to openBIS format
------
To transform a QIAGEN library (or a file in similar format) you can use the command line tool called "metadata importer".

It takes following parameters:
    metadata-importer.bat  <master-plate-file-path> <experiment-identifier> <plate-geometry> <group>
Where
    - plate-geometry is one of:
        96_WELLS_8X12
        384_WELLS_16X24
        1536_WELLS_32X48
e.g:
    metadata-importer.bat master-plate-example.csv /LMC/BLUE-PROJECT/TRAIL-EXPERIMENT 384_WELLS_16X24 LMC

The result of the transformation are 3 files, for genes, oligos and plates.
You have to register these files manually from the web browser in openBIS (see next steps).

Note for Java developers:
If your library file format is slightly different you can get the "metadata importer" source code and modify it to fit your needs.
The source code is available at:
    https://svncisd.ethz.ch/repos/cisd/screening_tools/trunk
You are interested in this package:
    ch.systemsx.cisd.openbis.metadata
You can build the package using ant 'metadata-jar' target (build/build.xml).

II. Register genes and oligos
------
1. Register genes: go to "Material -> Import" menu and select material type "GENE". Then select a genes.txt file and click "Save".
2. Register oligos: as above but select material type "OLIGO"

III. Register the experiment with plates
------
1. Ensure that the appropriate group and project exist
- you can create a new group in "Administration -> Groups", "Add group"
- you can create a new project in "Administration -> Project -> New"

2. Register the experiment with the plates
Go to "Experiment -> New"
- specify the code of your experiment and choose a project for it
- select "Samples: register from a file and attach"
- for "Sample Type" choose "(multiple)"
- choose a "plates.txt" file generated in the step I.
- fill other atributes and click "Save". 
It will take some time to register an experiment if you have a lot of plates in the file. 

