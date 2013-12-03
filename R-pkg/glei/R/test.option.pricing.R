### Test grid.search ###
params <- list('v1' = seq(0, 1, length=4),
               'v2' = seq(0, 1, length=2),
               'v3' = seq(5, 6, length=2)
               )
g <- expand.grid(params)
max.row <- grid.search(g, sum, 1, 0.2)
stopifnot(all(max.row == c(0.2, 1.0, 6.0, 7.2)))

### Test sim.returns.skewed.normal ### TODO: Nalan fix, test not working
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

### Test f.t.k.skewed.normal ###
stopifnot(abs(f.t.k.skewed.normal(f0k=1, gamma=1, sigma=0.01, n=1E6, T=1)) < eps)

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


### Test option.pricing ###
data <- c(1300,43.70,                 
          1320,28.80,                        
          1325,25.50,
          1330,22.40,
          1340,16.70,
          1345,14.30,
          1350,12.10,
          1355,10.10,
          1360, 8.40,
          1365, 6.90,
          1370, 5.60,
          1375, 4.50,
          1380, 3.60,
          1390, 2.40,
          1395, 1.90,
          1400, 1.50,
          1405, 1.20,
          1410, 0.95,
          1415, 0.75,
          1420, 0.60,
          1425, 0.50,
          1430, 0.45,
          1435, 0.30,
          1440, 0.25,
          1445, 0.20,
          1450, 0.20,
          1460, 0.15,
          1470, 0.15,
          1475, 0.10)

data <- matrix(data,29,2,byrow=TRUE)
data <- data[seq(nrow(data),1,-1),]
f0 <- 1337.70
f0k <- f0 / data[,1]
Kvec <- data[,1]
C <- data[,2]
T <- 1
r <- 0.067

n = 1E3
params <- list('v1' = seq(0.8, 1.2, length=11),
               'v2' = seq(0.4, 1.2, length=11),
               'v3' = seq(0.1, 0.4, length=11)
               )  # FIXME sizes are actually c(40,16,15)
grids <- as.matrix(expand.grid(params))

out <- option.pricing(grids, n, f0k, Kvec, C, r, T)
cat("output$par should be the same upto 1 digit")
print(out$par)
