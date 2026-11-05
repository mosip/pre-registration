import { DatePipe } from "@angular/common";
import jstz from "jstz";

export class AuditModel {

    public eventName: string;
    public description: string;
    public actionUserId: string;

    constructor(
        public actionTimeStamp: any = new DatePipe("en-US").transform(new Date(),'yyyy-MM-ddTHH:mm:ss.SSS',jstz.determine().name()),
        public moduleName: string = "PREREGISTRATION_UI",
        public moduleId: string = "PREREG_UI"
    ) {}
}
