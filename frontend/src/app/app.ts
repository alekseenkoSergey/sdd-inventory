import { Component, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './auth/services/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('frontend-temp');
  isLoading = signal(true);

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.checkSessionOnLoad().subscribe(
      () => this.isLoading.set(false),
      () => this.isLoading.set(false)
    );
  }
}
