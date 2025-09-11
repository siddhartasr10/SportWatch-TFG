import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AuthService } from '../../services/auth-service/auth-service.service';

@Injectable({
  providedIn: 'root',
})
export class LoggedGuard implements CanActivate {
    // default .msg value of the call to api/check-user when username is invalid:
    private invalidUsernameMsg: string = "Invalid user token, no username could be found or token wasn't signed or expired";
  constructor(
    private authService: AuthService,
    private router: Router) {}

  canActivate(): Observable<boolean> {
      return this.authService.checkUser().pipe(map(usernameOrError => {
          console.log("Mensajillo: ", usernameOrError);
          if (usernameOrError['msg'] == this.invalidUsernameMsg) {
              this.router.navigate(["/", "login"]);
              return false;
          }
          return true;
      }));
  }
}
