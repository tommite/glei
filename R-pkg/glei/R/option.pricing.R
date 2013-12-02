grid.search <- function(g, f, param, find.val, ...) {
    g <- cbind(g, apply(g, 1, f, ...))

    lb <- max(g[g[,param] <= find.val,param])
    ub <- min(g[g[,param] >= find.val,param])

    stopifnot(lb == 0)
    stopifnot(ub == 1/3)

    mod.inds <- which(g[,param] %in% c(lb, ub))
    rows <- g[mod.inds,]
    rest.indices <- c(-param, -ncol(rows))

    ip.cases <- unique(rows[,rest.indices])

    res <- apply(ip.cases, 1, function(ip) {
        ip.rows <- rows[apply(rows[,rest.indices], 1, function(x) {all(ip == x)}),]
        approx(x=ip.rows[,param], y=ip.rows[,ncol(ip.rows)], xout=find.val)$y
    })

    new.g <- matrix(ncol=ncol(rows), nrow=nrow(ip.cases))
    new.g[,param] <- find.val
    new.g[,-param] <- cbind(as.matrix(ip.cases), res)

    max.rows <- new.g[which.max(new.g[,ncol(new.g)]),]
    if (is.matrix(max.rows) && nrow(max.rows) > 1) {
        warning('more than 1 maxima')
        max.rows <- max.rows[1,]
    }
    max.rows
}

##
## gamma, sigma: distribution params
## 
sim.returns.skewed.normal <- function(gamma, sigma, n, T) {
    if(n<1 | (n%%1!=0))
        stop("'n' should be an integer >0")
    if(gamma <= 0)
        stop("'gamma' should be positive")
    if(sigma <= 0)
        stop("'sigma' should be positive")

    sumReturns <- rep(0,n)
    ## prob(x>0)
    ppos <- gamma / (1/gamma + gamma)
    ## prob(x<=0)
    pneg <- 1 - ppos
  
    for(t in 1:T){
        ## simulate from standard normal, positive values
        simnorm <- abs(rnorm(n))
        ## simulate indicator, for positive return values
        simsign <- as.numeric(runif(n) < ppos)
 
        ## element by element multiplication
        x <- (1/sqrt(252)) * simnorm * sigma * (simsign*gamma - (1-simsign)/gamma) 
        sumReturns <- sumReturns + x
    }
    return(sumReturns)
}

## Function to simulate returns from skewed normal distribution 
## inputs:
##   n     : [integer>0] number of simulations
##   T     : [integer>0] days to maturity
## outputs:
##  mean_PayoffK : [double] mean payoff evaluations
f.t.k.skewed.normal <- function(f0k, fun, n,T){
    tmp <- sim.returns.skewed.normal(fun,n,T)
    tmp <- exp(tmp) / mean(exp(tmp))
    FtK <- tmp * f0k
    PayoffK <- (FtK > 1) * (FtK - 1) # element by element
    mean_PayoffK <- mean(PayoffK)
    return(mean_PayoffK)
}
