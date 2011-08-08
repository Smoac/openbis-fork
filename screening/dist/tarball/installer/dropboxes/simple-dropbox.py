#! /usr/bin/env python
# This is an example Jython dropbox for importing HCS image datasets
 
import os
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import ImageMetadata
     
class MyImageDataSetConfig(SimpleImageDataConfig):
    def extractImageMetadata(self, imagePath):
        image_tokens = ImageMetadata()
     
        basename = os.path.splitext(imagePath)[0]
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
 
if incoming.isDirectory():
    imageDataset = MyImageDataSetConfig()
    imageDataset.setRawImageDatasetType()
    imageDataset.setPlate("MY-SPACE", incoming.getName())
    factory.registerImageDataset(imageDataset, incoming, service)
