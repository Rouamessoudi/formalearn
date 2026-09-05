import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token;
  const request = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` }, withCredentials: false })
    : req;
  return next(request).pipe(
    catchError((err: HttpErrorResponse) => {
      const isLogin = req.url.includes('/auth/login');
      if (err.status === 401 && !isLogin && auth.token) {
        auth.logout();
      }
      return throwError(() => err);
    })
  );
};
