source('option.pricing.R')

set.seed(1111)
eps <- 1E-3

stopifnot(abs(f.t.k.skewed.normal(f0k=1, gamma=1, sigma=0.01, n=1E6, T=1)) < eps)
