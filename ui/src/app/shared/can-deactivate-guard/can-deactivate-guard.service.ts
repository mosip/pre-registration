import { Injectable } from "@angular/core";
import { UnloadDeactivateGuardService } from "./unload-guard/unload-deactivate-guard.service";
import { CanDeactivate } from "@angular/router";
import { AuthService } from "src/app/auth/auth.service";
import { MatDialog } from "@angular/material";
import { DialougComponent } from "../dialoug/dialoug.component";
import { Observable } from "rxjs";
import { DataStorageService } from "src/app/core/services/data-storage.service";

@Injectable({
  providedIn: "root",
})
export class CanDeactivateGuardService
  implements CanDeactivate<UnloadDeactivateGuardService> {
  //langCode = localStorage.getItem("langCode");
  constructor(
    private authService: AuthService,
    public dialog: MatDialog,
    private dataStorageService: DataStorageService
  ) {}

  canDeactivate(
    component: UnloadDeactivateGuardService
  ): boolean | Observable<boolean> | Promise<boolean> {
    if (this.authService.isAuthenticated() && !component.canDeactivate()) {
      let message;
      let ok_text;
      let no_text;
      return new Promise((resolve) => {
       this.dataStorageService
        .getI18NLanguageFiles(localStorage.getItem("userPrefLanguage"))
        .subscribe((response) => {
          message = response["dialog"]["navigation_alert"];
          ok_text = response["dialog"]["action_ok"];
          no_text = response["dialog"]["title_discard"];
          const body = {
            case: "CONFIRMATION",
            message: message,
            yesButtonText: ok_text,
            noButtonText: no_text,
          };
          this.dialog
            .open(DialougComponent, { width: "400px", data: body })
            .beforeClosed()
            .subscribe((res) => {
              if (res === true) resolve(true);
              else resolve(false);
            });
        });
      });
    } else return true;
  }
}
