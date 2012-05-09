library(glei)

## Read data
load('NY.Rdata')
y=as.vector(dem.data$lnwwage.IV)
x=as.vector(dem.data$educ.IV)
z=as.matrix(dem.data$qob.IV[,1:3])
nr <- 1e6
sizes <- ceiling(seq(1, 10) * length(x)/10)

compRes <- function(useGPU, nr, sizes) {
  t(sapply(sizes, function(len) {
    t <- system.time(
                     res <- is.iv.tstudent(y=y[1:len], x=x[1:len], z=z[1:len,], nr=nr, useGPU=useGPU)
                     )[3]
    return(c(t, res))
  }))
}

resCPU <- compRes(FALSE, nr, sizes)
resGPU <- compRes(TRUE, nr, sizes)

pdf('runtimeplot-iv-gpu_vs_cpu.pdf')
matplot(y=t(rbind(resCPU[,1], resGPU[,1])), pch=1, ylab='time(s)', x=sizes, xlab='size(data)')
legend(x='topleft', legend=c('cpu', 'gpu'), pch=1, col=c(1,2))
dev.off()
