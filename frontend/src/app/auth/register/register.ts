import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule,       
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms'
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink} from '@angular/router';
import { AuthService } from '../auth-service';

const passwordsMatch: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const password = group.get('password')?.value;
  const confirmPassword = group.get('confirmPassword')?.value;
  return password === confirmPassword ? null : { passwordsMismatch: true};
};

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  selector: 'app-register',
  styleUrl: './register.css',
  templateUrl: './register.html',
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group(
    {
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
      password: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(72)]],
      confirmPassword: ['', Validators.required]
    },
    { validators: passwordsMatch }
  );

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const { username, password } = this.form.getRawValue();

    this.authService.register({ username, password }).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(this.messageFor(err.status));
        this.loading.set(false);
      }
    });
  }

  private messageFor(status: number): string {
    switch (status) {
      case 409: return 'Ese nombre de usuario ya existe.';
      case 400: return 'Revisa los datos introducidos.';
      default: return 'No se ha podido conectar con el servidor.';
    }
  }
}
