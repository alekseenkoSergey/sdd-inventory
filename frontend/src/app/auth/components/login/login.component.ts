import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OAuthService } from '../../services/oauth.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  standalone: true,
  imports: [CommonModule]
})
export class LoginComponent {
  errorMessage: string | null = null;

  constructor(
    private oAuthService: OAuthService,
    private authService: AuthService
  ) {}

  loginWithGoogle(): void {
    this.errorMessage = null;
    try {
      this.oAuthService.initiateGoogleLogin();
    } catch (error) {
      this.errorMessage = 'Failed to initiate login. Please try again.';
    }
  }
}
