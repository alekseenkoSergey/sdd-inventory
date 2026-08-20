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
  private apiBaseUrl = 'http://localhost:8080/api/v1';
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
      `${this.apiBaseUrl}/items/${itemId}/movements`,
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
  ): Observable<StockMovement[]> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    const params: { [key: string]: string } = {};
    if (startDate) params['startDate'] = startDate;
    if (endDate) params['endDate'] = endDate;

    return this.http.get<StockMovement[]>(
      `${this.apiBaseUrl}/items/${itemId}/movements`,
      { params, withCredentials: true }
    ).pipe(
      tap(movements => {
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
