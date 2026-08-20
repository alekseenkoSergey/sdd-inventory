import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError, finalize } from 'rxjs/operators';
import {
  StockMovement,
  CreateStockMovementRequest,
  MovementHistoryResponse
} from '../models/stock-movement.model';

@Injectable({
  providedIn: 'root'
})
export class StockMovementService {
  private apiUrl = 'http://localhost:8080/api/v1/stock-movements';
  private httpOptions = {
    withCredentials: true
  };

  private loadingSubject = new BehaviorSubject<boolean>(false);
  loading$ = this.loadingSubject.asObservable();

  private errorSubject = new BehaviorSubject<string | null>(null);
  error$ = this.errorSubject.asObservable();

  constructor(private http: HttpClient) {}

  createMovement(itemId: number, request: CreateStockMovementRequest): Observable<StockMovement> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.post<StockMovement>(
      `${this.apiUrl}/items/${itemId}`,
      request,
      this.httpOptions
    ).pipe(
      tap(movement => {
        this.loadingSubject.next(false);
      }),
      catchError(error => {
        const errorMessage = this.extractErrorMessage(error);
        this.errorSubject.next(errorMessage);
        this.loadingSubject.next(false);
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  getMovementHistory(
    itemId: number,
    startDate?: string,
    endDate?: string
  ): Observable<MovementHistoryResponse> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    let params = {};
    if (startDate) params['startDate'] = startDate;
    if (endDate) params['endDate'] = endDate;

    return this.http.get<MovementHistoryResponse>(
      `${this.apiUrl}/items/${itemId}/history`,
      { params, withCredentials: true }
    ).pipe(
      tap(response => {
        this.loadingSubject.next(false);
      }),
      catchError(error => {
        const errorMessage = this.extractErrorMessage(error);
        this.errorSubject.next(errorMessage);
        this.loadingSubject.next(false);
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  private extractErrorMessage(error: any): string {
    if (error.status === 0) {
      return 'Network error. Please check your connection.';
    }
    if (error.error && typeof error.error === 'object') {
      if (error.error.message) {
        return error.error.message;
      }
      if (error.error.error) {
        return error.error.error;
      }
    }
    if (error.statusText) {
      return error.statusText;
    }
    return 'An error occurred. Please try again.';
  }
}
