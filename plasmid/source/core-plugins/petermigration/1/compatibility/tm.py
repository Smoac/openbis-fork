import re

def calculate_Onuc(): 
	result =68.3+ (0.41* float(len(re.findall("G|g|C|c", entity.propertyValue('SEQUENCE'))) * 100)/float(len(re.findall("A|a|C|c|G|g|T|t|R|r|Y|y|M|m|K|k|S|s|W|w|H|h|b|B|D|d|X|x|n|N", entity.propertyValue('SEQUENCE'))))) -(600/float(len(re.findall("A|a|C|c|G|g|T|t|R|r|Y|y|M|m|K|k|S|s|W|w|H|h|b|B|D|d|X|x|n|N", entity.propertyValue('SEQUENCE')))))		

	return result
     
def calculate():
	return calculate_Onuc()
