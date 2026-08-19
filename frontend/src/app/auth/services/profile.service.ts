import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import { UserProfile } from '../models/user.model';
import { Observable, of } from 'rxjs';
import { shareReplay, catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private profileCache$: Observable<UserProfile | null> | null = null;

  constructor(private authService: AuthService) {}

  getProfile(): Observable<UserProfile | null> {
    if (!this.profileCache$) {
      this.profileCache$ = this.authService.getProfile().pipe(
        catchError(() => of(null)),
        shareReplay(1)
      );
    }
    return this.profileCache$;
  }

  displayProfile(): UserProfile | null {
    return null;
  }

  clearCache(): void {
    this.profileCache$ = null;
  }
}
