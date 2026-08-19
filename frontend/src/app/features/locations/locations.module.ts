import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { LocationListComponent } from './location-list/location-list.component';
import { LocationFormComponent } from './location-form/location-form.component';
import { LocationService } from './location.service';

@NgModule({
  declarations: [
    LocationListComponent,
    LocationFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HttpClientModule
  ],
  providers: [LocationService],
  exports: [LocationListComponent]
})
export class LocationsModule { }
