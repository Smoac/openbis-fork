#! /usr/bin/env python

import os
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import *
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry
     
class IBrain2ImageDataSetConfig(SimpleImageDataConfig):
	THUMBANAIL_SIZE = 200
	
    def extractImageMetadata(self, imagePath):
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
     
        image_tokens.channelCode = basename.split("_")[-1] + " ("+ token_dict["c"] + ")"
        return image_tokens

    def geom(self, row, col):
		return Geometry.createFromRowColDimensions(row, col)

    """ 
    Parameters:
        image_tokens_list - list of ImageTokens
    Returns:  (rows, columns) tuple describing the matrix of tiles (aka fields or sides) in the well  
    """
    def getTileGeometry(self, imageTokens, maxTileNumber):
        # if a number of tiles is strange, assume that one tile is missing
        if maxTileNumber == 5 or maxTileNumber == 7 or maxTileNumber == 11 or maxTileNumber == 13:
            maxTileNumber = maxTileNumber + 1
    
        if maxTileNumber % 4 == 0 and maxTileNumber != 4:
            return self.geom(4, maxTileNumber / 4) # (4,2), (4,4)
        elif maxTileNumber % 3 == 0:
            return self.geom(maxTileNumber / 3, 3) # (3,3), (4,3), (5,3)
        elif maxTileNumber % 2 == 0:
            return self.geom(maxTileNumber / 2, 2) # (2,2), (3,2), (5,2), (7,2)
        else:
            return self.geom(maxTileNumber, 1)
           
            