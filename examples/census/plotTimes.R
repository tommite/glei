library(glei)

## Read data
load('NY.Rdata')
y=as.vector(dem.data$lnwwage.IV)
x=as.vector(dem.data$educ.IV)
z=as.matrix(dem.data$qob.IV[,1:3])
nr <- 1e6
sizes <- ceiling(seq(1, 10) * length(z)/10)

compRes <- function(useGPU, nr, sizes) {
  t(sapply(sizes, function(x) {
    t <- system.time(
                     res <- is.iv.tstudent(y=y, x=x, z=z[1:x,], nr=nr, useGPU=useGPU)
                     )[3]
    return(c(res, t))
  }))
}

resCPU <- compRes(FALSE, nr, sizes)
resGPU <- compRes(TRUE, nr, sizes)

pdf('runtimeplot-iv-gpu_vs_cpu.pdf')
matplot(y=t(rbind(resCPU[,3], resGPU[,3])), pch=1, ylab='time(s)', x=sizes, xlab='size(data)')
legend(x='topleft', legend=c('cpu', 'gpu'), pch=1, col=c(1,2))
dev.off()
