import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Category, CreateCategoryRequest, RenameCategoryRequest } from '../models/category.model';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiUrl = 'http://localhost:8080/api/v1/categories';
  private httpOptions = {
    withCredentials: true
  };

  constructor(private http: HttpClient) {}

  createCategory(request: CreateCategoryRequest): Observable<Category> {
    return this.http.post<Category>(this.apiUrl, request, this.httpOptions).pipe(
      catchError(this.handleError)
    );
  }

  listCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(this.apiUrl, this.httpOptions).pipe(
      catchError(this.handleError)
    );
  }

  getCategory(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.apiUrl}/${id}`, this.httpOptions).pipe(
      catchError(this.handleError)
    );
  }

  renameCategory(id: number, request: RenameCategoryRequest): Observable<Category> {
    return this.http.patch<Category>(`${this.apiUrl}/${id}`, request, this.httpOptions).pipe(
      catchError(this.handleError)
    );
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, this.httpOptions).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    return throwError(() => error);
  }
}
