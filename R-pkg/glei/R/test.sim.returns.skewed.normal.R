source('option.pricing.R')

## TODO: Nalan fix, test not working

eps <- 1E-3
set.seed(1111)
x <- sim.returns.skewed.normal(gamma=1, sigma=0.01, n=1E6, T=1)
stopifnot(abs(mean(x)) < eps)
stopifnot(abs(var(x) - 0.1) < eps)

ppos <- gamma / (1 / gamma + gamma)
stopifnot(abs(ppos - mean(x>0)) < eps)

x <- sim.returns.skewed.normal(gamma=3, sigma=0.01, n=1E6, T=1)
ppos <- gamma / (1 / gamma + gamma)
stopifnot(abs(ppos - mean(x > 0)) < eps)
