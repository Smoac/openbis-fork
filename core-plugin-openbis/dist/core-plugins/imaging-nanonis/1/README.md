# IMAGING-NANONIS core plugin
This core-plugin was created to showcase an imaging technology plugin capabilities with scientific data. It contains a set of input
.SXM and .DAT files with scripts to turn them into images. 

## Prerequisites - Python Installation

### using Python Virtual Environment

It is possible to configure a virtual environment for running python scripts for this plugin.
How to configure environment
```bash
# Create venv
python3 -m venv ~/my_venv
# Activation 
source ~/my_venv/bin/activate
# Installation of packages
pip3 install -r scripts/python_requirements.txt
```

Configuring imaging plugin to use virtual environment:
1. Modify `plugin.properties` of `imaging` core plugin and add `python3-path` property pointing to your python virtual environment (you can find `plugin.properties` here: `<CORE_PLUGINS_FOLDER>/imaging/1/dss/services/imaging/plugin.properties`)
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

### Python modules configuration
This plugin has been verified to work with python 3.10.12 and modules specified in [python_requirements.txt](scripts/python_requirements.txt)

This file can be used to install packages via `pip` tool, i.e:
```bash
pip3 install -r scripts/python_requirements.txt
```

## Step-by-Step Installation
- Python 3.10 Virtual Environment ready
- Imaging core-plugin installed in Openbis

### Plugin Configuration

1. Include imaging-test core plugin into core-plugins of your installation
2. Configure DSS service properties (`<DSS_FOLDER>/etc/service.properties`) to include paths to python scripts needed for image generation. Properties are `imaging.nanonis.sxm-script-path` and `imaging.nanonis.dat-script-path`.
   ```properties
   imaging.nanonis.sxm-script-path=<PATH_TO_CORE_PLUGINS_FOLDER>/imaging-nanonis/1/dss/services/imaging-nanonis/nanonis_sxm.py
   imaging.nanonis.dat-script-path=<PATH_TO_CORE_PLUGINS_FOLDER>/imaging-nanonis/1/dss/services/imaging-nanonis/nanonis_dat.py
   ```
3. run `install.sh` script from `imaging-nanonis/1/scripts/` directory.
4. restart Openbis

### Data import

In `imaging-nanonis/1/scripts/` you can find `import_data.sh` script. It will upload test data to a running Openbis instance.

It accepts 2 parameters:
- Openbis url (default: http://localhost:8888/openbis)
- Path to folder with nanonis data (default: ../nanonis_example/data)


## Uninstall 
1. remove `python3-path` from `<CORE_PLUGINS_FOLDER>/imaging/1/dss/services/imaging/plugin.properties`
2. run `uninstall.sh` script from `scripts` directory
3. remove imaging-nanonis from enabled core-plugins
