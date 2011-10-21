#!/links/application/dsu/Python-3.2/python

'''
Docu
'''

import subprocess
import shlex
import os
import fnmatch
import concurrent.futures

rscriptPath = '/links/application/dsu/R-scripts'
pwd = os.getcwd()
pattern = '*.fastq.gz'
rscript = '/links/application/dsu/R-2.13.2/bin/Rscript'
concatenationScript='/links/application/dsu/bin/concatenate_pdfs.py'
maxConcurrentJobs=5

def run_me(fastqFile):
  (path, file) = os.path.split(fastqFile)
  os.chdir(path)
  args =  rscript + ' --vanilla ' + rscriptPath + '/' + 'fastq_quality.R ' + file
  #print(args)
  SplitArgs = shlex.split(args)
  p = subprocess.Popen(SplitArgs)
  p.wait()
  #subprocess.Popen(concatenationScript)

def findFiles (pattern):
  matches = []
  for root, dirnames, filenames in os.walk(pwd):
    for filename in fnmatch.filter(filenames, pattern):
        matches.append(os.path.join(root, filename))
  return matches
            
def callR():
  matchingFiles = findFiles(pattern)
  with concurrent.futures.ThreadPoolExecutor(max_workers=maxConcurrentJobs) as executor:
    out = [executor.submit(run_me, lane)
                    for lane in matchingFiles]
callR()
