#include <R.h>
#include <Rmath.h>
#include <Rinternals.h>
#include <R_ext/Rdynload.h>

typedef struct Matrix {
	double * const data;
	int const nRow;
	int const nCol;
} Matrix;

/**
 * Get an element of a matrix.
 * @param i Row index.
 * @param j Column index.
 */
static inline double *get(Matrix *m, int i, int j) {
	return m->data + j * (m->nRow) + i;
}

/**
 * Write a row to a matrix.
 */
static inline void writeRow(Matrix *m, int i, double *x) {
	for (int j = 0; j < m->nCol; ++j) {
		*get(m, i, j) = x[j];
	}
}

/**
 * Simulate skewed normal normal for multiple values (grid points)
 * 
 * @param nr the number of points in f0k, sigma, and gamma
 * @param f0k a vector of the observed prices 
 * @param gamma, sigma vectors of distribution parameters
 * @param n the number of simulations
 * @param T days to maturity
 * @param result a vector where to store the result at
 */
void sim_returns_skewed_normal_all(int *nr, double *f0k, double *gamma, double *sigma, int *n, int*T, double *result);

/**
 * Simulate skewed normal for a single grid point.
 * 
 * @param f0k the observed price
 * @param gamma, sigma the distribution parameters
 * @param n the number of simulations
 * @param T days to maturity
 */
double sim_returns_skewed_normal(double f0k, double gamma, double sigma, int n, int T);
