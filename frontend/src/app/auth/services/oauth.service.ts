import { Injectable } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class OAuthService {
  private readonly authorizationEndpoint = 'http://localhost:8080/oauth2/authorization/google';

  constructor(
    private router: Router,
    private route: ActivatedRoute
  ) {}

  initiateGoogleLogin(): void {
    window.location.href = this.authorizationEndpoint;
  }

  handleOAuthCallback(): void {
    this.router.navigate(['/home']);
  }

  checkForOAuthErrors(): { error: string | null; description: string | null } {
    let error: string | null = null;
    let description: string | null = null;

    this.route.queryParams.subscribe(params => {
      error = params['error'] || null;
      description = params['error_description'] || null;
    });

    return { error, description };
  }
}
