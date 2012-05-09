\name{is.iv.tstudent}
\alias{is.iv.tstudent}
\title{Instrumental variable model estimation through importance sampler with Student's t
  candidate distribution}
\description{
  Estimates an instrumental variable model using importance sampling with
  Student's t candidate distribution. The output has nr. instrumental
  variables + 4 components as:
  (beta, phi_1, phi_2, ..., phi_n, omega11, omega22, rho)
  where the phi_i correspond to the instrumental variables.
}
\usage{is.iv.tstudent(y, x, z, nr=1E6, mode=c(0, rep(1,
dim(as.matrix(z))[2]), 0.5, 0.5, 0), sigma=diag(dim(as.matrix(z))[2]+4),
dof=10, useGPU=TRUE, gpuType=1)}
\arguments{
\item{y}{dependent variable of the data.}
\item{x}{regressor of the data.}
\item{z}{instrumental variable observations.}
\item{nr}{The number of draws to use in the importance sampling.}
\item{mode}{Mode of the Student's t candidate distribution (default: [0,
  1 for each instrumental variable, 0.5, 0.5, 0])}
\item{sigma}{Sigma of the Student's t candidate distribution.}
\item{dof}{Degrees of freedom of the Student's t candidate distribution.}
\item{useGPU}{Whether to use GPU (TRUE) or CPU (FALSE) for the
  computations.}
\item{gpuType}{Used only if useGPU=TRUE. Possible values: 1 or 2. Tells
  the version of gpu to use. v1 = parallelization on MC iterations, v2 =
  parallelization on data rows.}
}
\keyword{ts}
\seealso{glei-package}
\examples{
# Set Java VM memory use to 2g not to run out of heap space
options( java.parameters = "-Xmx2g" )
library(glei)

x <- c(1, 2, 3)
y <- c(2, 3, 4)
z <- matrix(c(1, 2, 3, 4, 5, 6), nrow=3) # 2 instrumental variables
## With less iterations (use 1e6 for real analyses)
## Also, no GPU is used for this to pass on package builds
is.iv.tstudent(y=y, x=x, z=z, nr=100, useGPU=FALSE)
}