\name{is.garch.tstudent}
\alias{is.garch.tstudent}
\title{GARCH estimation through importance sampler with Student's t
  candidate distribution}
\description{
  Estimated a GARCH(p,q) model mean using importance sampling with
  Student's t candidate distribution
}
\usage{is.garch.tstudent(p, q, data, nr=1E6, mode=rep(0.5, (p+q)), sigma=diag(p+q), dof=10)}
\arguments{
\item{p}{p of the GARCH model (length of the alpha-vector).}
\item{q}{q of the GARCH model (length of the beta-vector).}
\item{data}{Observations vector.}
\item{nr}{The number of draws to use in the importance sampling.}
\item{mode}{Mode of the Student's t candidate distribution.}
\item{sigma}{Sigma of the Student's t candidate distribution.}
\item{dof}{Degrees of freedom of the Student's t candidate
  distribution.}
}
\keyword{ts}
\seealso{glei-package}
\examples{
# Set Java VM memory use to 2g not to run out of heap space
options( java.parameters = "-Xmx2g" )
library(glei)

data <- c(-0.3043770708483943, 0.24034250344474792, 0.6241783334951777, -0.34234584421795106, -0.6366107365716377, 0.4350807591612607, -1.0754863233481746, 0.9036812609800101, -0.6693537902009193, -0.5344470282358625, 0.05983668617294705, -0.4606904307333749, 0.11148989852925799, -0.15157270280729201, -0.07129291543315124, -0.026519766053507903, -0.8223514255485481, 0.5165787082050484, -0.20259278588746416, -0.09022964157838832, 0.4994975146659299, -0.08167085820840632, 0.3105172802904407, -0.13330829118220602, -0.07310553050961166, -0.7010911102802643, 1.5421987148604832, 1.1675551150229249, -0.7069395623281259, 1.574414527508866, 1.5350980171148434, 1.149590217584825, 0.2862297995859983, -0.6612992714949582, 1.3406307974600031, -1.4439935918083993, 0.3057526005821013, -0.6940497365603073, -1.0148508534970901, 1.9418082225338653, 1.308790448714808, -2.526881845855509, 2.293440211381766, -0.6865596655120385, 0.2908081908875546, -0.7225159966530642, -0.914166251243136, -1.642162939017988, -1.6786036116203504, -1.696644409951306)

## garch(1,1)-model with default parameters
## except less iterations (as this is just an example, use 1E6 for real analyses)
is.garch.tstudent(1, 1, data, nr=100)
}