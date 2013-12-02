
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
