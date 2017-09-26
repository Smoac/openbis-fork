import glob
import os
from datetime import datetime
from ch.systemsx.cisd.openbis.dss.generic.shared.utils import ExcelFileReader
from ch.systemsx.cisd.openbis.generic.shared.basic.dto import DataSetKind

def process(transaction):
    print "Starting..."
    spaceCode = "SYNC"
    p1 = transaction.createNewProject("/%s/P1" % spaceCode)
    exp1 = transaction.createNewExperiment(p1.getProjectIdentifier() + "/E1", "UNKNOWN")
    container_smp = transaction.createNewSample("/%s/S1" % spaceCode, "UNKNOWN")
    container_smp.setExperiment(exp1)
    component_samp = transaction.createNewSample("/%s/S2" % spaceCode, "UNKNOWN")
    component_samp.setContainer(container_smp)
    component_samp.setExperiment(exp1)
     
    comp_ds = createDsWithPermId(transaction, component_samp, "COMPONENT_DS1", "UNKNOWN")
    cont_ds = registerContainerDS(transaction, component_samp.getExperiment(), comp_ds.getDataSetCode())
 
    '''A new sample of the type added by the master data initialization script'''
    md_test_smp = transaction.createNewSample("/%s/S3" % spaceCode, "MD_TEST")
    md_test_smp.setPropertyValue("GENDER", "MALE")

    #===========================================================================
    # sample = transaction.getSampleForUpdate("/DS1_SRC/S8")
    # sample.setPropertyValue("resolution", "")
    #===========================================================================

    #===========================================================================
    # exp2 = transaction.createNewExperiment(p1.getProjectIdentifier() + "/E2", "UNKNOWN")
    # parent_smp = transaction.createNewSample("/TEST/S2", "UNKNOWN")
    # parent_smp.setExperiment(exp2)
    # parent_ds = createDsWithPermId(transaction, exp2, "PARENT_DS1", "UNKNOWN")
    # comp_ds.setParentDatasets([parent_ds.getCode()])
    # component_smp.setParentSampleIdentifiers(parent_smp.getSampleIdentifier())
    #===========================================================================

def createDsWithPermId(transaction, sample, code, ds_type):
    ds = transaction.createNewDataSet(ds_type, code)
    ds.setSample(sample)
    transaction.moveFile(transaction.getIncoming().getPath(), ds)
    return ds

def registerContainerDS(transaction, experiment, comp_ds_code):
    container = transaction.createNewDataSet("HCS_IMAGE_CONTAINER_RAW", "CONTAINER_DS1", DataSetKind.CONTAINER)
    container.setExperiment(experiment)
    container.setContainedDataSetCodes([comp_ds_code])

    #raise Exception('stop')

