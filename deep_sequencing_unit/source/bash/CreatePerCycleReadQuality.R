library(ShortRead)
args <- commandArgs(TRUE)
reads <- readFastq(args[1])
if (length(reads) > 1000000) {
	reads <- sample(reads,1000000) # if more than a million reads, sample randomly
}
qual <- FastqQuality(quality(quality(reads))) # get quality scores
readM <- as(qual, "matrix") # convert scores to matrix
pdf(file=paste(args[1],"boxplot.pdf", sep="_")) # Save box plot as boxplot.pdf in current folder
boxplot(readM, outline = FALSE, main="Per Cycle Read Quality", sub=args[1], xlab="Cycle", ylab="Phred Quality", col="grey")
dev.off()