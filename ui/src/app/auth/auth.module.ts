import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { CommonModule } from "@angular/common";

import { AuthRoutingModule } from "./auth-routing.module";
import { LoginComponent } from "./login/login.component";
import { AppRoutingModule } from "../app-routing.module";
import { SharedModule } from "../shared/shared.module";
import { CoreModule } from "../core/core.module";
import {
  RecaptchaModule
} from "ng-recaptcha";
import { CaptchaComponent } from './captcha/captcha.component';
@NgModule({
  declarations: [LoginComponent, CaptchaComponent],
  imports: [
    FormsModule,
    CommonModule,
    ReactiveFormsModule,
    AuthRoutingModule,
    AppRoutingModule,
    SharedModule,
    CoreModule,
    RecaptchaModule
  ],
  providers: [],
})
export class AuthModule {}
