import { NgModule } from '@angular/core';
import { LocationListComponent } from './location-list/location-list.component';
import { LocationService } from './location.service';

@NgModule({
  imports: [
    LocationListComponent
  ],
  providers: [LocationService],
  exports: [LocationListComponent]
})
export class LocationsModule { }
