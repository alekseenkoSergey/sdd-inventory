import { Injectable } from '@angular/core';
import { HttpClient, HttpClientModule } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  get<T>(endpoint: string): any {
    return this.http.get<T>(`${this.baseUrl}${endpoint}`, { withCredentials: true });
  }

  post<T>(endpoint: string, body: any): any {
    return this.http.post<T>(`${this.baseUrl}${endpoint}`, body, { withCredentials: true });
  }

  put<T>(endpoint: string, body: any): any {
    return this.http.put<T>(`${this.baseUrl}${endpoint}`, body, { withCredentials: true });
  }

  delete<T>(endpoint: string): any {
    return this.http.delete<T>(`${this.baseUrl}${endpoint}`, { withCredentials: true });
  }
}
