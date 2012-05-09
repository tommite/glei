## IV model parameters: y, x, z
## Important sampler parameters: nr
## Conditional t-student-distribution: mode, sigma, dof, data
## To use GPU or CPU for computation: useGPU
## If useGPU = true, gpuType tells which type of GPU to use (v1 or v2)
is.iv.tstudent <- function(y, x, z, nr=1E6,
                           mode=c(0, rep(1, dim(as.matrix(z))[2]), 0.5, 0.5, 0),
                           sigma=diag(dim(as.matrix(z))[2]+4), dof=10,
                           useGPU=TRUE, gpuType=1) {
  nr <- as.integer(nr)
  dof <- as.integer(dof)
  x <- as.vector(x)
  y <- as.vector(y)
  z <- as.matrix(z)

  if (nr < 1) {
    stop("nr has to be positive")
  }
  if (dof < 1) {
    stop("dof has to be positive")
  }

  xdim <- length(x)
  ydim <- length(y)
  zdim <- dim(z)

  if (xdim != ydim) {
    stop("x dimension must match y dimension")
  }
  if (zdim[1] != ydim) {
    stop("z nr. rows must match y dimension")
  }

  gpu <- gpuType
  if (useGPU) {
    if (gpuType == 1) {
      warpSize <- default.warpsize()
      if (nr %% warpSize != 0) {
        nr = as.integer(ceiling(nr / warpSize) * warpSize)
        message("Number of iterations not multiple of warp size (", warpSize,
                ") - rounding up to ", nr, " iterations")
      }
    } 
  } else {
    gpu = 0
  } 


  tryCatch(
           .jcall("fi/smaa/glei/r/ImportanceSamplerRFacade", 
                  "[D", "IVimportanceSample",
                  as.vector(y), as.vector(x),
                  as.vector(z), as.integer(nrow(z)),
                  nr, dof, data, mode, as.vector(sigma),
                  as.integer(nrow(sigma)), as.integer(gpu),
                  simplify=TRUE),
           NoClassDefFoundError = function(e) {cannotInitGPU()},
           UnsatisfiedLinkError = function(e) {cannotInitGPU()}
           )
}

cannotInitGPU <- function() {
  stop("Cannot init GPU subsystem, try with useGPU=FALSE", call.=FALSE)
}
