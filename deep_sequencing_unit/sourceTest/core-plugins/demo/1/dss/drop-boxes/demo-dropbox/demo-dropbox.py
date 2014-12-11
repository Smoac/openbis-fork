#! /usr/bin/env python

def process(tr):
    for file in tr.getIncoming().listFiles():
        openFile = None
        
        try:
            openFile = open(file.getAbsolutePath())
            content = openFile.read().strip()
            
            experiment = tr.createNewExperiment("/CISD/DEFAULT/" + content, "SIRNA_HCS")
            experiment.setPropertyValue("DESCRIPTION", "some description")
        finally:
            if openFile:
                openFile.close()
