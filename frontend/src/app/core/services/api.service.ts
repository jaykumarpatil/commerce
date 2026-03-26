import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import { environment } from '@env/environment';

export interface HttpOptions {
  params?: HttpParams | { [param: string]: string | number | boolean | readonly (string | number | boolean)[] };
  headers?: { [header: string]: string | string[] };
  reportProgress?: boolean;
  withCredentials?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly timeout = 30000; // 30 seconds

  get<T>(endpoint: string, options?: HttpOptions): Observable<T> {
    return this.http.get<T>(`${this.baseUrl}${endpoint}`, this.buildOptions(options))
      .pipe(
        retry({ count: 1, delay: 1000 }),
        catchError(this.handleError)
      );
  }

  post<T>(endpoint: string, body: unknown, options?: HttpOptions): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}${endpoint}`, body, this.buildOptions(options))
      .pipe(
        catchError(this.handleError)
      );
  }

  put<T>(endpoint: string, body: unknown, options?: HttpOptions): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}${endpoint}`, body, this.buildOptions(options))
      .pipe(
        catchError(this.handleError)
      );
  }

  patch<T>(endpoint: string, body?: unknown, options?: HttpOptions): Observable<T> {
    return this.http.patch<T>(`${this.baseUrl}${endpoint}`, body, this.buildOptions(options))
      .pipe(
        catchError(this.handleError)
      );
  }

  delete<T>(endpoint: string, options?: HttpOptions): Observable<T> {
    return this.http.delete<T>(`${this.baseUrl}${endpoint}`, this.buildOptions(options))
      .pipe(
        catchError(this.handleError)
      );
  }

  private buildOptions(options?: HttpOptions): HttpOptions {
    if (!options) {
      return { withCredentials: true };
    }
    return {
      ...options,
      withCredentials: options.withCredentials ?? true
    };
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unknown error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else {
      // Server-side error
      switch (error.status) {
        case 400:
          errorMessage = error.error?.message || 'Bad request';
          break;
        case 401:
          errorMessage = 'Unauthorized - Please login again';
          break;
        case 403:
          errorMessage = 'Forbidden - You do not have permission';
          break;
        case 404:
          errorMessage = error.error?.message || 'Resource not found';
          break;
        case 422:
          errorMessage = error.error?.message || 'Validation error';
          break;
        case 500:
          errorMessage = 'Server error - Please try again later';
          break;
        case 502:
        case 503:
        case 504:
          errorMessage = 'Service unavailable - Please try again later';
          break;
        default:
          errorMessage = error.error?.message || `Error: ${error.status}`;
      }
    }

    console.error('API Error:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }
}
