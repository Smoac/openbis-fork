#! /usr/bin/env python

def process(tr):
    for file in tr.getIncoming().listFiles():
        openFile = None
        
        try:
            openFile = open(file.getAbsolutePath())
            content = openFile.read().strip()
            
            experiment = tr.createNewExperiment("/BSSE_FLOWCELLS/FLOWCELLS/" + content, "HT_SEQUENCING")
        finally:
            if openFile:
                openFile.close()
