import { Component, OnInit, Input, EventEmitter, Output } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
@Component({
  selector: "app-captcha",
  templateUrl: "./captcha.component.html",
  styleUrls: ["./captcha.component.css"],
})
export class CaptchaComponent implements OnInit {
  @Input() captchaSiteKey: string;
  @Input() resetCaptcha: boolean;
  @Output() captchaEvent = new EventEmitter<string>();
  langCode: string;
  constructor(private activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.paramMap.subscribe((param) => {
      this.langCode = param.get("lang").substr(0, 2);
    });
  }

  ngOnChanges(): void {
    if (this.resetCaptcha) this.handleReset();
  }

  recaptcha(captchaResponse: any) {
    console.log(`Resolved captcha with response: ${captchaResponse}`);
      this.captchaEvent.emit(captchaResponse);
   }

  recaptchaError(event) {
    alert(event);
  }

  handleReset() {
    grecaptcha.reset();
  }
}
