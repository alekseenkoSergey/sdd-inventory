import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { OAuthService } from '../../services/oauth.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
  standalone: true,
  imports: [CommonModule]
})
export class LoginComponent implements OnInit {
  errorMessage: string | null = null;
  isLoading = signal(false);

  constructor(
    private oAuthService: OAuthService,
    private authService: AuthService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Check for OAuth errors in query parameters
    this.route.queryParams.subscribe(params => {
      if (params['error']) {
        this.errorMessage = params['error_description'] || 'Authentication failed. Please try again.';
      }
    });
  }

  loginWithGoogle(): void {
    this.errorMessage = null;
    this.isLoading.set(true);
    try {
      this.oAuthService.initiateGoogleLogin();
      // Note: isLoading will remain true as user is redirected to Google
    } catch (error) {
      this.isLoading.set(false);
      this.errorMessage = 'Failed to initiate login. Please try again.';
    }
  }
}
