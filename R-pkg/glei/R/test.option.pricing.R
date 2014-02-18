source('option.pricing.R')

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
cat("out$par should be the same upto 1 digit")
print(out$par)
