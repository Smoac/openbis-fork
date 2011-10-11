import os
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import ImageMetadata, SimpleImageDataConfig, Location
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry

from java.io import File
from ch.systemsx.cisd.imagereaders import ImageReaderFactory, ImageID

class OperaFlexImageDataSet(SimpleImageDataConfig):
    #imageReader = ImageReaderFactory.tryGetReader("BioFormats", "FlexReader")

    def extractImagesMetadata(self, imagePath, imageIdentifiers):
        tokens = []
        for image in imageIdentifiers:
            #id = ImageID(image.getSeriesIndex(), image.getTimeSeriesIndex(), image.getFocalPlaneIndex(), image.getColorChannelIndex())
            #metadata = self.imageReader.readMetaData(File(incoming, imagePath), id, None)
            #print metadata
            
            ix = image.getTimeSeriesIndex()

            token = ImageMetadata()

            basename = os.path.basename(imagePath)
            fileName = os.path.splitext(basename)[0]
            row = int(fileName[0:3])
            rowLetter = chr(ord('A') - 1 + row)
            col = int(fileName[3:6])
            token.well = rowLetter + str(col)

            channels = ["Channel 1", "Channel 2", "Channel 3"]
            channelsNum = len(channels)
            
            token.tileNumber = (ix / channelsNum) + 1
            token.channelCode = channels[ix % channelsNum]
            token.imageIdentifier = image
            #print token
            tokens.append(token)
        return tokens

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
        
    def geom(self, row, col):
        return Geometry.createFromRowColDimensions(row, col)        
    
    def getTileCoordinates(self, tileNumber, tileGeometry):
        mapping = [Location(1,2), Location(2,1), Location(1,1), Location(2,2)]
        if tileNumber < 5:
            return mapping[tileNumber - 1]
        else:
            columns = tileGeometry.getWidth()
            row = ((tileNumber - 1) / columns) + 1
            col = ((tileNumber - 1) % columns) + 1
            return Location(row, col)
    
if incoming.isDirectory():
    imageDataset = OperaFlexImageDataSet()
    imageDataset.setRawImageDatasetType()
    #imageDataset.setImageLibrary("BioFormats", "FlexReader")
    imageDataset.setImageLibrary("BioFormats", "TiffDelegateReader")
    imageDataset.setRecognizedImageExtensions(["flex"])
    space = "TEST"
    plateName = "PLATE1"
    imageDataset.setPlate(space, plateName)
    factory.registerImageDataset(imageDataset, incoming, service)