import {Component, OnInit, ViewChild} from '@angular/core';
import {Ticket} from '../../models/ticket';
import {RestService} from '../../services/rest.service';
import {Router} from '@angular/router';
import {ClarityIcons, trashIcon} from '@cds/core/icon';
import {AlertComponent} from '../alert/alert.component';
import {BookingRequest} from '../../models/booking-request';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: []
})
export class HomeComponent implements OnInit {

  tickets: Ticket[] = [];
  ticket: Ticket = new Ticket();
  selected: Ticket[] = [];
  // @ts-ignore
  @ViewChild(AlertComponent, {static: true}) private alert: AlertComponent;
  payModal = false;

  constructor(private restService: RestService, private router: Router) {
    ClarityIcons.addIcons(trashIcon);
  }

  ngOnInit(): void {
    this.getTickets();
  }

  getTickets(): void {
    this.ticket = new Ticket();
    this.restService.getTickets().subscribe(data => {
      this.tickets = data;
    });
  }

  holdBooking(): void {
    const request = new BookingRequest();
    request.ticketIds = [];
    request.user = sessionStorage.getItem('user');
    this.selected.forEach(item => {
      request.ticketIds.push(Number(item.id));
    });
    this.restService.holdBooking(request)
      .subscribe(data => {
          if (data) {
            this.payModal = true;
          } else {
            this.alert.showError('Ticket is already reserved, Try again!');
            this.getTickets();
          }
        },
        error => {
          this.alert.showError('Ticket is already reserved, Try again!');
          this.getTickets();
        });

  }

  cancelBooking(): void {
    const request = new BookingRequest();
    request.ticketIds = [];
    request.user = sessionStorage.getItem('user');
    this.selected.forEach(item => {
      request.ticketIds.push(Number(item.id));
    });
    this.restService.cancelBooking(request)
      .subscribe(data => {
        this.payModal = false;
      });
  }

  confirmBooking(): void {
    const request = new BookingRequest();
    request.ticketIds = [];
    request.user = sessionStorage.getItem('user');
    this.selected.forEach(item => {
      request.ticketIds.push(Number(item.id));
    });
    this.restService.bookTicket(request)
      .subscribe(data => {
        if (data) {
          this.alert.showSuccess('Ticket booked successfully!');
        } else {
          this.alert.showError('Ticket booking failed!');
        }
        this.payModal = false;
        this.getTickets();
      });
  }

  getTotal(): number {
    let sum = 0;
    this.selected.forEach(item => {
      // @ts-ignore
      sum += item.price;
    });
    return sum;
  }

  getSeatStatus(ticket: Ticket): string {
    // @ts-ignore
    if (ticket.lockedBy !== '') {
      return 'RESERVED';
    }
    if (ticket.booked) {
      return 'BOOKED';
    } else {
      return 'AVAILABLE';
    }
  }

}
