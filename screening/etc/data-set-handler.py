#! /usr/bin/env python
# This is an example Jython dropbox for importing HCS image datasets

import os
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier as identifier
import ch.systemsx.cisd.openbis.generic.shared.basic.dto as dto
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import ImageFileInfo, ImageDataSetInformation, Channel

# ------------
# You may want to modify this part.
# ------------

# type of the new dataset
DATASET_TYPE = "HCS_IMAGE"
# file format of files in a new dataset
FILE_FORMAT = "TIFF"
# space where the plate for which the dataset has been acquired exist
SAMPLE_SPACE = "DEMO"
# number of rile rows and columns on a well 
TILE_ROWS_NUMBER = 2
TILE_COLUMNS_NUMBER = 2
# available channels, for each channel code and label have to be specified
CHANNELS = [ Channel("DAPI", "DAPI"), Channel("FITC", "FITC"), Channel("CY5", "Cy5") ]

RECOGNIZED_IMAGES_EXTENSIONS = ["tiff", "tif", "png", "gif", "jpg", "jpeg"]

# extracts code of the sample from the directory name
def extract_sample_code(file_basename):
    return file_basename.split(".")[0]

# Creates ImageFileInfo for a given path to an image
# Example file name: A - 1(fld 1 wv Cy5 - Cy5).tif
def create_image_info(path):
    basename = os.path.splitext(path)[0]

    wellText = basename[0:find(basename, "(")] # A - 1
    well = wellText.replace(" - ", "")
    fieldText = basename[find(basename, "fld ")+4 : find(basename, " wv")]
    try:
        fieldNum = int(fieldText) - 1
    except ValueError:
        raise Exception("Cannot parse field number from '"+fieldText+"' in '"+basename+"' file name.")
    channel = basename[rfind(basename, " - ") + 3 : -1]
    
    tileRow = (fieldNum / TILE_COLUMNS_NUMBER) + 1
    tileCol = (fieldNum % TILE_COLUMNS_NUMBER) + 1
    img = ImageFileInfo(channel, tileRow, tileCol, path)
    img.setWell(well)
    return img

# ------------
# You probably do not want to modify this part
# ------------

def rfind(text, pattern):
    ix = text.rfind(pattern)
    ensurePatternFound(ix, text, pattern)
    return ix

def find(text, pattern):
    ix = text.find(pattern)
    ensurePatternFound(ix, text, pattern)
    return ix

def ensurePatternFound(ix, file, pattern):
    if ix == -1:
        raise Exception("Cannot find '"+pattern+"' pattern in file name '"+file+"'")    

def create_dataset():
    basename = extract_file_basename(incoming.getName())
    dataset = Dataset()
    
    dataset.dataset_type = DATASET_TYPE
    dataset.file_format = FILE_FORMAT
    dataset.sample_space = SAMPLE_SPACE
    dataset.sample_code = extract_sample_code(basename)
    
    dataset.tile_rows_number = TILE_ROWS_NUMBER
    dataset.tile_columns_number = TILE_COLUMNS_NUMBER
    
    return dataset

def get_available_channels():
    return CHANNELS

class Dataset:
    sample_code = None
    sample_space = None
    dataset_type = None
    file_format = None
    # -- imaging specific
    tile_rows_number = -1
    tile_columns_number = -1

def create_image_infos(dir):
    images = []
    dir_path = dir.getPath()
    for file in os.listdir(dir_path):
    	ext = os.path.splitext(file)[1][1:].lower()
	try:
		extIx = RECOGNIZED_IMAGES_EXTENSIONS.index(ext)
                # not reached if extension not found
		image = create_image_info(file)
                images.append(image)	
	except ValueError:
		pass # extension not recognized	
    return images

def set_dataset_info(registration_details, dataset):
    data_set_info = registration_details.getDataSetInformation()
    data_set_info.setSpaceCode(dataset.sample_space)
    data_set_info.setSampleCode(dataset.sample_code)
    registration_details.setFileFormatType(dto.FileFormatType(dataset.file_format));
    registration_details.setDataSetType(dto.DataSetType(dataset.dataset_type));
    registration_details.setMeasuredData(True);
    # -- imaging specific
    data_set_info.setTileGeometry(dataset.tile_rows_number, dataset.tile_columns_number)
    images = create_image_infos(incoming)
    data_set_info.setImages(images)
    channels = get_available_channels()
    data_set_info.setChannels(channels)
    
def extract_file_basename(filename):
    lastDot = filename.rfind(".")
    if lastDot != -1:
        return filename[0:lastDot]
    else:
        return filename
     
dataset = create_dataset()
registration_details = factory.createRegistrationDetails()
set_dataset_info(registration_details, dataset)
service.queueDataSetRegistration(incoming, registration_details)
