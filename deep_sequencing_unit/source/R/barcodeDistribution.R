library("ShortRead")

# Collect arguments
args <- commandArgs(TRUE)
print(args)
path <- (args[1])
path <- "/Users/kohleman/PycharmProjects/qgf/barcodeDistribution/testData"  
filenames <- list.files(path, pattern="lane6", full.names=TRUE)
totalIndexList <- ""

plotTopIndices <- function (topIndices, xName="") {
  
  # Creates a barplot out of a data frame.
  #
  # Args:
  #   topIndices: data.frame which must have the columns 
  #   'totalIndexList' and 'Freq'
  #   xName: Name for the x-axes (xlab)
  #
  # Returns:
  #   an image/ a pdf
    
  par(mfcol=c(1, 1))
  countLength<- nchar(toString(max(topIndices$Freq)))
  
  par(las=2) 
  par(mar=c(5,8,4,2)) # increase y-axis margin.
  
  b <- barplot(topIndices$Freq, main="Top 15 indices distribution", xlab=paste("Count of Indices for", xName), horiz=TRUE,
               names=topIndices$totalIndexList, cex.names=.75, las=1)
  text(cex=.75, x=topIndices$Freq-(countLength * 2), y=b, labels=topIndices$Freq, xpd=TRUE)
}

# -----------------------------------------------------------------------------

multmerge <- function(datalist, mergeBy){
  # Merges a list if data frames by the 'mergeBy' parameter
  Reduce(function(...) {merge(..., by = mergeBy, all=TRUE)}, datalist)
}

# -----------------------------------------------------------------------------

streamFASTQ <- function (file, verbose = TRUE) {

  fileBaseName <- basename(file)
  strm <- FastqStreamer(file, readerBlockSize=1e7)
  if (verbose) {
    print (paste("Opening " ,fileBaseName))
  }
  s <- unlist(strsplit(fileBaseName, "_"))
    
  repeat {
    fq <- yield(strm)
    if (length(fq) == 0)
      break
    
    #print (fq)
    #print(sread(fq))
    #print(quality(fq))
    
    header <- id(fq)
    a <- (as.character(header))
    split <- (strsplit(a, ":"))
    indexList <- lapply(split, function(x) strsplit(x, ":")[10])
    indices <- unlist(indexList)
    
    totalIndexList <- c(totalIndexList, indices)
  }

  close(strm)

  counts <- as.data.frame(table(totalIndexList)) 
  sortedCounts <- counts[order(- counts$Freq), ]
  
  return (sortedCounts)
}

ldf <- lapply(filenames, streamFASTQ)

# if there are more than one FASTQ file we do some more stuff
if (length(ldf) > 1) {
  list1 <- multmerge(ldf, "totalIndexList")
  # Replacing all NAs with 0
  list1 [is.na(list1)] <- 0
  # sum up the values row-wise for each column, but leave out the first and the
  # last column 
  list1$Freq<- apply(list1[,c(-1,-length(list1))],1,sum)
} else {
  print ("Only one FASTQ found")
  list1 <- ldf[[1]]
}

ttt1 <- list1[c("totalIndexList","Freq")]
orderedDf <- ttt1[with(ttt1, order(- Freq)), ]
topIndices <- orderedDf[1:15, ]
#print (topIndices)

fileBaseName <- (basename(filenames[1]))
plotTopIndices (topIndices, fileBaseName)

#write.table(list1, file = "list1.csv", sep = ",", col.names = NA, qmethod = "double")