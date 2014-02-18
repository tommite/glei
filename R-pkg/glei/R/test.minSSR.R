source('option.pricing.R')

### Test minSSR ###
F0_K <- c(1.2,1.8)
Kvec <- F0_K/2
C <- F0_K*4
T <- 5
r <- 0.6
params <- list('v1' = seq(1, 1, length=1),
               'v2' = seq(2, 3, length=4)
               )
grids <- expand.grid(params)
fun.on.grids <- cbind(grids, apply(grids, 1, sum))
## print(FunInterp)
out <-  minSSR(fun.on.grids, F0_K, Kvec, C, r, T)

## TODO: Nalan check the above and make stopifnot's
