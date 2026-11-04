import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material';

@Injectable({
  providedIn: 'root'
})
export abstract class UnloadDeactivateGuardService {
  abstract canDeactivate(): boolean;
  constructor(public dialog: MatDialog) {}
  // @HostListener('window:beforeunload', ['$event'])
  // unloadNotification($event: any) {
  //   if (!this.canDeactivate()) {
  //     $event.returnValue = true;
  //   }
  // }
}
