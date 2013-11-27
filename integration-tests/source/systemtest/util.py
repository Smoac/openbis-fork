def readProperties(propertiesFile):
    """
    Reads a Java properties file and returns the key-value pairs as a dictionary.
    """
    with open(propertiesFile, "r") as f:
        result = {}
        for line in f.readlines():
            trimmedLine = line.lstrip().rstrip()
            if len(trimmedLine) > 0 and not trimmedLine.startswith('#'):
                splittedLine = line.split('=', 1)
                key = splittedLine[0].lstrip().rstrip()
                value = splittedLine[1].lstrip().rstrip()
                result[key] = value
        return result
    
def writeProperties(propertiesFile, dictionary):
    """
    Saves the specified dictionary as a Java dictionary file.
    """
    with open(propertiesFile, "w") as f:
        for key, value in dictionary.iteritems():
            f.write("%s=%s\n" % (key, value))
