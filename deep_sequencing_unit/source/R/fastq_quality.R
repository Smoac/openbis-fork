require(multicore)
require(ShortRead)
require(RColorBrewer)
require(RSvgDevice)

# ShortRead Quality assessment ################################################
args <- commandArgs(TRUE)
fastq_file <- args[1]

# Ugly but only way to simulate a rsplit
p=""
tokens<-unlist(strsplit(fastq_file, "/"))
path_tokens<-tokens[2:(length(tokens)-1)]
fastq_only<-tokens[length(tokens)]
for (x in 1:length(path_tokens)) p<-paste(p,path_tokens[x],sep="/")

qa <- qa(p,fastq_only, type="fastq")
qa_report <- report (qa, dest=paste(fastq_file, "ShortRead_qa", sep="_"),
type="html")

# Boxplot for per cycle Phred quality
reads <- readFastq(fastq_file)
# if more than a million reads, sample randomly
if (length(reads) > 10000000) {
	reads <- sample(reads,10000000)
}
qual <- FastqQuality(quality(quality(reads))) # get quality scores
readM <- as(qual, "matrix") # convert scores to matrix

devSVG(file = paste(fastq_file,"boxplot.svg", sep="_"), width = 10,
height = 8, bg = "white", fg = "black", onefile=TRUE, xmlHeader=TRUE)
boxplot(readM, outline = FALSE, main="Per Cycle Read Quality", sub=fastq_file,
xlab="Cycle", ylab="Phred Quality", col=brewer.pal(11, "Spectral")[1])
dev.off()

# Save box plot as boxplot.pdf in current folder
pdf(file=paste(fastq_file,"boxplot.pdf", sep="_"))
boxplot(readM, outline = FALSE, main="Per Cycle Read Quality", sub=fastq_file,
xlab="Cycle", ylab="Phred Quality", col=brewer.pal(11, "Spectral")[1])
dev.off()

# Nucleotides per Cycle  ######################################################
a <-alphabetByCycle(sread(reads))
cycles <- dim(a)[2]
total_number_bases_first_cycle <- a[,1][[1]]+ a[,1][[2]]+a[,1][[3]]+ a[,1][[4]]
+ a[,1][[5]]
n <- c(names(a[1,1]),names(a[2,1]), names(a[3,1]), names(a[4,1]),
names(a[15,1]))

# Save box plot in current folder
pdf(file=paste(fastq_only,"nuc_per_cycle.pdf", sep="_"))
par(xpd=T, mar=par()$mar+c(0,0,0,4))
barplot(a, main="Numbers of nucleotides per cycle", sub=fastq_file,
ylab="Absolute number",  col=heat.colors(5), names.arg=c(1:cycles))
legend(cycles+10, total_number_bases_first_cycle, n, fill=heat.colors(5))
dev.off()

devSVG(file = paste(fastq_only,"nuc_per_cycle.svg", sep="_"), width = 10,
height = 8, bg = "white", fg = "black", onefile=TRUE, xmlHeader=TRUE)
par(xpd=T, mar=par()$mar+c(0,0,0,4))
barplot(a, main="Numbers of nucleotides per cycle", sub=fastq_file,
ylab="Absolute number",  col=heat.colors(5), names.arg=c(1:cycles))
legend(cycles+10, total_number_bases_first_cycle, n, fill=heat.colors(5))
dev.off()


# Taken from
# http://www.bioconductor.org/help/workflows/high-throughput-sequencing/

seq <- readFastq(fastq_file)
tbl <- tables(seq)[[2]]

pdf(file=paste(fastq_only,"NumberOfOccurrences.pdf", sep="_"))

## Calculate and plot cumulative reads vs. occurrences
xyplot(cumsum(nReads * nOccurrences) ~ nOccurrences, tbl, 
scales=list(x=list(log=TRUE)), main=fastq_only, type="b", pch=20,
xlab="Number of Occurrences", 
ylab="Cumulative Number of Reads")
dev.off()

devSVG(file = paste(fastq_only,"NumberOfOccurrences.svg", sep="_"), width = 10,
height = 8, bg = "white", fg = "black", onefile=TRUE, xmlHeader=TRUE)
xyplot(cumsum(nReads * nOccurrences) ~ nOccurrences, tbl,
scales=list(x=list(log=TRUE)), main=fastq_only, type="b", pch=20,
xlab="Number of Occurrences",
ylab="Cumulative Number of Reads")
dev.off()