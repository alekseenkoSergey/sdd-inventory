import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Location, CreateLocationRequest, RenameLocationRequest, ErrorResponse } from './location.model';

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private apiUrl = '/locations';

  constructor(private http: HttpClient) { }

  createLocation(name: string): Observable<Location> {
    const request: CreateLocationRequest = { name };
    return this.http.post<Location>(this.apiUrl, request)
      .pipe(catchError(this.handleError));
  }

  getLocations(): Observable<Location[]> {
    return this.http.get<Location[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  getLocation(id: number): Observable<Location> {
    return this.http.get<Location>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  renameLocation(id: number, name: string): Observable<Location> {
    const request: RenameLocationRequest = { name };
    return this.http.put<Location>(`${this.apiUrl}/${id}`, request)
      .pipe(catchError(this.handleError));
  }

  deleteLocation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unexpected error occurred';

    if (error.error instanceof ErrorEvent) {
      errorMessage = error.error.message;
    } else {
      const errorResponse: ErrorResponse = error.error;
      errorMessage = errorResponse.message || errorMessage;
    }

    return throwError(() => new Error(errorMessage));
  }
}
