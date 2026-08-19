import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LocationService } from '../location.service';
import { Location } from '../location.model';
import { LocationFormComponent } from '../location-form/location-form.component';

@Component({
  selector: 'app-location-list',
  templateUrl: './location-list.component.html',
  styleUrls: ['./location-list.component.css'],
  standalone: true,
  imports: [CommonModule, LocationFormComponent]
})
export class LocationListComponent implements OnInit {
  locations: Location[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  showForm = false;
  editingLocationId: number | null = null;

  constructor(private locationService: LocationService) { }

  ngOnInit(): void {
    this.loadLocations();
  }

  loadLocations(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.locationService.getLocations().subscribe({
      next: (locations) => {
        this.locations = locations;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error.message;
        this.isLoading = false;
      }
    });
  }

  openCreateForm(): void {
    this.editingLocationId = null;
    this.showForm = true;
  }

  openEditForm(location: Location): void {
    this.editingLocationId = location.id;
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
  }

  onLocationCreated(location: Location): void {
    this.locations.push(location);
    this.closeForm();
  }

  onLocationUpdated(location: Location): void {
    const index = this.locations.findIndex(l => l.id === location.id);
    if (index !== -1) {
      this.locations[index] = location;
    }
    this.closeForm();
  }

  deleteLocation(location: Location): void {
    if (!confirm(`Are you sure you want to delete '${location.name}'?`)) {
      return;
    }

    this.isLoading = true;
    this.locationService.deleteLocation(location.id).subscribe({
      next: () => {
        this.locations = this.locations.filter(l => l.id !== location.id);
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.message;
      }
    });
  }

  trackByLocationId(index: number, location: Location): number {
    return location.id;
  }
}
