import {Component} from '@angular/core';

export enum AlertType {
  SUCCESS,
  WARNING,
  ERROR,
  INFORMATION
}

@Component({
  selector: 'app-alert',
  templateUrl: './alert.component.html',
  styleUrls: ['alert.component.scss']
})
export class AlertComponent {

  public openAlert = false;
  public alertMessage = '';
  public alertType = '';

  constructor() {
  }

  public closeAlert(): void {
    // clear alert
    this.alertMessage = '';
    this.alertType = '';
    this.openAlert = false;
  }

  public showMessage(msg: string, alertType: AlertType): void {
    if (msg) {
      this.alertMessage = msg;
      switch (alertType) {
        case AlertType.SUCCESS:
          this.alertType = 'success';
          break;
        case AlertType.WARNING:
          this.alertType = 'warning';
          break;
        case AlertType.ERROR:
          this.alertType = 'danger';
          break;
        case AlertType.INFORMATION:
        default:
          this.alertType = 'info';
          break;
      }
      this.openAlert = true;

      if (alertType === AlertType.SUCCESS) { // Auto close success messages
        setTimeout(
          () => {
            this.closeAlert();
          },
          10000
        );
      }
    } else {
      this.closeAlert();
    }
  }

  public showError(error: any | string): void {
    if (error && error.status && error.status === 404) {
      this.showMessage('Network or connectivity issue. Please try again after sometime', AlertType.ERROR);
    } else if (error && error.status && error.status === 502) {
      this.showMessage('Could not get a response from the API. Please try again after sometime', AlertType.ERROR);
    } else if (error && error.error && error.error.message) {
      this.showMessage(error.error.message, AlertType.ERROR);
    } else if (error && typeof error === 'object' && error.message) {
      this.showMessage(error.message, AlertType.ERROR);
    } else {
      this.showMessage(error, AlertType.ERROR);
    }
  }

  public showSuccess(msg: string): void {
    this.showMessage(msg, AlertType.SUCCESS);
  }

  public showWarning(msg: string): void {
    this.showMessage(msg, AlertType.WARNING);
  }

  public showInformation(msg: string): void {
    this.showMessage(msg, AlertType.INFORMATION);
  }

  public onAlertClosedChange(closed: boolean): void {
    this.openAlert = !closed;
  }
}
