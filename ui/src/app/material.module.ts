import { NgModule } from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatExpansionModule } from '@angular/material/expansion';
import {
  MatButtonModule,
  MatButtonToggleModule,
  MatCardModule,
  MatCheckboxModule,
  MatDialogModule,
  MatIconModule,
  MatInputModule,
  MatListModule,
  MatProgressSpinnerModule,
  MatRadioModule,
  MatSelectModule,
  MatSidenavModule,
  MatSlideToggleModule,
  MatToolbarModule,
  MatDatepickerModule,
  MatNativeDateModule,
  MatPaginatorModule,
  MatFormFieldModule
} from '@angular/material';
import { NgxMatSelectSearchModule } from 'ngx-mat-select-search';
import {MatSnackBarModule} from '@angular/material/snack-bar';
@NgModule({
  exports: [
    MatButtonModule,
    MatButtonToggleModule,
    MatCardModule,
    MatCheckboxModule,
    MatDialogModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatProgressSpinnerModule,
    MatRadioModule,
    MatSelectModule,
    MatSidenavModule,
    MatSlideToggleModule,
    MatToolbarModule,
    MatExpansionModule,
    MatTooltipModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatPaginatorModule,
    MatFormFieldModule,
    NgxMatSelectSearchModule,
    MatSnackBarModule
  ]
})
export class MaterialModule {}
