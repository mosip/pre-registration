import { FormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { BookingRoutingModule } from './booking-routing.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { CenterSelectionComponent } from './center-selection/center-selection.component';
import { MapComponent } from './map/map.component';
import { TimeSelectionComponent } from './time-selection/time-selection.component';
import {MatSnackBarModule} from '@angular/material/snack-bar';

@NgModule({
  declarations: [CenterSelectionComponent, MapComponent, TimeSelectionComponent],
  imports: [CommonModule, SharedModule, BookingRoutingModule, FormsModule,MatSnackBarModule]
})
export class BookingModule {}
