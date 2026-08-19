import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { OAuthService } from '../../services/oauth.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  standalone: true,
  imports: [CommonModule]
})
export class LoginComponent implements OnInit {
  errorMessage: string | null = null;

  constructor(
    private oAuthService: OAuthService,
    private authService: AuthService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['error']) {
        this.errorMessage = params['error_description'] || 'Authentication failed. Please try again.';
      }
    });
  }

  loginWithGoogle(): void {
    this.errorMessage = null;
    try {
      this.oAuthService.initiateGoogleLogin();
    } catch (error) {
      this.errorMessage = 'Failed to initiate login. Please try again.';
    }
  }
}
