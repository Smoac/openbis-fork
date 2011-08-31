#!/usr/bin/env python

"""
A module with classes and functions for generating test images, plates, and overlays for the screening version of openBIS.
"""

import canvas
import string

def generate_tile_description(tile, time = None, depth = None):
    """
    Generate a description for the combination of well, tile, channel
    and, optionaly, depth and/or time
    """
    
    desc = "s"+ str(tile)
    if depth is not None:
        desc = desc + "_z" + str(depth)
    if time is not None:
        desc = desc + "_t" + str(time)
    return desc

def generate_file_name(well, channel, desc):
    """
    Generate a name for a file using the description and channel
    """
    
    return "bPLATE_w" + well + "_" + desc + "_c" + channel + ".png"

def drawRect(canvas, r, g, b, start, isUnfilled = 0):
    canvas.draw_inset_rect(r, g, b, start, isUnfilled)

def drawText(canvas, x, y, text):
    canvas.draw_text(x, y, text)
    
def calcTile(coords):
    x,y = coords
    return (y-1)*3+x
    
class ImageGeneratorConfig:
    """
    Represents the configuration options for generating images
    """
    def __init__(self):
        self.number_of_tiles = 9
        self.image_size = 512
        self.is_split = False
        # Use an array with None for not time points / depth points
        self.time_points = [ None ]
        self.depth_points = [ None ]
        self.tile_description_generator = generate_tile_description
        self.file_name_generator = generate_file_name

class PlateGeneratorConfig(ImageGeneratorConfig):
    """
    Represents the configuration options for generating plates
    """
    def __init__(self):
        ImageGeneratorConfig.__init__(self)
        self.rows = 8
        self.cols = 12

class WellGenerator:
    """
    A class that generates raw images or overlays for a well. Used by the PlateGenerator and ImageGenerator.
    """
    
    def __init__(self, config, well, directory):
        """Constructor that takes the config and the well we are generating images for."""
        self.config = config
        self.well = well
        self.directory = directory
        
    def generate_raw_images(self):
        for tile in range(1, self.config.number_of_tiles + 1):
            for time in self.config.time_points:
                for depth in self.config.depth_points:
                    desc = self.config.tile_description_generator(tile, time, depth)
                    if self.config.is_split:
                        file_name = self.config.file_name_generator(self.well, "RED", desc)
                        self._generate_tile(file_name, desc, 1, 0, 0)
                        
                        file_name = self.config.file_name_generator(self.well, "GREEN", desc)
                        self._generate_tile(file_name, desc, 0, 1, 0)
                        
                        file_name = self.config.file_name_generator(self.well, "BLUE", desc)
                        self._generate_tile(file_name, desc, 0, 0, 1)                        
                    else:
                        file_name = self.config.file_name_generator(self.well, "RGB", desc)
                        self._generate_tile(file_name, desc, 1, 1, 1) 
        
    def _generate_tile(self, filename, tile_desc, r, g, b):
        tile_canvas = canvas.TileCanvas(self.config.image_size, self.config.image_size)
        drawRect(tile_canvas, 0, 0, 0, 0) # fill with black
        
        if self.config.is_split:
            # if split, we want to draw white instead of the specified color, since
            # the channel assignment will happen in openBIS, not here
            if r:
                drawRect(tile_canvas, 1, 1, 1, 20) # red
            if g:
                drawRect(tile_canvas, 1, 1, 1, 70) # green
            if b:
                drawRect(tile_canvas, 1, 1, 1, 120) # blue
        else:
            drawRect(tile_canvas, 1, 0, 0, 20) # red
            drawRect(tile_canvas, 0, 1, 0, 70) # green
            drawRect(tile_canvas, 0, 0, 1, 120) # blue
    
        # text annotation
        drawText(tile_canvas, 5, self.config.image_size / 2, tile_desc)
        drawText(tile_canvas, 5, 5, self.well)

        # Write the bitmap to disk in PNG format
        tile_canvas.write_png_file(self.directory + "/" + filename)

    def drawMatrix(self, coordsList, dir, channel, isOverlay):
        nonemptyTiles = set([ calcTile(coords) for coords in coordsList ])
        for tile in range(1, 10):
            imageCanvas = canvas.TileCanvas(self.config.image_size, self.config.image_size)
            if tile in nonemptyTiles:
                if not isOverlay:
                    drawRect(imageCanvas, 0, 0, 0, 0)
                    drawRect(imageCanvas, 0.5, 0.5, 0.5, 70, isUnfilled = 0)
                elif isOverlay == 1:
                    drawRect(imageCanvas, 1, 1, 1, 30, isUnfilled = 1)
                else:
                    drawText(imageCanvas, size/2, size/2, "X")
            destFile = dir + "/c"+channel + "_s" + str(tile) +".png"
            imageCanvas.write_png_file(destFile)

        
class PlateGenerator:
    """
    A class that generates raw images or overlays for a plate. Similar 
    to GenericImageGenerator, but with plates added.
    
    To use, instantiate it with a PlateGeneratorConfig that describes its properties, then
    call generate_raw_images or generate_overlays
    """
    
    def __init__(self, config):
        """Constructor that takes the configuration for the plate"""
        self.config = config
        
    def generate_raw_images(self, directory):
        for row in range(0, self.config.rows):
            for col in range(1, self.config.cols + 1):
                well = string.letters[26 + row] + str(col)
                well_generator = WellGenerator(self.config, well, directory)
                well_generator.generate_raw_images()
                
class GenericImageGenerator:
    """
    A class that generates raw images or overlays simulating images from a microscope. Similar to PlateGenerator, except without the plates.
    
    To use, instantiate it with a ImageGeneratorConfig that describes its properties, then
    call generate_raw_images or generate_overlays
    """
    
    def __init__(self, config):
        """Constructor that takes the configuration for the plate"""
        self.config = config
        
    def generate_raw_images(self, directory):
        well_generator = WellGenerator(self.config, "", directory)
        well_generator.generate_raw_images()
