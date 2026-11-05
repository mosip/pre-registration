import { NgModule } from '@angular/core';
import { Routes, RouterModule, PreloadAllModules } from '@angular/router';
import { FaqComponent } from './core/faq/faq.component';
import { AboutUsComponent } from './core/about-us/about-us.component';
import { ContactComponent } from './core/contact/contact.component';
import { ParentComponent } from './shared/parent/parent.component';
import { AuthGuardService } from './auth/auth-guard.service';

/**
 * @description These are the routes.
 */
const appRoutes: Routes = [
  { path: ':userPreferredLanguage/dashboard', loadChildren: './feature/dashboard/dashboard.module#DashboardModule' },
  { path: 'about-us', component: AboutUsComponent },
  { path: 'faq', component: FaqComponent },
  { path: 'contact', component: ContactComponent },      
  {
    path: ':userPreferredLanguage/pre-registration',
    component: ParentComponent,
    canActivate: [AuthGuardService],
    children: [
      { path: '', pathMatch: 'full', redirectTo: '/' },
      { path: 'demographic/new', loadChildren: './feature/demographic/demographic.module#DemographicModule' },
      { path: 'demographic/:appId', loadChildren: './feature/demographic/demographic.module#DemographicModule' },
      { path: 'file-upload/:appId', loadChildren: './feature/file-upload/file-upload.module#FileUploadModule' },
      { path: 'summary/:appId', loadChildren: './feature/summary/summary.module#SummaryModule' },
      { path: 'booking/:appId', loadChildren: './feature/booking/booking.module#BookingModule' },
      { path: 'booking/multiappointment', loadChildren: './feature/booking/booking.module#BookingModule' },
      

    ]
  }
];

/**
 * @author Shashank Agrawal
 *
 * @export
 * @class AppRoutingModule
 */
@NgModule({
  imports: [RouterModule.forRoot(appRoutes, { useHash: true, preloadingStrategy: PreloadAllModules, onSameUrlNavigation: 'reload' })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
