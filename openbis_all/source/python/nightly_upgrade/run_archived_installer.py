#!/usr/bin/env python

"""Run a .tar.gz installer.

Unpack the installer, run it, and then clean up.

"""

import subprocess
import json
import os.path
import glob
import shutil

def setup_and_preflight():
  """Define global variables and check that the environment is correct"""
  global dest_dir
  global installer, installer_folder
  global config_file_path
  dest_dir = os.path.expanduser("~")
  installers = glob.glob(os.path.join(dest_dir, "openBIS-installation-standard-technologies-*-*.tar.gz"))
  if len(installers) < 1:
    print("No installer of the form 'openBIS-installation-standard-technologies-*-*.tar.gz' found in " + dest_dir)
    exit(-1)
  
  if len(installers) > 1:
    print("Multiple installers found : " + " ".join(installers))
    exit(-1)
  
  installer = installers[0]
  installer_folder = installer[0:-7]
  
  config_file_path = os.path.join(os.path.dirname(__file__), "console.properties")
  if not os.path.exists(config_file_path):
    print("Could not find installer config file at : " + config_file_path)
    exit(-1)

def run_cmd(cmd):
  print(" ".join(cmd))
  print("\n")
  return subprocess.check_output(cmd)

def unpack_installer():
  unpack_cmd = ["tar", "-zx", "-f", installer, "-C", dest_dir]
  run_cmd(unpack_cmd)
  
def run_installer():
  # Copy the canonical config file to the installer folder
  shutil.copy(config_file_path, installer_folder)
  install_cmd = [os.path.join(installer_folder, "run-console.sh")]
  run_cmd(install_cmd)

  
def cleanup():
  if not os.path.exists(installer_folder):
    return
  
  rm_cmd = ["rm", "-rf", installer_folder]
  run_cmd(rm_cmd)



# # # # # # # # # # # # # # # # # # # # # # # # #
# The Script
setup_and_preflight()
unpack_installer()
run_installer()
cleanup()
