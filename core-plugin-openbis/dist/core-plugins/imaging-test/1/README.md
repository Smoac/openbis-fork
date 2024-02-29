# IMAGING-TEST core plugin
This core-plugin was created to showcase a basic imaging technology. It contains a set of input
JSON files with pre-rendered images. Update and export flow consists of a Python script that generates an image
with randomly selected pixels. 


## Prerequisites
- Python >= 3.10 with [`numpy`, `pillow`] modules installed
- Imaging core-plugin installed in Openbis

## Configuration

1. Include imaging-test core plugin into core-plugins of your installation
2. Configure DSS service properties (`<DSS_FOLDER>/etc/service.properties`) to include path to python script needed for image generation. The property is `imaging.imaging-test.script-path`.
   (To use default script, configure it as: `imaging.imaging-test.script-path=<PATH_TO_CORE_PLUGINS_FOLDER>/imaging-test/1/dss/services/imaging-test/test_adaptor_script.py`)
3. Restart Openbis

## Data import



In `imaging-test/1/imaging_test_example/` you can find a python script (`importer.py`) that uploads test data into the system.

## Prerequisites

- Python >= 3.10 with [`pybis`] module installed