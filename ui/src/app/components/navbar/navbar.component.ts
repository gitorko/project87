import {Component, OnInit} from '@angular/core';
import {ClarityIcons, userIcon} from '@cds/core/icon';
import {RestService} from '../../services/rest.service';
import {Router} from "@angular/router";

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html'
})
export class NavbarComponent implements OnInit {

  user: string | null;

  constructor(private restService: RestService, private router: Router) {
    this.user = null;
    ClarityIcons.addIcons(userIcon);
  }

  ngOnInit(): void {
    const token = sessionStorage.getItem('user');
    this.user = token;
    if (token === '' || token === null) {
      this.restService.getUser()
        .subscribe(data => {
          this.user = data;
          sessionStorage.setItem('user', data);
        }, error => {
          this.user = 'fail';
        });
    }
  }

  logout(): void {
    console.log('logout!');
    sessionStorage.removeItem('user');
    this.router.navigate(['/']).then(() => {
      window.location.reload();
    });
  }

}
