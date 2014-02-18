#include "optionpricing.h"

static const int NR_WORKING_DAYS_PER_YEAR = 252;

void ftk_skewed_normal_all(int *_nr, double *f0k, double *gamma, double *sigma, int *_n, int *_T, double *result) {
  int nr = *_nr;
  int n = *_n;
  int T = *_T;

  GetRNGstate();

  for(int i=0;i<nr;i++) {
    result[i] = ftk_skewed_normal(f0k[i], gamma[i], sigma[i], n, T);
  }
  PutRNGstate();
}

double ftk_skewed_normal(double f0k, double gamma, double sigma, int n, int T) {
  double * result = (double *) malloc (n * sizeof(double));

  sim_returns_skewed_normal(f0k, gamma, sigma, n, T, result);
    
  double final_res = 0.0;
  
  double meanexp = 0.0;
  for (int i=0;i<n;i++) {
    meanexp += exp(result[i]);
  }
  meanexp /= n;

  for (int i=0;i<n;i++) {
    double tmp = exp(result[i]) / meanexp;
    tmp *= f0k;
    if (tmp > 1.0) {
      final_res += tmp;
    }
  }

  free(result);

  return final_res / n;
}

void sim_returns_skewed_normal(double f0k, double gamma, double sigma, int n, int T, double *result) {
  assert(n > 0);
  assert(gamma > 0.0);
  assert(sigma > 0.0);
  assert(T > 0);

  // prob(x > 0)
  double ppos = gamma / (1.0 / gamma + gamma);
  // prob(x <= 0)
  double pneg = 1.0 - ppos;

  // alloc needed arrays
  double *simnorm = (double *) malloc(n * sizeof(double));
  double *simsign = (double *) malloc(n * sizeof(double));

  const double const1 = 1.0 / sqrt(NR_WORKING_DAYS_PER_YEAR);

  // empty result
  memset(result, 0, n * sizeof(double));

  for (int t=0;t<T;t++) {
    fill_simnorm(simnorm, n);
    fill_simsign(simsign, n, ppos);

    for (int i=0;i<n;i++) {
      result[i] += const1 * simnorm[i] * sigma * 
	(simsign[i] * gamma - (1.0 - simsign[i]) / gamma);
    }
  }

  free(simnorm);
  free(simsign);
}


