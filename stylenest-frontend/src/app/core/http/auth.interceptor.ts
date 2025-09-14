import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('jwt');
  // Do not attach Authorization header to public auth endpoints
  const url = req.url || '';
  const isAuthEndpoint = url.startsWith('/auth') || url.includes('/auth/');
  if (token && !isAuthEndpoint) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req);
};
