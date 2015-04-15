configuration = {}
configuration["YEAST"] = ["PLASMID"]

def getSampleTypeCode(sampleAdaptor):
    return sampleAdaptor.samplePE().getSampleType().getCode()

def calculate():
    #Configuration
    sampleTypeCode = getSampleTypeCode(entity)
    typesForGenotype = configuration[sampleTypeCode]
    
    genotypeResult = ""
    sampleCodesInGenotype = {} #To avoid to add repetitions
    isFirst = True
    parentIterables = [entity.parents()]

    while len(parentIterables) > 0:
        parentIterable = parentIterables.pop(0)
        for parent in parentIterable:
            parentTypeCode = getSampleTypeCode(parent)
            if parentTypeCode in typesForGenotype:
                parentCode = parent.code()
                if parentCode not in sampleCodesInGenotype: #To avoid to add repetitions
                    #Check if is the first to add the separator or not
                    if isFirst:
                        isFirst = False
                    else:
                        genotypeResult = genotypeResult + ", "
                    #Add the code
                    genotypeResult = genotypeResult + parent.code()
                    sampleCodesInGenotype[parent.code()] = True
            else:
                parentIterables.append(parent.parents())
    return genotypeResult