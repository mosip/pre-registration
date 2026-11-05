import { Injectable } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { UnloadDeactivateGuardService } from '../unload-guard/unload-deactivate-guard.service';
import { MatDialog } from '@angular/material';

@Injectable({
  providedIn: 'root'
})
export abstract class FormDeactivateGuardService extends UnloadDeactivateGuardService {
  abstract get userForm(): FormGroup;
  abstract get canDeactivateFlag(): boolean;

  constructor(dialoug: MatDialog) {
    super(dialoug);
  }

  flag: boolean;
  canDeactivate(): boolean {
    if (!this.canDeactivateFlag) {
      return true;
    } else if (this.userForm) {
      (<any>Object).values(this.userForm.controls).forEach((element: FormControl) => {
        let tempFlag = element.value !== '' ? true : false;
        if (tempFlag) {
          if (this.userForm.dirty) this.flag = true;
          else this.flag = false;
        }
      });
      return !this.flag;
    }
  }
}
