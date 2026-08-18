import { Component } from '@angular/core';
import { OAuthService } from '../../services/oauth.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
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
