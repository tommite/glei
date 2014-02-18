source('option.pricing.R')

### Test grid.search ###
params <- list('v1' = seq(0, 1, length=4),
               'v2' = seq(0, 1, length=2)
               )
g <- expand.grid(params)
g <- cbind(g, apply(g, 1, function(x) {x[1] * 2 + x[2]}))
colnames(g)[3] <- 'v3'
stopifnot(all(grid.search(g, 1, 0.2) ==
              matrix(c(0.2, 0, 0.4, 0.2, 1, 1.4), byrow=T, ncol=3)))
stopifnot(all(grid.search(g, 1, 0.0) ==
              matrix(c(0, 0, 0, 0, 1, 1), byrow=T, ncol=3)))

params2 <- c(list('v0' = 0.2:3.2), params)
g2 <- expand.grid(params2)
g2 <- cbind(g2, apply(g2, 1, function(x) {x[1] + x[2]^2 + (3*x[3])^3}))
colnames(g2)[4] <- 'v3'
max.rows2 <- grid.search(g2, 1, 2.2) ## TODO: add stopifnot

g2[sample(1:nrow(g2)),]
