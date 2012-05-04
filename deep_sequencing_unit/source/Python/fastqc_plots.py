#!/usr/local/bin/python3.2

import subprocess
import collections
import os
import fnmatch
import concurrent.futures

fastqcBinary ='/links/application/dsu/FastQC/fastqc'
outDir = '../fastqc'
threads = '10'
cmdLine = [fastqcBinary, '--outdir', outDir, '--quiet', '--casava', '--nogroup', '--threads', threads]

pwd = os.getcwd()
pattern = '*.fastq.gz'
maxConcurrentJobs=5

def run_me(path,files):
  '''
  Builds up the final command and runs it using the subprocess module 
  '''
  os.chdir(path)
  if (not os.path.isdir(outDir)):
    os.makedirs(outDir)

  filesToProcess = []
  for file in files:
   filesToProcess.append(os.path.join(path, file)) 
  
  p = subprocess.Popen(cmdLine + filesToProcess)
  p.wait()

def findPaths (pattern):
  '''
  Finds all files matching the given pattern and writes the result in a 
  dictionary with the path as the key and the list of files as the value 
  '''
  fileDict = {}
  for root, dirnames, filenames in os.walk(pwd):
    for filename in fnmatch.filter(filenames, pattern):
        if (root in fileDict):
          fileDict[root].append(filename)
        else:
          fileDict[root] = [filename]
  return fileDict
            
def callCommandLine():
  '''
  Calls a command line program with maxConcurrentJobs in parallel
  '''
  matchingPaths = findPaths(pattern)
  with concurrent.futures.ThreadPoolExecutor(max_workers=maxConcurrentJobs) as executor:
    out = [executor.submit(run_me, path, files)
                    for path,files in matchingPaths.items()]

callCommandLine()
