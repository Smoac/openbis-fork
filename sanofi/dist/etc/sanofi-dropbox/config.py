from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import OriginalDataStorageFormat

""" Switch to True in development environment to use a mock of Abase database """
TEST_MODE=False

"""
Allows to recognize that the subdirectory of the incoming dataset directory contains overlay images.
This text has to appear in the subdirectory name.
"""
OVERLAYS_DIR_PATTERN = "_ROITiff"
""" name of the color which should be treated as transparent in overlays """
OVERLAYS_TRANSPARENT_COLOR = "black"
    
""" should thumbnails be generated? """
GENERATE_THUMBNAILS = True

""" the maximal width and height of the generated thumbnails """
MAX_THUMNAIL_WIDTH_AND_HEIGHT = 256

"""
Number of threads that are used for thumbnail generation will be equal to:
   this constant * number of processor cores
Set to 1/<number-of-cores> if ImageMagic 'convert' tool is not installed.
"""
ALLOWED_MACHINE_LOAD_DURING_THUMBNAIL_GENERATION = 1.0

""" should all dataset in one experiment use the same channels? """
STORE_CHANNELS_ON_EXPERIMENT_LEVEL = False

""" should the original data be stored in the original form or should we pack them into one container? """
ORIGINAL_DATA_STORAGE_FORMAT = OriginalDataStorageFormat.UNCHANGED

""" Change to 'False' if 'convert' tool is not installed """
USE_IMAGE_MAGIC_CONVERT_TOOL = False

""" -------------------------------------------- """
"""         This part rarely changes             """ 
""" -------------------------------------------- """


""" the url of the Sanofi's openBIS installation """
OPENBIS_URL = "http://openbis-test-bw.sanofi.com:8080/openbis"

""" experiment property name where emails are fetched from """
EXPERIMENT_RECIPIENTS_PROPCODE = "OBSERVER_EMAILS"
""" dataset property name where acquisition batch is saved """
IMAGE_DATASET_BATCH_PROPCODE = "ACQUISITION_BATCH"

""" the sample type identifying plates """
PLATE_TYPE = "PLATE"
""" file format code of files in a new image dataset """
IMAGE_DATASET_FILE_FORMAT = "TIFF"
""" file format of the image overlay dataset """
OVERLAY_IMAGE_FILE_FORMAT = "PNG"
""" file format of the analysis dataset """
ANALYSIS_FILE_FORMAT = "CSV"
