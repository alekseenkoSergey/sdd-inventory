import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Location, CreateLocationRequest, RenameLocationRequest, ErrorResponse } from './location.model';

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private apiUrl = '/api/locations';
  private httpOptions = {
    withCredentials: true
  };

  constructor(private http: HttpClient) { }

  createLocation(name: string): Observable<Location> {
    const request: CreateLocationRequest = { name };
    return this.http.post<Location>(this.apiUrl, request, this.httpOptions)
      .pipe(catchError(this.handleError));
  }

  getLocations(): Observable<Location[]> {
    return this.http.get<Location[]>(this.apiUrl, this.httpOptions)
      .pipe(catchError(this.handleError));
  }

  getLocation(id: number): Observable<Location> {
    return this.http.get<Location>(`${this.apiUrl}/${id}`, this.httpOptions)
      .pipe(catchError(this.handleError));
  }

  renameLocation(id: number, name: string): Observable<Location> {
    const request: RenameLocationRequest = { name };
    return this.http.put<Location>(`${this.apiUrl}/${id}`, request, this.httpOptions)
      .pipe(catchError(this.handleError));
  }

  deleteLocation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, this.httpOptions)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unexpected error occurred';

    console.error('LocationService error:', error);

    if (error.error instanceof ErrorEvent) {
      errorMessage = error.error.message;
    } else if (error.error instanceof ProgressEvent) {
      errorMessage = `HTTP Error ${error.status}: ${error.statusText}`;
    } else if (typeof error.error === 'string') {
      errorMessage = `HTTP Error ${error.status}: ${error.statusText}`;
    } else if (error.error && typeof error.error === 'object') {
      const errorResponse: ErrorResponse = error.error;
      errorMessage = errorResponse.message || `HTTP Error ${error.status}: ${error.statusText}`;
    } else {
      errorMessage = `HTTP Error ${error.status}: ${error.statusText}`;
    }

    return throwError(() => new Error(errorMessage));
  }
}
