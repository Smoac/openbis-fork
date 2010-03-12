High Content Screening: how to import screen data into openBIS

I. Transform the library file to adhere to openBIS format
------
To transform a QIAGEN library (or a file in similar format) you can use the command line tool called "metadata importer".

It takes following parameters:
    metadata-importer.bat  <library-file-path> <experiment-identifier> <plate-geometry> <space>
Where
    - plate-geometry is one of:
        96_WELLS_8X12
        384_WELLS_16X24
        1536_WELLS_32X48
e.g:
    metadata-importer.bat qiagen-library.csv /LMC/BLUE-PROJECT/TRAIL-EXPERIMENT 384_WELLS_16X24 LMC

The result of the transformation are 3 files, for genes, oligos and plates.
You have to register these files manually from the web browser in openBIS (see next steps).

Here is an example of the library file:

barcode,row,col,sirna,productId,productName,reseqMrnas,geneId,symbol,description
H001-1A,A,1,TCCCGTATAAGTATGTTCCAA,SI00077350,Hs_BMP15_3,NM_005448,9210,BMP15,bone morphogenetic protein 15
H001-1A,B,1,ACCCAGGATATCTCCACCAAA,SI00147777,Hs_CATSPER1_4,NM_053054,117144,CATSPER1,"cation channel, sperm associated 1"
H001-1A,C,1,CCCAACTACCAAGGTCAACAA,SI00354753,Hs_CRYGC_1,NM_020989,1420,CRYGC,"crystallin, gamma C"
...

The mandatory columns in the library file are:
   barcode, row, col, sirna, productId, symbol
	'barcode'	- plate code
	'row'	- well's row (letter: A-Z)
	'col'	- well's column (number, starting with 1)
	'sirna'	- is the oligo sequence (could be optional as well)
	'productId'	- should be the id of the oligo.
	'symbol'	- should be the unique gene symbol
The other columns are optional and can be left empty.

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
   It can take few minutes to register 10.000 genes.
2. Register oligos: as above but select material type "OLIGO" and file oligos.txt. It takes around 10 min for 30.000 oligos.

III. Register the experiment with plates
------
1. Ensure that the appropriate space and project exist
- you can create a new space in "Administration -> Spaces", "Add space"
- you can create a new project in "Administration -> Project -> New"

2. Register the experiment
- go to "Experiment -> New"
- specify the code of your experiment and choose a project for it
- fill other atributes and click "Save". 

3. Register plates and wells
- go to "Sample -> Import"
- for "Sample Type" choose "(multiple)"
- choose a "plates.txt" file generated in the step I. and click "Save"
It takes around 20 min to register 100 plates with 384 wells in each.

IV. Register images
------
To register images just move or copy a directory containing them to the "images dropbox" (see Configuration chapter).
The names of the images should adhere to the schema:
  <plate-code>_<well-code>_<tile-code>
e.g. for a plate H004-1A, well A01 and tile 3 it should be:
  H004-1A_A01_03

If you do not want to copy or move your data there is an option to store links to the data 
(symbolic link to original directory and hard links to images), 
using no additional disk space and without changing your original file structure.
Just create a symbolic link in the "images dropbox" with an absolute path to the directory with plate images. 
Of course this approach requires, that the original data are never moved.
Use the "submit-images.sh" script to create links for the whole directory of plates (see Configuration chapter).

V. Register image analysis results
------
Any csv file (with columns separated by ',') with image analysis results can be registered as a dataset and connected to a plate.
The name of the file should be equal to the plate code, e.g. 'H001-1A.csv' for H001-1A plate.
It's enough to copy such a file into the 'incoming-segmented-jpg' dropbox directory.

If analysis results for all plates are stored in one file, it has to be splitted into one file per plate.
There is a "image-analysis-spliter" tool to do that.
It requires that the first column of analysis file contains plate name and its header is 'barcode' .
All other columns can have any format, although it would be good to introduce a standard at some point,
so that we do not have to configure filters in openBIS each time (columns like cellNumber, hitRate, geneName)

Configuration
-------
- submit-images.sh
- images dropbox - a directory where plate images should be copied.
	There are 3 directories for different types of images:
		/mnt/cluster/openbis/incoming/incoming-raw
		/mnt/cluster/openbis/incoming/incoming-jpg
		/mnt/cluster/openbis/incoming/incoming-segmented-jpg
- image-analysis-spliter - a script to split image analysis results

