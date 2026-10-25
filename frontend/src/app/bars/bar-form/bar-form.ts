import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ActiveCommunityService } from '../../communities/active-community-service';
import { BarRequest } from '../bar.model';
import { BarService } from '../bar-service';

@Component({
  selector: 'app-bar-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './bar-form.html',
  styleUrl: './bar-form.css',
})
export class BarForm {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly barService = inject(BarService);
  private readonly router = inject(Router);
  protected readonly active = inject(ActiveCommunityService);

  protected readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
    city: ['', Validators.maxLength(255)],
    address: ['', Validators.maxLength(255)],
    zone: ['', Validators.maxLength(255)],
  });

  protected readonly saving = signal(false);
  protected readonly saveError = signal<string | null>(null);

  protected submit(): void {
    const community = this.active.activeCommunity();

    if (this.form.invalid || !community || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.saveError.set(null);

    const { name, city, address, zone } = this.form.getRawValue();
    const request: BarRequest = {
      name: name.trim(),
      city: toNullable(city),
      address: toNullable(address),
      zone: toNullable(zone),
    };

    this.barService.createBar(community.id, request).subscribe({
      next: () => {
        void this.router.navigate(['/']);
      },
      error: () => {
        this.saveError.set('No se ha podido guardar el bar. Inténtalo de nuevo.');
        this.saving.set(false);
      },
    });
  }
}

function toNullable(value: string): string | null {
  const trimmed = value.trim();
  return trimmed ? trimmed : null;
}