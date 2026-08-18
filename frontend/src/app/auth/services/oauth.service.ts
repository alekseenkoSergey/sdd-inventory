import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class OAuthService {
  private readonly authorizationEndpoint = 'http://localhost:8080/oauth2/authorization/google';

  constructor(private router: Router) {}

  initiateGoogleLogin(): void {
    window.location.href = this.authorizationEndpoint;
  }

  handleOAuthCallback(): void {
    this.router.navigate(['/home']);
  }
}
