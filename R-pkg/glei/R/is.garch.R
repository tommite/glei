## GARCH model parameters: p, q
## Important sampler parameters: nr
## Conditional t-student-distribution: mode, sigma, dof, data
## To use GPU or CPU for computation: useGPU
is.garch.tstudent <- function(p, q, data, nr=1E6,
                              mode=rep(0.5, (p+q)),
                              sigma=diag(p+q), dof=10,
                              useGPU=TRUE) {
  p <- as.integer(p)
  q <- as.integer(q)  
  nr <- as.integer(nr)
  dof <- as.integer(dof)

  if (p < 1) {
    stop("p has to be positive")
  }
  if (q < 1) {
    stop("q has to be positive")
  }
  if (nr < 1) {
    stop("nr has to be positive")
  }
  if (dof < 1) {
    stop("dof has to be positive")
  }

  gpu <- 1
  if (useGPU) {
    warpSize <- default.warpsize()
    if (nr %% warpSize != 0) {
      nr = ceiling(nr / warpSize) * warpSize
      message("Number of iterations not multiple of warp size (", warpSize,
              ") - rounding up to ", nr, " iterations")
    }
  } else {
    gpu = 0
  } 


  tryCatch(
           .jcall("fi/smaa/glei/r/GARCHRFacade", "[D", "importanceSample",
                  p, q, nr, dof, data, mode, as.vector(sigma),
                  as.integer(nrow(sigma)), as.integer(gpu),
                  simplify=TRUE),
           NoClassDefFoundError = function(e) {cannotInitGPU()},
           UnsatisfiedLinkError = function(e) {cannotInitGPU()}
           )
}

cannotInitGPU <- function() {
  stop("Cannot init GPU subsystem, try with useGPU=FALSE", call.=FALSE)
}
