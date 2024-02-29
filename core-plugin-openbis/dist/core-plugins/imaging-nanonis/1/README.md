# IMAGING-NANONIS core plugin
This core-plugin was created to showcase an imaging technology plugin capabilities with scientific data. It contains a set of input
.SXM and .DAT files with scripts to turn them into images. 


## Prerequisites
- Python >= 3.10
- Imaging core-plugin installed in Openbis

### Python modules configuration
This plugin has been verified to work with python 3.10.12 and modules specified in [python_requirements.txt](python_requirements.txt)

This file can be used to install packages via `pip` tool, i.e:
```bash
pip3 install -r python_requirements.txt
```


## Plugin Configuration

1. Include imaging-test core plugin into core-plugins of your installation
2. Configure DSS service properties (`<DSS_FOLDER>/etc/service.properties`) to include paths to python scripts needed for image generation. Properties are `imaging.nanonis.sxm-script-path` and `imaging.nanonis.dat-script-path`.
   ```properties
   imaging.nanonis.sxm-script-path=<PATH_TO_CORE_PLUGINS_FOLDER>/imaging-nanonis/1/dss/services/imaging-nanonis/nanonis_sxm.py
   imaging.nanonis.dat-script-path=<PATH_TO_CORE_PLUGINS_FOLDER>/imaging-nanonis/1/dss/services/imaging-nanonis/nanonis_dat.py
   ```
3. Restart Openbis

## Data import

In `imaging-nanonis/1/nanonis_example/` you can find a python script (`nanonis_importer.py`) that uploads test data into the system.

## Prerequisites

- Python >= 3.10 with [`pybis`] and other modules from [python_requirements.txt](python_requirements.txt) installed

## Using Python Virtual Environment
It is possible to configure a virtual environment for running python scripts for this plugin.
How to configure environment (may require installation of `venv` python module)
```bash
# Create venv
python3 -m venv my_venv
# Activation 
source ~/my_venv/bin/activate
# Installation of packages
pip3 install -r python_requirements.txt
```

Configuring imaging plugin to use virtual environment:
1. Modify `plugin.properties` of `imaging` core plugin and add `python3-path` property pointing to your python virtual environment (you can find `plugin.properties` here: `<CORE_PLUGINS_FOLDER/imaging/1/dss/services/imaging/plugin.properties>`)
   ```properties
   python3-path = ~/my_venv/bin/python
   ```
2. Restart Openbis

### Removal of virtual environment

To deactivate virtual environment in the terminal type in:
```bash
deactivate
```

Afterwards, virtual environment can be deleted by simply removing `my_venv` directory
