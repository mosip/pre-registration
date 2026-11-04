import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { PdfViewerModule } from "ng2-pdf-viewer"; // <- import OrderModule
import { ReactiveFormsModule } from "@angular/forms";
import { SharedModule } from "src/app/shared/shared.module";
import { FileUploadRoutingModule } from "./file-upload-routing.module";
import { FileUploadComponent } from "./file-upload/file-upload.component";

/**
 * @description THis module is responsible for uploading the documents in specified format. This is an optional module.
 *
 * @author Shashank Agrawal
 *
 * @export
 * @class FileUploadModule
 */
@NgModule({
  declarations: [FileUploadComponent],
  imports: [
    CommonModule,
    PdfViewerModule,
    ReactiveFormsModule,
    FileUploadRoutingModule,
    SharedModule,
  ]
})
export class FileUploadModule {}
