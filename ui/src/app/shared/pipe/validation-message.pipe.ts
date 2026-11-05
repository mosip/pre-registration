import { Pipe, PipeTransform } from '@angular/core';
import { DataStorageService } from 'src/app/core/services/data-storage.service';

@Pipe({name: 'validationMessage'})
export class ValidationMessagePipe implements PipeTransform {
  constructor(private dataStorageService:DataStorageService){}
  message;
  async transform(value: string , arg1:any): Promise<string> {
    console.log(arg1);
    await this.getValidationMessage(value);
    return this.message[arg1];
  }

  getValidationMessage(language){
    return new Promise(resolve =>{
      this.dataStorageService
      .getI18NLanguageFiles(language)
      .subscribe((response) => {
        this.message = response["demographic"]["validation"];
        resolve(true);
      });
    });
  }
}