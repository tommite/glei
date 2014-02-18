source('option.pricing.R')

stopifnot(abs(f.t.k.skewed.normal(f0k=1, gamma=1, sigma=0.01, n=1E6, T=1)) < eps)
