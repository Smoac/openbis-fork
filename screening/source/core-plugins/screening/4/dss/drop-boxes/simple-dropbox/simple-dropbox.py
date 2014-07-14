#! /usr/bin/env python
def process(transaction):
    expid = "/DEFAULT/DEFAULT/DEFAULT"
    exp = transaction.getExperiment(expid)
     
    if None == exp:
        exp = transaction.createNewExperiment(expid, "TEST-EXPERIMENT")
   
    container = transaction.getDataSetForUpdate("20140627153354751-4102")

    contained = transaction.createNewDataSet()
    contained.setDataSetType("UNKNOWN")
    contained.setExperiment(exp)

    contained2 = transaction.createNewDataSet()
    contained2.setDataSetType("UNKNOWN")
    contained2.setExperiment(exp)

    containedList = list(container.getContainedDataSetCodes())
    containedList = [ contained.getDataSetCode() ] + containedList + [ contained2.getDataSetCode() ]

    container.setContainedDataSetCodes(containedList)

    print 'Contained data sets: ' + str(containedList)

    transaction.createNewFile(contained, "contained")
    transaction.createNewFile(contained2, "contained2")

