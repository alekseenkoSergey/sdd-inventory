import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LocationListComponent } from '../location-list/location-list.component';
import { LocationFormComponent } from '../location-form/location-form.component';
import { Location } from '../location.model';

@Component({
  selector: 'app-locations-page',
  templateUrl: './locations-page.component.html',
  styleUrl: './locations-page.component.css',
  standalone: true,
  imports: [CommonModule, LocationListComponent, LocationFormComponent]
})
export class LocationsPageComponent implements OnInit {
  @ViewChild(LocationListComponent) locationListComponent!: LocationListComponent;

  showCreateDialog = false;
  editingLocationId: number | null = null;

  constructor(private router: Router) {}

  ngOnInit(): void {}

  goBack(): void {
    this.router.navigate(['/home']);
  }

  openCreateDialog(): void {
    this.editingLocationId = null;
    this.showCreateDialog = true;
  }

  closeCreateDialog(): void {
    this.showCreateDialog = false;
  }

  onLocationCreated(location: Location): void {
    this.closeCreateDialog();
    if (this.locationListComponent) {
      this.locationListComponent.loadLocations();
    }
  }

  onLocationUpdated(location: Location): void {
    this.closeCreateDialog();
    if (this.locationListComponent) {
      this.locationListComponent.loadLocations();
    }
  }
}
