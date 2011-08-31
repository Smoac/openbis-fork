#!/usr/bin/env python

"""
Generate a plate.
"""

import imagegen
import os
import shutil

def overlayTests(sampleCode):
  rootDir = "targets/generated-images"
  recreateDir(rootDir)
  
  dir = rootDir + "/" + sampleCode+".basic"
  recreateDir(dir)
  drawMatrix(((1,1), (1,2), (1,3), (3,1)), dir, "NUCLEUS",  0);
  drawMatrix(((1,2), (2,2), (2,3), (3,1)), dir, "CELL",   0);
  drawMatrix(((1,3), (2,3), (3,3), (3,1)), dir, "MITOCHONDRION",  0);
  
  dir = rootDir + "/" + sampleCode+".overlay-surround"
  recreateDir(dir)
  drawMatrix(((1,1), (1,2), (1,3), (3,1)), dir, "NUCLEUS",  1);
  drawMatrix(((1,2), (2,2), (2,3), (3,1)), dir, "CELL",     1);
  drawMatrix(((1,3), (2,3), (3,3), (3,1)), dir, "MITOCHONDRION",  1);

  dir = rootDir + "/" + sampleCode+".overlay-text"
  recreateDir(dir)
  drawMatrix(((1,1), (1,2), (1,3), (3,1)), dir, "NUCLEUS-TEXT",   2);
  drawMatrix(((1,2), (2,2), (2,3), (3,1)), dir, "CELL-TEXT",    2);
  drawMatrix(((1,3), (2,3), (3,3), (3,1)), dir, "MITOCHONDRION-TEXT",  2);


def recreateDir(dir):
  if os.path.exists(dir):
    shutil.rmtree(dir)
  os.mkdir(dir)

recreateDir("PLATONIC")
config = imagegen.PlateGeneratorConfig()

# Alternative Configurations
#config.time_points = [ 5, 10, 15 ]
#config.depth_points = [ 3, 6, 9 ]
#config.is_split = True

generator = imagegen.PlateGenerator(config)
generator.generate_raw_images("PLATONIC")
