import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { Event as NavigationEvent, Router, NavigationStart } from '@angular/router';
import { filter } from 'rxjs/operators';

import { AutoLogoutService } from 'src/app/core/services/auto-logout.service';
import { ConfigService } from './core/services/config.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'pre-registration';
  message: object;
  subscriptions: Subscription[] = [];

  constructor(private autoLogout: AutoLogoutService, private router: Router, private configService: ConfigService) {}

  ngOnInit() {
    this.subscriptions.push(this.autoLogout.currentMessageAutoLogout.subscribe(() => {
       // This is intentional
    }));
    this.autoLogout.changeMessage({ timerFired: false });
    this.routerType();
  }

  routerType() {
    this.subscriptions.push(
      this.router.events
        .pipe(filter((event: NavigationEvent) => event instanceof NavigationStart))
        .subscribe((event: NavigationStart) => {
          if (event.restoredState) {
           // This is intentional
          }
        })
    );
  }

  // preventBack() {
  //   window.history.forward();
  //   window.onunload = function() {
  //     null;
  //     console.log("Hello")
  //   };
  // }

  @HostListener('mouseover')
  @HostListener('document:mousemove', ['$event'])
  @HostListener('keypress')
  @HostListener('click')
  @HostListener('document:keypress', ['$event'])
  onMouseClick() {
    this.autoLogout.setisActive(true);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
