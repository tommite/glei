### Test grid.search ###
params <- list('v1' = seq(0, 1, length=4),
               'v2' = seq(0, 1, length=2),
               'v3' = seq(5, 6, length=2)
               )
g <- expand.grid(params)
max.row <- grid.search(g, sum, 1, 0.2)
stopifnot(all(max.row == c(0.2, 1.0, 6.0, 7.2)))

### Test sim.returns.skewed.normal ###
eps <- 1E-5
set.seed(1111)
x <- sim.returns.skewed.normal(gamma=1, sigma=0.01, n=1E6, T=1)
stopifnot(abs(mean(x)) < eps)
stopifnot(abs(var(x) - 0.1) < eps)

ppos <- gamma / (1 / gamma + gamma)
stopifnot(abs(ppos - mean(x>0)) < eps)

x <- sim.returns.skewed.normal(gamma=3, sigma=0.01, n=1E6, T=1)
ppos <- gamma / (1 / gamma + gamma)
stopifnot(abs(ppos - mean(x>0)) < eps)



