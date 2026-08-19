import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { UserProfile } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  public authenticatedSubject = new BehaviorSubject<boolean>(false);
  public authenticated$ = this.authenticatedSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(): Observable<any> {
    return this.http.get(`${this.apiUrl}/login`, { withCredentials: true })
      .pipe(
        tap(() => this.authenticatedSubject.next(true))
      );
  }

  logout(): Observable<any> {
    return this.http.post(`${this.apiUrl}/logout`, {}, { withCredentials: true })
      .pipe(
        tap(() => {
          this.authenticatedSubject.next(false);
        })
      );
  }

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/user/profile`, { withCredentials: true });
  }

  checkSessionOnLoad(): Observable<boolean> {
    return this.getProfile()
      .pipe(
        map(() => {
          this.authenticatedSubject.next(true);
          return true;
        }),
        tap(() => {
          console.log('Session restored on page refresh');
        })
      );
  }

  isAuthenticated(): boolean {
    return this.authenticatedSubject.value;
  }
}
