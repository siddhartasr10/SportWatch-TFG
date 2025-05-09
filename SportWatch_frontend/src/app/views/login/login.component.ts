import { Component, WritableSignal, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

import { Router, RouterLink } from '@angular/router';
import {FormControl, ReactiveFormsModule, Validators} from '@angular/forms';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button'

import { AuthService } from '../../shared/services/auth-service/auth-service.service';
import { NavbarComponent } from '../../shared/components/navbar/navbar.component'

@Component({
  selector: 'app-login',
    imports: [NavbarComponent, RouterLink, ReactiveFormsModule, NgIf, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
    constructor(private authService: AuthService, private router : Router) {}

    username : FormControl<string | null> = new FormControl('', [Validators.required, Validators.maxLength(50)]);
    password : FormControl<string | null> = new FormControl('', [Validators.required, Validators.maxLength(72)]);

    isFormInvalid : WritableSignal<boolean> = signal(true);

    usernameErrMsg : WritableSignal<string> = signal('');
    passwordErrMsg : WritableSignal<string> = signal('');

    loginErr : WritableSignal<string> = signal('');
    // Updates global form validation state, acting as a formGroup (i don't like short form groups).
    updateFormValidity() : void {
        (this.username.invalid || this.password.invalid)
            ? this.isFormInvalid.set(true)
            : this.isFormInvalid.set(false);
    }


    // Handles all error Msgs that come from validators.
    // target: username || password
    updateErrorMsg(target : string, control : FormControl) : void {
        if (!control.errors) return;
        let msg : string = "";
        let firstErr = Object.keys(control.errors!)[0];

        (firstErr === 'required')
            ? msg = "¡Tienes que introducir un valor!"
            : msg = "¡Campo demasiado largo!";

        (target === "username")
            ? this.usernameErrMsg.set(msg)
            : this.passwordErrMsg.set(msg);

    }


    // Te registra y te lleva al feed, que por el momento no tengo implementado jajaj
    handleLogin() : void {
        this.authService.login(this.username.value!, this.password.value!).subscribe({
            next: () => this.router.navigateByUrl("/feed"),
            // when status is 401 and authManager in the backend found invalid user it won't return any message so we set one manually. (In the register authManager shouldnt never found unauthorized user, cause we create one).
            error: (body : HttpErrorResponse) => (body.status === 401) ? this.loginErr.set("Usuario o Contraseña inválidos") : this.loginErr.set(body.error.message)
        });
    }
}
