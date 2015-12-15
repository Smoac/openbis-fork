import re

def calculate_sysname(): 
	result = (len(re.findall("A|a|T|t", entity.propertyValue('SEQUENCE'))) * 100)/len(re.findall("A|a|C|c|G|g|T|t|U|u", entity.propertyValue('SEQUENCE')))	

	type=entity.propertyValue('OLIGO_TYPE')
	if type == "DNA":
		type = "d"
	else:
		type ="r"

	sense= entity.propertyValue('SENSE')
	if sense == "SENSE":
		sense = "s"
	elif sense == "ANTISENSE":
		sense ="a"

	group =  entity.propertyValue('GROUP')


	fromNuc =  entity.propertyValue('FROM_NUC')


	toNuc=  entity.propertyValue('TONUC')
	


	result = type + "-" +sense +"-" +group + "-" + "[" +fromNuc +"-"+toNuc+"]"

	return result
     
def calculate():
	return calculate_sysname()