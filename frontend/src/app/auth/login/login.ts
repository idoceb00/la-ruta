import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth-service';
import { ErrorBox } from '../../shared/error-box/error-box';
import { Button } from '../../shared/button/button';
import { TextInput } from '../../shared/text-input/text-input';

@Component({
  imports: [ReactiveFormsModule, RouterLink, ErrorBox, Button, TextInput],
  selector: 'app-login',
  styleUrl: './login.css',
  templateUrl: './login.html',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => void this.router.navigate(['/']),
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(
          err.status === 401
            ? 'Usuario o contraseña incorrectos.'
            : 'No se ha podido conectar con el servidor.',
        );
        this.loading.set(false);
      },
    });
  }
}