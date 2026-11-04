import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-parent',
  templateUrl: './parent.component.html',
  styleUrls: ['./parent.component.css']
})
export class ParentComponent implements OnInit {
  componentName: string;

  constructor(private router: Router, private translate: TranslateService) {
    this.translate.use(localStorage.getItem('langCode'));
  }

  ngOnInit() {}

  onActivate($event) {
    const route_parts = this.router.url.split('/');
    if (route_parts[3] === 'demographic') {
      this.componentName = 'DemographicComponent';
    } else if (route_parts[3] === 'file-upload') {
      this.componentName = 'FileUploadComponent';
    } else if (route_parts[5] === 'pick-center') {
      this.componentName = 'CenterSelectionComponent';
    } else if (route_parts[5].includes('pick-time')) {
      this.componentName = 'TimeSelectionComponent';
    } else if (route_parts[5] === 'acknowledgement') {
      this.componentName = 'AcknowledgementComponent';
    } else if (route_parts[5] === 'preview') {
      this.componentName = 'PreviewComponent';
    }
  }
}
