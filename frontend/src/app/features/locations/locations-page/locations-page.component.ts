import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LocationListComponent } from '../location-list/location-list.component';

@Component({
  selector: 'app-locations-page',
  templateUrl: './locations-page.component.html',
  styleUrl: './locations-page.component.css',
  standalone: true,
  imports: [CommonModule, LocationListComponent]
})
export class LocationsPageComponent implements OnInit {
  constructor(private router: Router) {}

  ngOnInit(): void {}

  goBack(): void {
    this.router.navigate(['/home']);
  }
}
