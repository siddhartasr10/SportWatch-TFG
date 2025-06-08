import { NgIf } from '@angular/common';
import { Component, Input, WritableSignal, signal } from '@angular/core';
import { AbstractControl, FormControl, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';

import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';

import { UploadService } from '../../services/upload-service/upload-service.service';
import { SportCategory } from '../../interfaces/categories';
import { IvsChannelInfo } from '../../interfaces/IvsChannelInfo';

@Component({
  selector: 'app-upload-form',
  imports: [MatButtonModule, MatFormFieldModule, MatInput, ReactiveFormsModule, NgIf],
  templateUrl: './upload-form.component.html',
  styleUrl: './upload-form.component.css'
})
export class UploadFormComponent {
    constructor(private uploadService: UploadService) {}
    // Signal significa que no tengo que hacer change detector ref. :)
    title : FormControl<string | null> = new FormControl('', [Validators.required, Validators.maxLength(32), Validators.minLength(3)]);
    category: FormControl<string | null> = new FormControl('', [Validators.required, Validators.maxLength(30), Validators.minLength(2), this.categoryValidator()]);
    description: FormControl<string | null> = new FormControl('', [Validators.maxLength(512)]);

    isFormInvalid: boolean = true;

    titleErrMsg: WritableSignal<string> = signal('');
    categoryErrMsg: WritableSignal<string> = signal('');

    isRequestingChannelInfo : boolean = false;
    channelInfo : IvsChannelInfo | null = null;

    categoryValidator(): ValidatorFn {
      return (control: AbstractControl): ValidationErrors | null => {
          const isVerified = Object.values(SportCategory).find((category) => category.toLowerCase() === control.value.toLowerCase()) !== undefined;
          return isVerified ? null : {invalidcategory: {value: control.value, validCategories: SportCategory}};
      };
    }


    handleUpload() : void | boolean {
        if (this.isFormInvalid) return;

        this.isRequestingChannelInfo = true;
        console.log(this.isRequestingChannelInfo);

        if (this.title.value === null)
            return this.isFormInvalid = true;
        console.log({title: this.title.value!, category: this.category.value, description: this.description.value});

        // I don't need stream service, db entry gets created by the
        // backend in its uploadService.
        this.uploadService.requestChannel({title: this.title.value!, category: this.category.value, description: this.description.value})
            .subscribe((channel : IvsChannelInfo) => this.channelInfo = channel);
    }


    updateFormValidity() : void {
        this.isFormInvalid = (this.title.invalid || this.category.invalid)
        ? true
        : false;
    }


    // target here can be title | category
    updateErrorMsg(target : string , control : FormControl) : void {
        if (!control.errors) return;
        let msg : string = "";
        let firstErr = Object.keys(control.errors!)[0];

        // Se que un enum hace esto mejor, no me lo restriegues.
        if (firstErr === 'required')
            msg = "¡Tienes que introducir un valor!"
        if (firstErr === 'minlength')
            msg = "¡Campo demasiado corto!";
        if (firstErr === 'maxlength')
            msg = "¡Campo demasiado largo!";
        if (firstErr === 'invalidcategory')
            msg = "¡Ese deporte no existe!";


        (target === "title")
            ? this.titleErrMsg.set(msg)
            : this.categoryErrMsg.set(msg);

    }

    onModalClick(ev : Event) : void {
        ev.stopPropagation();
    }

}
