import { Component, signal, WritableSignal } from '@angular/core';
import { NgIf } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

import { Router, RouterLink } from '@angular/router';
import { AbstractControl, FormControl, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button'

import { AuthService } from '../../shared/services/auth-service/auth-service.service';
import { NavbarComponent } from '../../shared/components/navbar/navbar.component';

@Component({
  selector: 'app-register',
  imports: [NavbarComponent, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, ReactiveFormsModule, NgIf],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
    constructor(private authService: AuthService, private router : Router) {}

    username : FormControl<string | null> = new FormControl('', [Validators.required, Validators.maxLength(50), Validators.minLength(3)]);
    password : FormControl<string | null> = new FormControl('', [Validators.required, Validators.maxLength(72), Validators.minLength(7)]);
    verifyPassword : FormControl<string | null> = new FormControl('', [Validators.required, this.verifierValidator(this.password)]);
    email : FormControl<string | null> = new FormControl('', [Validators.required, Validators.maxLength(400), Validators.email]);

    // I don't want to use FormGroup cause i don't want to use form, as I won't use them for nothing.
    isFormInvalid : WritableSignal<boolean> = signal(true);

    usernameErrMsg : WritableSignal<string> = signal('');
    passwordErrMsg : WritableSignal<string> = signal('');
    verifyPasswordErrMsg: WritableSignal<string> = signal('');
    emailErrMsg : WritableSignal<string> = signal('');


    registrationErr : WritableSignal<string> = signal('');

    /** Validator that checks if a field is equal to a specific field */
    verifierValidator(fieldTargeted : AbstractControl): ValidatorFn {
      return (control: AbstractControl): ValidationErrors | null => {
          const isVerified = fieldTargeted.value === control.value;
          return isVerified ? null : {wrongverification: {value: control.value, comparedValue: fieldTargeted.value}};
      };
    }

    // Updates global form validation state, acting as a formGroup (i don't like short form groups).
    updateFormValidity() : void {
        (this.username.invalid || this.password.invalid || this.verifyPassword.invalid || this.email.invalid)
            ? this.isFormInvalid.set(true)
            : this.isFormInvalid.set(false);
    }


    // Handles all error Msgs that come from validators.
    // target: username || password || verifyPassword || email
    updateErrorMsg(target : string, control : FormControl) : void {
        if (!control.errors) return;
        let msg : string = "";
        let firstErr = Object.keys(control.errors!)[0];

        switch (firstErr) {
            case 'required':
                msg = "¡Tienes que introducir un valor!";
                break;
            case 'minlength':
                msg = "¡Longitud del campo insuficiente!";
                break;
            case 'maxlength':
                msg = "¡Campo demasiado largo!";
                break;
            case 'email':
                msg = "¡Email inválido!";
                break;
            case 'wrongverification':
                msg = "¡La contraseña que has introducido no es igual a la anterior!";
        }

        switch (target) {
            case "username":
                this.usernameErrMsg.set(msg);
                break;

            case "password":
                this.passwordErrMsg.set(msg);
                break;

            case "email":
                this.emailErrMsg.set(msg);
                break;

            case "verifyPassword":
                this.verifyPasswordErrMsg.set(msg);
                break;
        }
    }

    // Synchronizes verifyPassword validation state even if not focused or inputted.
    // Even if a validator checks that password and verifyPassword arent equal, if verifyPassword isn't being touched validator won't trigger.
    // (validators cannot act directly on other form controls, this is why this func exists)
    passwordControlSynchronizer() : void {
        this.updateErrorMsg('verifyPassword', this.verifyPassword);

        (this.password.value === this.verifyPassword.value && this.verifyPassword.value !== "")
            ? this.verifyPassword.setErrors(null)
            : this.verifyPassword.setErrors({...this.verifyPassword.errors, wrongverification: true});
        // I check that the verifyPassword control is not empty to avoid errors setting to null when both fields are empty.
        //
        this.updateFormValidity();
    }


    // Te registra y te lleva al feed, que por el momento no tengo implementado jajaj
    handleRegister() : void {
        if (this.isFormInvalid()) return;
        this.authService.register(this.username.value!, this.password.value!, this.email.value!).subscribe({
            next: () => this.router.navigateByUrl("/feed"),
            error: (body : HttpErrorResponse) => this.registrationErr.set(body.error.message)

        });
    }

}
