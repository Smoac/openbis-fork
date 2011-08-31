#!/usr/bin/env python

"""
Generate an image series.
"""

import imagegen
import os
import shutil


def recreateDir(dir):
  if os.path.exists(dir):
    shutil.rmtree(dir)
  os.mkdir(dir)

recreateDir("SERIES")
config = imagegen.ImageGeneratorConfig()
config.time_points = range(1, 101)

# Alternative Configurations
#config.depth_points = [ 3, 6, 9 ]
#config.is_split = True

generator = imagegen.GenericImageGenerator(config)
generator.generate_raw_images("SERIES")
