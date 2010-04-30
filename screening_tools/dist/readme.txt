High Content Screening: how to import screen data into openBIS

To simplify data import all new experiments should be created in one space. 
After all the data has been imported the whole project can be moved to the appropriate space 
(or a chosen experiment can be moved to a project in a different space).

[LMC configuration]
In the case of LMC the space where all the experiments should be creates is called 'LMC'.
[LMC configuration]

I. Prepare a library file
--------
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
	'geneId'	- id of the gene in the library, currently not used, but may be needed in future.

If there are any other columns, they are treated as oligo properties.
In this case appropriate property types must exist and be assigned to the OLIGO material type in openBIS.
You have to delete unnecessary columns from the library file.

In following cases the row in the file will be ignored:
- gene symbol is empty
- gene with the same symbol already exists
Oligo IDs must be unique, you may need to add a prefix to the "productId" column.

II. Import the library file
------
1. Ensure that the appropriate space and project exist
- you can create a new space in "Administration -> Spaces", "Add space"
- you can create a new project in "Administration -> Project -> New"

2. Register the experiment
- go to "Experiment -> New"
- specify the code of your experiment and choose a project for it
- fill other atributes and click "Save". 

3. Go to "Samples -> Import" menu and select 'LIBRARY'.
Select the experiment to which all the samples will be attached, other attributes and a file with a library.

It takes around 30-60 min to register a big screen with 300 plates with 384 wells in each plate.
An email will be sent to you when the operation will be finished or some errors will occur.

III. Register images
------
To register images just move or copy a directory containing them to the "images dropbox" (see Configuration chapter).

[LMC configuration]
The names of the images should adhere to the schema:
  <plate-code>_<well-code>_<tile-code>
e.g. for a plate H004-1A, well A01 and tile 3 it should be:
  H004-1A_A01_03
[LMC configuration]

If you do not want to copy or move your data there is an option to store links to the data 
(symbolic link to original directory and hard links to images), 
using no additional disk space and without changing your original file structure.
Just create a symbolic link in the "images dropbox" with an absolute path to the directory with plate images. 
Of course this approach requires, that the original data are never moved.

[LMC configuration]
Use the "submit-images.sh" script to create links for the whole directory of plates.
- images dropbox - a directory where plate images should be copied.
	There are 3 directories for different types of images:
		z:/openbis/incoming/incoming-raw
		z:/openbis/incoming/incoming-jpg
		z:/openbis/incoming/incoming-segmented-jpg
[LMC configuration]


IV. Register image analysis results
------
Any csv file (with columns separated by ',') with image analysis results can be registered as a dataset and connected to a plate.
The name of the file should be equal to the plate code, e.g. 'H001-1A.csv' for H001-1A plate.
It's enough to copy such a file into the 'incoming-plate-analysis' dropbox directory.

At the moment openBIS requires images analysis results for each plate to be in a separate file. 
If you have them in one file you can transform it to many files in 4 simple steps:
1. ensure that the first 3 columns in the CSV file are:
	barcode, row, col
2. copy a program to split the image analysis results to you local folder (see configuration below).
3. run the splitting script:
	 java -jar openbis-analysis-data-splitter.jar <image-analysis-file-name>
  It creates a directory 'plates'. You will find one CSV file for each plate inside.
4. copy all the csv files from the 'plates' directory to the incoming directory for image analysis results.

That's all - analysis results will be imported assuming that the plates already exist.

[LMC configuration]
- The splitter program can be found on cluster machine:
		z:\openbis\tools\openbis-analysis-data-splitter.jar
- The incoming directory for image analysis results is:
  	z:\openbis\incoming\incoming-plate-analysis\
[LMC configuration]


