import { Injectable } from "@angular/core";
import { Location } from "@angular/common";
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse,
  HttpInterceptor,
} from "@angular/common/http";
import { Observable, throwError } from "rxjs";
import { catchError, tap } from "rxjs/operators";
import { Router } from "@angular/router";
import { MatDialog } from "@angular/material";
import Utils from "src/app/app.util";
import * as appConstants from "../app.constants";

/**
 * @description This is the interceptor service, which intercept all the http request.
 * @author Shashank Agrawal
 *
 * @export
 * @class AuthInterceptorService
 */
@Injectable({
  providedIn: "root",
})
export class AuthInterceptorService implements HttpInterceptor {
  /**
   * @description Creates an instance of AuthInterceptorService.
   * @memberof AuthInterceptorService
   */
  constructor(
    private router: Router,
    private locaiton: Location,
    private dialog: MatDialog
  ) {}

  /**
   * @description This is the interceptor, which intercept all the http request
   *
   * @param {HttpRequest<any>} req
   * @param {HttpHandler} next
   * @returns {Observable<HttpEvent<any>>}
   * @memberof AuthInterceptorService
   */
  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    const copiedReq = req.clone({
      withCredentials: true,
    });
    return next.handle(copiedReq).pipe(
      tap(response => {
        let body = response["body"];
        if (body && body[appConstants.NESTED_ERROR]) {
          console.log("response is null and there are some API errors");
          let errNestedObject = {};
          errNestedObject[appConstants.NESTED_ERROR] = body[appConstants.NESTED_ERROR];
          let errObject = {};
          errObject[appConstants.ERROR] = errNestedObject;
          //console.log(errObject);
          throw errObject; 
        }
      }),
      catchError((error: HttpErrorResponse) => {
        if (Utils.authenticationFailed(error)) {
          console.log("forcefully logout the user");
          localStorage.setItem(
            appConstants.FORCE_LOGOUT,
            appConstants.FORCE_LOGOUT_YES
          );
          this.router.navigateByUrl("/");  
        }
        //handle 500 error
        if (error.status === 500) {
          // this.router.navigateByUrl('/');
        }
        return throwError(error);
      })
    );
  }
}
