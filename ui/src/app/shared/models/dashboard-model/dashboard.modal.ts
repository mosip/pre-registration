/**
 * @description This is the data object defination for the applicant coming as response in the dashboard
 * @author Shashank Agrawal
 *
 * @export
 * @interface Applicant
 */
export interface Applicant {
  applicationID: string;
  bookingType: string;
  name: string;
  appointmentDateTime: string;
  appointmentDate: string;
  appointmentTime: string;
  status: string;
  regDto: any;
  //postalCode: string;
  dataCaptureLangs: string[];
}
