library(glei)

## Read data
data <- read.csv(gzfile("dataSP500_1950_2012.csv.gz"))
T <- nrow(data) # number of observations
close <- data[,7] # calculate log-returns from closing prices
lnret <- 100 * (log(close[2:T]) - log(close[1:(T-1)])) # log-percentage 

nr <- 1e6
sizes <- ceiling(seq(1, 10) * length(lnret)/10)

compRes <- function(useGPU, nr, sizes) {
  t(sapply(sizes, function(x) {
    t <- system.time(
                     res <- is.garch.tstudent(data=lnret[1:x], nr=nr, useGPU=useGPU)
                     )[3]
    return(c(res, t))
  }
                   ))
}

resCPU <- compRes(FALSE, nr, sizes)
resGPU <- compRes(TRUE, nr, sizes)

pdf('runtimeplot-garch-gpu_vs_cpu.pdf')
matplot(y=t(rbind(resCPU[,3], resGPU[,3])), pch=1, ylab='time(s)', x=sizes, xlab='size(data)')
legend(x='topleft', legend=c('cpu', 'gpu'), pch=1, col=c(1,2))
dev.off()
