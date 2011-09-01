import imagegen
import os
import shutil


def recreateDir(dir):
  if os.path.exists(dir):
    shutil.rmtree(dir)
  os.mkdir(dir)

recreateDir("PLATONIC-OVERLAYS")
config = imagegen.PlateGeneratorConfig()

# Alternative Configurations
#config.time_points = [ 5, 10, 15 ]
#config.depth_points = [ 3, 6, 9 ]
#config.is_split = True

generator = imagegen.PlateGenerator(config)
generator.generate_overlay_images("PLATONIC-OVERLAYS", "OVERLAY", 10, 10)