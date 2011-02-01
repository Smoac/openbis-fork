#! /usr/bin/env python
# This is an example Jython dropbox for importing HCS image datasets

import os
import shutil
import random

import ch.systemsx.cisd.openbis.generic.shared.basic.dto as dto
from ch.systemsx.cisd.openbis.generic.shared.basic.dto import FileFormatType, DataSetType, SampleType, NewSample
from ch.systemsx.cisd.openbis.generic.shared.dto.identifier import SampleIdentifier
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import *
from ch.systemsx.cisd.openbis.dss.etl.custom.geexplorer import GEExplorerImageAnalysisResultParser
from java.io import File

# ------------
# Dropbox specific image dataset registration. You may want to modify this part.
# ------------

""" type of the new image dataset """
IMAGE_DATASET_TYPE = "HCS_IMAGE"
""" file format of files in a new image dataset """
IMAGE_FILE_FORMAT = "TIFF"
""" type of the new analysis dataset """
ANALYSIS_DATASET_TYPE = "HCS_IMAGE_ANALYSIS_DATA"
""" file format of the analysis dataset file """
ANALYSIS_FILE_FORMAT = "CSV"

""" space where the plate for which the dataset has been acquired exist """
PLATE_SPACE = "DEMO"

RECOGNIZED_IMAGES_EXTENSIONS = ["tiff", "tif", "png", "gif", "jpg", "jpeg"]


# ---------
""" sample type code of the plate, needed if a new sample is registered automatically """
PLATE_TYPE_CODE = "PLATE"
""" project and experiment where new plates will be registered """
DEFAULT_PROJECT_CODE = "TEST"
DEFAULT_EXPERIMENT_CODE = "SANOFI"
PLATE_GEOMETRY_PROPERTY_CODE = "$PLATE_GEOMETRY"
PLATE_GEOMETRY = "384_WELLS_16X24"

""" extracts code of the sample from the directory name """
def extract_sample_code(incoming_name):
    file_basename = extract_file_basename(incoming_name)
    #return file_basename.split(".")[0]
    code = file_basename[file_basename.find("plates_") + 7 : file_basename.rfind("_") ]
    if code == "":
        code = file_basename
    return code

def get_tile_coords(tile_num, tile_geometry):
    columns = tile_geometry[1]
    row = ((tile_num - 1) / columns) + 1
    col = ((tile_num - 1) % columns) + 1
    return (row, col)

def get_max_tile_number(image_tokens_list):
    max_tile = 0
    for image_tokens in image_tokens_list:
        max_tile = max(max_tile, image_tokens.tile)
    return max_tile

""" returns (rows, columns) """
def get_tile_geometry(image_tokens_list):
    max_tile = get_max_tile_number(image_tokens_list)
    if max_tile % 4 == 0 and max_tile != 4:
        return (max_tile / 4, 4)
    elif max_tile % 3 == 0:
        return (max_tile / 3, 3)
    elif max_tile % 2 == 0:
        return (max_tile / 2, 2)
    else:
        return (max_tile, 1)

class ImageTokens:
    channel = None
    tile = -1
    path = ""
    well = ""

"""
Creates ImageTokens for a given path to an image
Example file name: A - 1(fld 1 wv Cy5 - Cy5).tif
"""
def create_image_tokens(path):
    image_tokens = ImageTokens()
    image_tokens.path = path

    basename = os.path.splitext(path)[0]

    wellText = basename[0:find(basename, "(")] # A - 1
    image_tokens.well = wellText.replace(" - ", "")
    
    fieldText = basename[find(basename, "fld ") + 4 : find(basename, " wv")]
    try:
        image_tokens.tile = int(fieldText)
        #print "image_tokens.tile", image_tokens.tile
    except ValueError:
        raise Exception("Cannot parse field number from '" + fieldText + "' in '" + basename + "' file name.")

    image_tokens.channel = basename[rfind(basename, " - ") + 3 :-1]
    return image_tokens
    
""" 
Creates ImageFileInfo for a given path to an image
Example file name: A - 1(fld 1 wv Cy5 - Cy5).tif
"""
def create_image_info(image_tokens, tile_geometry):
    fieldNum = image_tokens.tile - 1
    tileRow = (fieldNum / tile_geometry[1]) + 1
    tileCol = (fieldNum % tile_geometry[1]) + 1

    img = ImageFileInfo(image_tokens.channel, tileRow, tileCol, image_tokens.path)
    img.setWell(image_tokens.well)
    return img

def parse_image_tokens(dir):
    image_tokens_list = []
    dir_path = dir.getPath()
    for file in os.listdir(dir_path):
        ext = get_file_ext(file)
        try:
            extIx = RECOGNIZED_IMAGES_EXTENSIONS.index(ext)
            # not reached if extension not found
            image_tokens = create_image_tokens(file)
            image_tokens_list.append(image_tokens)    
        except ValueError:
            pass # extension not recognized    
    return image_tokens_list

    
def create_image_infos(image_tokens_list, tile_geometry):
    images = []
    for image_tokens in image_tokens_list:
        image = create_image_info(image_tokens, tile_geometry)
        images.append(image)    
    return images

# ------------
# Generic utility
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
        raise Exception("Cannot find '" + pattern + "' pattern in file name '" + file + "'")    

def extract_file_basename(filename):
    lastDot = filename.rfind(".")
    if lastDot != -1:
        return filename[0:lastDot]
    else:
        return filename

def get_file_ext(file):
    return os.path.splitext(file)[1][1:].lower()

def find_file_by_ext(incoming_file, expected_ext):
    if not incoming_file.isDirectory():
        return None
    incoming_path = incoming_file.getPath()
    for file in os.listdir(incoming_path):
        ext = get_file_ext(file)
        if ext.upper() == expected_ext.upper():
            return File(incoming_path, file)
    return None

def find_dir(incoming_file, dir_name_marker):
    if not incoming_file.isDirectory():
        return None
    incoming_path = incoming_file.getPath()
    for file in os.listdir(incoming_path):
        if dir_name_marker.upper() in file.upper():
            return File(incoming_path, file)
    return None

def get_random_string():
    return str(int(random.random()*1000000000))

def get_tmp_dir(incoming, label):
    dropbox_parent_dir = incoming.getParentFile().getParent()
    tmp_dir = File(dropbox_parent_dir, "tmp")
    os.mkdir(tmp_dir.getPath())
    tmp_labeled_dir = File(tmp_dir, label + ".tmp." + get_random_string())
    os.mkdir(tmp_labeled_dir.getPath())
    return tmp_labeled_dir

# ------------
# Generic dataset registration
# ------------

class Dataset:
    sample_code = ""
    sample_space = ""
    dataset_type = ""
    file_format = ""
    is_measured = True
    parent_dataset_code = ""

def set_dataset_details(dataset, registration_details):
    data_set_info = registration_details.getDataSetInformation()
    data_set_info.setSpaceCode(dataset.sample_space)
    data_set_info.setSampleCode(dataset.sample_code)
    if (dataset.parent_dataset_code):
        data_set_info.setParentDataSetCodes([dataset.parent_dataset_code])
    registration_details.setFileFormatType(FileFormatType(dataset.file_format));
    registration_details.setDataSetType(DataSetType(dataset.dataset_type));
    registration_details.setMeasuredData(dataset.is_measured);
    return registration_details

# ------------
# Image dataset registration
# ------------

class ImageDataset: # extends Dataset
    tile_num = -1
    tile_rows_number = -1
    tile_columns_number = -1
    images = None
    channels = None

def get_available_channels(images):
    channel_codes = {}
    for image in images:
        channel_codes[image.getChannelCode()] = 1
    channels = []
    for channelCode in channel_codes.keys():
        channels.append(Channel(channelCode, channelCode))
    return channels

def create_image_dataset(incoming):
    dataset = ImageDataset()
    
    dataset.dataset_type = IMAGE_DATASET_TYPE
    dataset.file_format = IMAGE_FILE_FORMAT
    dataset.sample_space = PLATE_SPACE
    dataset.sample_code = extract_sample_code(incoming.getName())
    dataset.is_measured = True
    dataset.parent_dataset_code = None

    image_tokens_list = parse_image_tokens(incoming)
    tile_geometry = get_tile_geometry(image_tokens_list)
    dataset.images = create_image_infos(image_tokens_list, tile_geometry)
    dataset.channels = get_available_channels(dataset.images)
    
    dataset.tile_rows_number = tile_geometry[0]
    dataset.tile_columns_number = tile_geometry[1]

    return dataset

def create_image_dataset_details(image_dataset):
    registration_details = factory.createImageRegistrationDetails()
    
    set_dataset_details(image_dataset, registration_details)

    data_set_info = registration_details.getDataSetInformation()
    data_set_info.setTileGeometry(image_dataset.tile_rows_number, image_dataset.tile_columns_number)
    data_set_info.setImages(image_dataset.images)
    data_set_info.setChannels(image_dataset.channels)
    return registration_details
    
# ---------------------

def create_overlay_dataset(overlays_dir, image_dataset, img_dataset_code):
    dataset = ImageDataset()
    
    dataset.dataset_type = OVERLAY_IMAGE_DATASET_TYPE
    dataset.file_format = OVERLAY_IMAGE_FILE_FORMAT
    dataset.sample_space = image_dataset.sample_space
    dataset.sample_code = image_dataset.sample_code
    dataset.is_measured = False
    dataset.parent_dataset_code = img_dataset_code

    tile_geometry = (image_dataset.tile_rows_number, image_dataset.tile_columns_number)

    image_tokens_list = parse_image_tokens(overlays_dir)
    dataset.images = create_image_infos(image_tokens_list, tile_geometry)
    dataset.channels = get_available_channels(dataset.images)
    
    dataset.tile_rows_number = tile_geometry[0]
    dataset.tile_columns_number = tile_geometry[1]

    return dataset

def create_overlay_dataset_details(overlays_dir, image_dataset, img_dataset_code):
    overlay_dataset = create_overlay_dataset(overlays_dir, image_dataset, img_dataset_code)
    overlay_dataset_details = create_image_dataset_details(overlay_dataset)

    data_set_info = overlay_dataset_details.getDataSetInformation()
    config = ImageStorageConfiguraton.createDefault()
    config.setStoreChannelsOnExperimentLevel(False)
    data_set_info.setImageStorageConfiguraton(config)
    return overlay_dataset_details

# ---------------------

def create_analysis_dataset(sample_space, sample_code, parent_dataset_code):
    dataset = Dataset()
    
    dataset.dataset_type = ANALYSIS_DATASET_TYPE
    dataset.file_format = ANALYSIS_FILE_FORMAT
    dataset.sample_space = sample_space
    dataset.sample_code = sample_code
    dataset.is_measured = False
    dataset.parent_dataset_code = parent_dataset_code
    return dataset

def create_analysis_dataset_details(sample_space, sample_code, parent_dataset_code):
    analysis_registration_details = factory.createRegistrationDetails()
    analysis_dataset = create_analysis_dataset(sample_space, sample_code, parent_dataset_code)
    set_dataset_details(analysis_dataset, analysis_registration_details)
    return analysis_registration_details

def register_sample_if_necessary(space_code, project_code, experiment_code, sample_code):   
    openbis = state.getOpenBisService()
    sampleIdentifier = SampleIdentifier.create(space_code, sample_code)
    if (openbis.tryGetSampleWithExperiment(sampleIdentifier) == None):
        sample = NewSample()
        sampleType = SampleType()
        sampleType.setCode(PLATE_TYPE_CODE)
        sample.setSampleType(sampleType)
        sample.setIdentifier(sampleIdentifier.toString())
        
        property = dto.VocabularyTermEntityProperty();
        vocabularyTerm = dto.VocabularyTerm();
        vocabularyTerm.setCode(PLATE_GEOMETRY);
        property.setVocabularyTerm(vocabularyTerm);
        propertyType = dto.PropertyType();
        dataType = dto.DataType();
        dataType.setCode(dto.DataTypeCode.CONTROLLEDVOCABULARY);
        propertyType.setDataType(dataType);
        propertyType.setCode(PLATE_GEOMETRY_PROPERTY_CODE);
        property.setPropertyType(propertyType);
        sample.setProperties([ property ])
        
        sample.setExperimentIdentifier("/" + space_code + "/" + project_code + "/" + experiment_code)
        openbis.registerSample(sample, None)

# ---------------------
       
"""
Allows to recognize that the subdirectory of the incoming dataset directory contains overlay images.
This text has to appear in the subdirectory name. 
"""
OVERLAYS_DIR_PATTERN = "overlays"

def register_images_with_overlays_and_analysis(incoming):
    if not incoming.isDirectory():
        return
    analysis_file = find_file_by_ext(incoming, "xml")
    overlays_dir = find_dir(incoming, OVERLAYS_DIR_PATTERN)
    
    image_dataset = create_image_dataset(incoming)
    image_dataset_details = create_image_dataset_details(image_dataset)

    plate_code = image_dataset_details.getDataSetInformation().getSampleCode()
    space_code = image_dataset_details.getDataSetInformation().getSpaceCode()
    
    if overlays_dir != None:
        overlays_label = "overlays"
        tmp_overlays_parent_dir = get_tmp_dir(incoming, overlays_label)
        tmp_overlays_dir = File(tmp_overlays_parent_dir, overlays_label)
        os.rename(overlays_dir, tmp_overlays_dir)
        
    if analysis_file != None:
        tmp_analysis_dir = get_tmp_dir(incoming, "image-analysis")
        tmp_analysis_file = File(tmp_analysis_dir, analysis_file.getName())
        GEExplorerImageAnalysisResultParser(analysis_file.getPath()).writeCSV(tmp_analysis_file)
    
    register_sample_if_necessary(space_code, DEFAULT_PROJECT_CODE, DEFAULT_EXPERIMENT_CODE, plate_code)
    img_dataset_code = service.queueDataSetRegistration(incoming, image_dataset_details).getCode()
    
    if overlays_dir != None:
        overlay_dataset_details = create_overlay_dataset_details(tmp_overlays_dir, image_dataset, img_dataset_code)
        service.queueDataSetRegistration(tmp_overlays_dir, overlay_dataset_details)
        
    if analysis_file != None:
        analysis_registration_details = create_analysis_dataset_details(space_code, plate_code, img_dataset_code)  
        service.queueDataSetRegistration(tmp_analysis_file, analysis_registration_details)


register_images_with_overlays_and_analysis(incoming)
