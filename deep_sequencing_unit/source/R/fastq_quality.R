# Needs ShortRead_1.11.42 or higher
#
# @author Manuel Kohler
# ETHZ Zurich, 2011

require(ShortRead)
require(RColorBrewer)
#require(RSvgDevice)

args <- commandArgs(TRUE)

loadFile <- function(args) {
  file <- args[1]
}

splitPathAndFileName <- function(fullPath) {
  # Splits a full path into path and file name and returns it as a list

  fastqFileOnly <- basename(fullPath)
  pathOnly <- dirname(fullPath)
  return (c(pathOnly, fastqFileOnly)) 
}


subSampledReads <- function(fastqFile, samples=1e7, blocksize=2e9) {
  # needed ShortRead_1.11.42
  fastqFile
  fqSub <- FastqSampler(fastqFile, n=samples, readerBlockSize=blocksize, verbose=FALSE)
  subReads <- yield(fqSub)
  return (subReads)
}

boxPlotPerCycle <- function() {
  # Boxplot for per cycle Phred quality
  qual <- FastqQuality(quality(quality(subReads))) # get quality scores
  readM <- as(qual, "matrix") # convert scores to matrix
  boxplot(readM, outline = FALSE, main="Per Cycle Read Quality", sub=fastq_only,
          xlab="Cycle", ylab="Phred Quality", col=brewer.pal(11, "Spectral")[1])
  return (boxplot)
}

nucleotidesPerCyclePlot <- function() {
  # Nucleotide distribution per cycle
  a <-alphabetByCycle(sread(subReads))
  cycles <- dim(a)[2]
  total_number_bases_first_cycle <- a[,1][[1]]+ a[,1][[2]]+a[,1][[3]]+ a[,1][[4]]
                                + a[,1][[5]]
  n <- c(names(a[1,1]),names(a[2,1]), names(a[3,1]), names(a[4,1]),
         names(a[15,1]))
  par(xpd=T, mar=par()$mar+c(0,0,0,4))
  barplot(a, main="Numbers of nucleotides per cycle", sub=fastq_only,
         ylab="Absolute number",  col=heat.colors(5), names.arg=c(1:cycles))
  legend(cycles+10, total_number_bases_first_cycle, n, fill=heat.colors(5))
  return (barplot)
}

cumOccurencesPlot <- function() {
  # Taken from
  # http://www.bioconductor.org/help/workflows/high-throughput-sequencing/
  # Calculate and plot cumulative reads vs. occurrences

  #seq <- readFastq(subReads)
  tbl <- tables(subReads)[[2]]
  xy <- xyplot(cumsum(nReads * nOccurrences) ~ nOccurrences, tbl,
           scales=list(x=list(log=TRUE)), main=fastq_only, type="b", pch=20,
           xlab="Number of Occurrences",
           ylab="Cumulative Number of Reads")
  return(xy)
}


plotPdf <- function (plotObject) {
  pdf(file=paste(fastq_file, "quality.pdf", sep="_"))
  plotObject
  dev.off() 
}

plotSvg <- function (plotObject) {
  devSVG(file = paste(fastq_file,"quality.svg", sep="_"), width = 10,
       height = 8, bg = "white", fg = "black", onefile=TRUE, xmlHeader=TRUE)
  plotObject
  dev.off()
}

# MAIN ########################################################################

fastq_file <- loadFile(args)
subReads <- subSampledReads(fastq_file)

fastqFilePathVector <- splitPathAndFileName(fastq_file)
fastq_only <- fastqFilePathVector[2]

pdf(file=paste(fastq_file,"quality.pdf", sep="_"), paper="a4")
 box <- boxPlotPerCycle()
 nuc <- nucleotidesPerCyclePlot()
 cumOccurencesPlot()
dev.off()

#plotPdf(box)
#plotPdf(nuc)
#plotPdf(cumOccurencesPlot())
