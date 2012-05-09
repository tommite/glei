library(glei)

## Read data
load('NY.Rdata')
y=as.vector(dem.data$lnwwage.IV)
x=as.vector(dem.data$educ.IV)
z=as.matrix(dem.data$qob.IV[,1:3])
nr <- 1e6
sizes <- ceiling(seq(1, 10) * length(x)/10)

compRes <- function(gputype, nr, sizes) {
  t(sapply(sizes, function(len) {
    t <- system.time(
                     res <- is.iv.tstudent(y=y[1:len], x=x[1:len], z=z[1:len,], nr=nr, useGPU=gputype>0, gpuType=gputype)
                     )[3]
    return(c(t, res))
  }))
}

resCPU <- compRes(0, nr, sizes)
resGPU1 <- compRes(1, nr, sizes)
resGPU2 <- compRes(2, nr, sizes)

pdf('runtimeplot-iv-gpu_vs_cpu.pdf')
matplot(y=t(rbind(resCPU[,1], resGPU1[,1], resGPU2[,1])), pch=1, ylab='time(s)', x=sizes, xlab='size(data)')
legend(x='topleft', legend=c('cpu', 'gpuv1', 'gpuv2'), pch=1, col=c(1,2,3))
dev.off()
