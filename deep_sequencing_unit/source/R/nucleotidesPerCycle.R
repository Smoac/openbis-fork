library(multicore)
library(ShortRead)
args <- commandArgs(TRUE)
reads <- readFastq(args[1])

a <-alphabetByCycle(sread(reads))
cycles <- dim(a)[2]
total_number_bases_first_cycle <- a[,1][[1]]+ a[,1][[2]]+a[,1][[3]]+ a[,1][[4]]+ a[,1][[5]]
n <- c(names(a[1,1]),names(a[2,1]), names(a[3,1]), names(a[4,1]), names(a[15,1]))
pdf(file=paste(args[1],"nuc_per_cycle.pdf", sep="_")) # Save box plot as boxplot.pdf in current folder
par(xpd=T, mar=par()$mar+c(0,0,0,4))
barplot(a, main="Numbers of nucleotides per cycle", sub=args[1], ylab="Absolute number",  col=heat.colors(5), names.arg=c(1:cycles))
legend(cycles+10, total_number_bases_first_cycle, n, fill=heat.colors(5))
dev.off()