# Taken from http://www.bioconductor.org/help/workflows/high-throughput-sequencing/

## Load packages; also loads Biostrings, IRanges, ...
library(multicore)
library(ShortRead)

args <- commandArgs(TRUE)
seq <- readFastq(args[1])

pdf(file=paste(args[1],"NumberOfOccurrences.pdf", sep="_"))

## Calculate and plot cumulative reads vs. occurrences
tbl <- tables(seq)[[2]]
xyplot(cumsum(nReads * nOccurrences) ~ nOccurrences, tbl, 
scales=list(x=list(log=TRUE)), main=args[1], type="b", pch=20,
xlab="Number of Occurrences", 
ylab="Cumulative Number of Reads")

dev.off()