params <- list('v1' = seq(0, 1, length=4),
               'v2' = seq(0, 1, length=2),
               'v3' = seq(5, 6, length=2)
               )
g <- expand.grid(params)

max.row <- grid.search(g, sum, 1, 0.2)

stopifnot(all(max.row == c(0.2, 1.0, 6.0, 7.2)))
