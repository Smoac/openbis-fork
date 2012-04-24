#!/usr/bin/env python

"""
An Jython dropbox for importing HCS image datasets produced by the scripts that generate platonic screening data.

The folder loaded to the dropbox folder should have the same name as the plate that the data will be attached to.
"""

import os
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig, ImageMetadata, Location, Channel, ChannelColor, ChannelColorComponent
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations import ImageTransformationBuffer
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.thumbnails import ResolutionBasedThumbnailsConfiguration

  
class ImageDataSetFlexible(SimpleImageDataConfig):
  def extractImageMetadata(self, imagePath):
    """
    Extracts tile number, channel code and well code for a given relative path to an image.
    Will be called for each file found in the incoming directory which has the allowed image extension.
    
    Example file name: bDZ01-1A_wD17_s3_z123_t321_cGFP
    Returns:
      ImageMetadata
    """
    image_tokens = ImageMetadata()
  
    basename = os.path.splitext(imagePath)[0]
    # 
    token_dict = {}
    for token in basename.split("_"):
      token_dict[token[:1]] = token[1:]
    
    image_tokens.well = token_dict["w"]
    fieldText = token_dict["s"]
    try:
      image_tokens.tileNumber = int(fieldText)
    except ValueError:
      raise Exception("Cannot parse field number from '" + fieldText + "' in '" + basename + "' file name.")
  
    image_tokens.channelCode = token_dict["c"]
    return image_tokens

  def getTileGeometry(self, imageTokens, maxTileNumber):
    """
    Overrides the default implementation which returns (1, maxTileNumber) geometry.
  
    Calculates the width and height of the matrix of tiles (a.k.a. fields or sides) in the well.
  
    Parameter imageMetadataList: a list of metadata for each encountered image
    Parameter maxTileNumber: the biggest tile number among all encountered images
    Returns:
      Geometry
    """
    return Geometry.createFromRowColDimensions(maxTileNumber / 3, 3);
  
  def getTileCoordinates(self, tileNumber, tileGeometry):
    """
    Overrides the default implementation which does the same thing (to demonstrate how this can be done). 
  
    For a given tile number and tiles geometry returns (x,y) which describes where the tile is
    located on the well.
  
    Parameter tileNumber: number of the tile
    Parameter tileGeometry: the geometry of the well matrix
    Returns:
       Location
    """
    columns = tileGeometry.getWidth()
    row = ((tileNumber - 1) / columns) + 1
    col = ((tileNumber - 1) % columns) + 1
    return Location(row, col)
    

def getAvailableChannelTransformations():
  """
  Create a collection of transformations that are applicable to the image
  """
  transforms = ImageTransformationBuffer()
  transforms.appendImageMagicConvert("-edge 1", "Edge detection")
  transforms.appendImageMagicConvert("-radial-blur 30", "Radial Blur")
  transforms.appendImageMagicConvert("-blur 3x.7 -solarize 50% -level 50%,0", "Fuzzy")
  transforms.appendImageMagicConvert("-shade 0x45", "3D 1")
  transforms.appendImageMagicConvert("-shade 90x60", "3D 2")
  transforms.appendImageMagicConvert("-blur 0x3 -shade 120x45 -normalize", "3D 3")
  transforms.appendImageMagicConvert("-motion-blur 0x12+45", "Motion Blur")
  transforms.appendImageMagicConvert("-fft -delete 1 -auto-level -evaluate log 100000", "FFT")
  
  return transforms.getTransformations()

      
if incoming.isDirectory(): 
  imageDataset = ImageDataSetFlexible()
  imageDataset.setRawImageDatasetType()
  imageDataset.setPlate("PLATONIC", incoming.getName())
  transforms = getAvailableChannelTransformations()
#  imageDataset.setGenerateImageRepresentationsWithScaleFactors([0.25, 0.5])    
#  imageDataset.setGenerateImageRepresentationsUsingImageResolutions(['128x128', '256x256'])
  for resolution in ['64x64', '128x128']:
    representation = imageDataset.addGeneratedImageRepresentationWithResolution(resolution)
    representation.setFileFormat('JPEG')
    for channel in ["DAPI", "GFP", "Cy5"]:
      representation.setTransformation(channel, transforms[1].getCode())
  imageDataset.addGeneratedImageRepresentationWithResolution('256x256')

  imageRegistrationDetails = factory.createImageRegistrationDetails(imageDataset, incoming)
  datasetInfo = imageRegistrationDetails.getDataSetInformation()
  channels = [ Channel(code, code) for code in ["DAPI", "GFP", "Cy5"]]
  colorComponents = [ ChannelColorComponent.BLUE, ChannelColorComponent.GREEN, ChannelColorComponent.RED]
  
  # Add transforms to the channels
  for channel in channels:
    channel.setAvailableTransformations(transforms)
  
  datasetInfo.setChannels(channels, colorComponents)
  
  factory.registerImageDataset(imageRegistrationDetails, incoming, service)
