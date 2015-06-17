import re

def calculate_Onuc(): 
	result = len(re.findall("A|a|C|c|G|g|T|t|U|u", entity.propertyValue('SEQUENCE')))	

	return result
     
def calculate():
	return calculate_Onuc()
