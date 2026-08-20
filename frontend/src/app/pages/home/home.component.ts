import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/services/auth.service';
import { ProfileService } from '../../auth/services/profile.service';
import { UserProfile } from '../../auth/models/user.model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
  standalone: true,
  imports: [CommonModule]
})
export class HomeComponent implements OnInit {
  userProfile$: Observable<UserProfile | null> | null = null;

  constructor(
    private profileService: ProfileService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userProfile$ = this.profileService.getProfile();
  }

  logout(): void {
    this.authService.logout().subscribe(
      () => {
        this.router.navigate(['/login']);
      },
      (error) => {
        console.error('Logout failed:', error);
        this.router.navigate(['/login']);
      }
    );
  }

  navigateToCategories(): void {
    this.router.navigate(['/categories']);
  }

  navigateToLocations(): void {
    this.router.navigate(['/locations']);
  }
}
