High Content Screening: how to import a library into openBIS
-------------------

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
You have to register these files manually from the web browser in openBIS in following order:
- genes: go to "Material -> Import" menu and select material type "GENE". Then select a genes.txt file and click "Save".
- oligos: as above but select material type "OLIGO"
- plates: go to "Sample -> Import" and select sample type "(multiple)" - you will register plates and wells at the same time.

----- Note for Java developers:
If your library file format is slightly different you can get the "metadata importer" source code and modify it to fit your needs.
The source code is available at:
    https://svncisd.ethz.ch/repos/cisd/screening_tools/trunk
You are interested in this package:
    ch.systemsx.cisd.openbis.metadata
You can build the package using ant 'metadata-jar' target (build/build.xml).
----- 