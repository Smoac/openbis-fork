#! /usr/bin/env python
def process(transaction):
    expid = "/MS_DATA/J_TRIPLE-TOF/2013-03"
    experiment = transaction.getExperiment(expid)

    sampid = '/MS_DATA/100422_JM_1P2_01'
    sample = transaction.getSample(sampid)

