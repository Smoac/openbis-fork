require(multicore)
require (ShortRead)
args <- commandArgs(TRUE)
fastq_file <- args[1]
qa <- qa(getwd(),args[1], type="fastq")
report_dest <- paste(fastq_file, "ShortRead_qa", sep="_")
qa_report <- report (qa, dest=report_dest , type="html")