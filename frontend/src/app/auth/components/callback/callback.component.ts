import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-callback',
  template: `
    <div style="display: flex; justify-content: center; align-items: center; height: 100vh; flex-direction: column; gap: 1rem;">
      <p>Completing login...</p>
      <div style="border: 3px solid #f3f3f3; border-top: 3px solid #4F46E5; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite;"></div>
    </div>
    <style>
      @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
      }
    </style>
  `,
  standalone: true,
  imports: [CommonModule]
})
export class CallbackComponent implements OnInit {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.login().subscribe(
      () => {
        this.router.navigate(['/home']);
      },
      (error) => {
        console.error('OAuth callback failed:', error);
        this.router.navigate(['/login'], { queryParams: { error: 'Authentication failed' } });
      }
    );
  }
}
