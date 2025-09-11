import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../services/auth-service/auth-service.service';
import { map } from 'rxjs';

// Opposite to logged guard this one checks if you aren't logged and redirects you to the feed if you're
// Could do this in the other guard but this is cleaner i think.
export const AlreadyLoggedGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    // default .msg value of the call to api/check-user when username is invalid:
    const invalidUsernameMsg: string = "Invalid user token, no username could be found or token wasn't signed or expired";

    return authService.checkUser().pipe(map(usernameOrError => {
          if (usernameOrError['msg'] == invalidUsernameMsg) {
              return true;
          }
          router.navigate(["/", "feed"]);
          return false;
      }));
};
