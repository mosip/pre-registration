import { Injectable } from "@angular/core";
import { Router } from "@angular/router";
import { UserIdleService } from "angular-user-idle";
import { DataStorageService } from "../core/services/data-storage.service";

@Injectable({
  providedIn: "root",
})
export class AuthService {
  cookieName = "Authorization";
  isCaptchaSuccess: boolean = false;
  constructor(
    private router: Router,
    private dataStorageService: DataStorageService,
    private userIdle: UserIdleService
  ) {}

  token: string;
  userPreferredLang: string = localStorage.getItem('userPrefLanguage');

  getUserPreferredLanguage() {
    return this.userPreferredLang;
  }

  setToken() {
    this.token = this.getCookie();
  }

  removeToken() {
    this.token = null;
  }

  getCookie() {
    const name = this.cookieName;
    var match = document.cookie.match(new RegExp("(^| )" + name + "=([^;]+)"));
    if (match) return match[2];
  }

  setCaptchaAuthenticate(isSuccess: boolean) {
    this.isCaptchaSuccess = isSuccess;
  }

  isCaptchaAuthenticated() {
    return this.isCaptchaSuccess;
  }

  isAuthenticated() {
    if (
      localStorage.getItem("loggedIn") === "true" &&
      (this.token !== null || this.token !== undefined)
    ) {
      return true;
    } else {
      return false;
    }
  }

  onLogout() {
    localStorage.setItem("loggedIn", "false");
    localStorage.setItem("loggedOut", "true");
    this.removeToken();
    localStorage.removeItem("config");
    localStorage.removeItem("dir");
    localStorage.clear();
    this.setCaptchaAuthenticate(false);
    this.dataStorageService.onLogout().subscribe();
    this.router.navigate(["/"]);
    this.userIdle.stopWatching();
  }
}
