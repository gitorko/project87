import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Ticket} from '../models/ticket';
import {BookingRequest} from '../models/booking-request';

@Injectable({
  providedIn: 'root'
})
export class RestService {

  constructor(private http: HttpClient) {
  }

  public getTickets(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>('/api/tickets');
  }

  public bookTicket(bookingRequest: BookingRequest): Observable<any> {
    return this.http.post('/api/ticket', bookingRequest);
  }

  public holdBooking(bookingRequest: BookingRequest): Observable<any> {
    return this.http.post('/api/hold', bookingRequest);
  }

  public cancelBooking(bookingRequest: BookingRequest): Observable<any> {
    return this.http.post('/api/cancel', bookingRequest);
  }

  public getUser(): Observable<string> {
    return this.http.get<string>('/api/user', {responseType: 'text' as 'json'});
  }

}
