NR.WORKING.DAYS.PER.YEAR <- 252 

###
## Performs a grid search for g on column index param, to produce
## a new grid where: g[,param] == find.val and last column of g is interpolated
## (linearly).
##
## PRE-COND: find.val >= min(g[,param]) && find.val <= max(g[,param])
## g: grid to search on (each row is one grid point, last column point value)
## param: index of the column to search on
## find.val: the value to search for
grid.search <- function(g, param, find.val) {
    stopifnot(find.val >= min(g[,param]) && find.val <= max(g[,param]))
    lb <- max(g[g[,param] <= find.val,param])
    ub <- min(g[g[,param] >= find.val,param])

    if (lb == ub) {
        return(g[g[,param] == lb,])
    }

    mod.inds <- which(g[,param] %in% c(lb, ub))
    rows <- g[mod.inds,]
    rest.indices <- c(-param, -ncol(rows))

    ip.cases <- as.matrix(unique(rows[,rest.indices]))

    res <- apply(ip.cases, 1, function(ip) {
        ip.rows <- rows[apply(as.matrix(rows[,rest.indices]), 1, function(x) {all(ip == x)}),]
        approx(x=ip.rows[,param], y=ip.rows[,ncol(ip.rows)], xout=find.val)$y
    })

    new.g <- matrix(ncol=ncol(rows), nrow=nrow(ip.cases))
    new.g[,param] <- find.val
    new.g[,-param] <- cbind(as.matrix(ip.cases), res)

    ## max.rows <- new.g[which.max(new.g[,ncol(new.g)]),]
    ## if (is.matrix(max.rows) && nrow(max.rows) > 1) {
    ##     warning('more than 1 maxima')
    ##     max.rows <- max.rows[1,]
    ## }
    ## max.rows
    new.g
}

##
## gamma, sigma: distribution params
## n: number of simulations
## T: time to maturity
## 
sim.returns.skewed.normal <- function(gamma, sigma, n, T) {
    if(n<1 | (n%%1!=0))
        stop("'n' should be an integer >0")
    if(gamma <= 0)
        stop("'gamma' should be positive")
    if(sigma <= 0)
        stop("'sigma' should be positive")
    stopifnot(T > 0)

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
        x <- (1/sqrt(NR.WORKING.DAYS.PER.YEAR)) * simnorm * sigma * (simsign*gamma - (1-simsign)/gamma) 
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
f.t.k.skewed.normal <- function(f0k, gamma, sigma, n,T){
    tmp <- sim.returns.skewed.normal(gamma, sigma, n, T)
    tmp <- exp(tmp) / mean(exp(tmp))
    FtK <- tmp * f0k
    PayoffK <- (FtK > 1) * (FtK - 1) # element by element
    mean_PayoffK <- mean(PayoffK)
    return(mean_PayoffK)
}

## Function to get SSR values in 2nd step
## inputs:
##   grids : l dimentional list of grids, each element is a vector of sizes (n1,...,nl)
## Funongrids: [matrix DxK], K=(l+1) for l parameters, D is the number of grid points
##   F0_K  : [vector, size k] of interpolation values for the first parameter in 'param'
##   Kvec     : [vector, size k] of strike prices (data)
##   C     : [vector, size k] of call option prices (data)
##   r     : [double>0] interest rate (annual)
##   T     : [integer>0] days to maturity
## outputs:
##  list containing the following:
##    minPars : [vector size (l-1)] of optimal parameter values, giving min SSR
##    minSSR  : [double] SSR value
##    SSRs    : [vector size (n2xn3x...xnl)] of SSR values
## FIXME: rm grids
minSSR <- function(Funongrids,F0_K,Kvec,C,r,T){
    ## data inputs should be same size vectors
    if(length(F0_K)!=length(Kvec) | length(F0_K)!=length(C))
        stop("data inputs should be same size vectors")
    D <- ncol(Funongrids) # number of grid evaluations
    K <- nrow(Funongrids)
    KF <- length(F0_K) # number of F0 evaluations
    KP <- K/KF # number of other parameter evaluations
    Tannual <- T / 252 #(time to maturity in years)
    
    tmp  <- seq(1,K,KF) 
    Pars <- Funongrids[tmp,-c(1,D)]
    if(is.vector(Pars))
        Pars <- matrix(Pars,nrow=KP)
    SSRs <- rep(0,KP)
    for(kf in 1:KF){
        ind <- seq(kf,K,KF)
        prices <- exp(-r*Tannual) * Kvec[kf] * Funongrids[ind,D]
        SSRs <- SSRs + (prices - C[kf])^2
    }
    minSSR <- min(SSRs)
    iminSSR <- which(SSRs==minSSR)
    minPars <- Pars[iminSSR,]

    ## set a very high SSR value if value is NaN
    if(is.nan(minSSR))
        minSSR <- SSRs <- 10e8

    return(list(minPars = minPars, minSSR = minSSR, SSRs = SSRs))
}

###
##
## grid: grid points of the model parameters
## n: number of simulations for stock price evaluation
## f0k: initial stock prices (m-column vector)
option.pricing <- function(grid, n, f0k, Kvec, C, r, T){
    ## Check input
    if(!is.vector(f0k) | !is.vector(Kvec) | !is.vector(C))
        stop('Data must be specified as vectors')        
    if(length(f0k) != length(Kvec))
        stop('length(f0k) != length(Kvec)')
    if (length(f0k) != length(C))
        stop('length(f0k) != length(C))')
    
    ## Calculate valuation in initial grid points
    model.fun.on.grid <- apply(grid, 1, function(x) {
        do.call(f.t.k.skewed.normal, c(unname(as.list(x)), n=n, T=T))
    })

    grid.with.model <- cbind(grid, model.fun.on.grid)
    
    ## Interpolate function in second step - finer grid values
    result.grids <- lapply(f0k, function(x) {grid.search(grid.with.model, 1, x)})
   
    ## Get parameter values leading to minimum SSR in second step
    minpars <-  sapply(result.grids, function(x) {
        minSSR(x, f0k, Kvec, C, r, T)$minPars
    })
    out <- optim(par = minpars, fn = CalcGridSSR,
                 Funongrids_init = Funongrids_init,
                 f0k = f0k, Kvec = Kvec,  C = C,
                 r = r, T = T)
    return(list(par=out$par,value=out$value))
}
