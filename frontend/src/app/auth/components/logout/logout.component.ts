import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ProfileService } from '../../services/profile.service';

@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html',
  standalone: true,
  imports: [CommonModule]
})
export class LogoutComponent {
  isLoggingOut = false;
  errorMessage: string | null = null;

  constructor(
    private authService: AuthService,
    private profileService: ProfileService,
    private router: Router
  ) {}

  logout(): void {
    this.isLoggingOut = true;
    this.errorMessage = null;

    this.authService.logout().subscribe({
      next: () => {
        this.profileService.clearCache();
        this.router.navigate(['/login']);
      },
      error: (error) => {
        this.isLoggingOut = false;
        this.errorMessage = 'Logout failed. Please try again.';
        console.error('Logout error:', error);
      }
    });
  }
}
