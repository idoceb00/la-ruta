import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { ActiveCommunityService } from '../../communities/active-community-service';
import { Bar, BarRequest } from '../bar.model';
import { BarService } from '../bar-service';

@Component({
  selector: 'app-bar-form',
  imports: [ReactiveFormsModule, RouterLink, NgTemplateOutlet],
  templateUrl: './bar-form.html',
  styleUrl: './bar-form.css',
})
export class BarForm {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly barService = inject(BarService);
  private readonly router = inject(Router);
  protected readonly active = inject(ActiveCommunityService);

  // Present only on /bars/:id/edit
  readonly id = input<number | null, string | undefined>(null, {
    transform: (value) => (value ? Number(value) : null),
  });

  protected readonly isEdit = computed(() => this.id() !== null);

  protected readonly bar = rxResource({
    params: () => this.id() ?? undefined,
    stream: ({ params: id }) => this.barService.getBar(id),
  });

  protected readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
    city: ['', Validators.maxLength(255)],
    address: ['', Validators.maxLength(255)],
    zone: ['', Validators.maxLength(255)],
  });

  protected readonly saving = signal(false);
  protected readonly saveError = signal<string | null>(null);

  constructor() {
    // Fill the form once the bar to edit has loaded
    effect(() => {
      const bar = this.bar.value();
      if (bar) {
        this.form.reset({
          name: bar.name,
          city: bar.city ?? '',
          address: bar.address ?? '',
          zone: bar.zone ?? '',
        });
      }
    });
  }

  protected submit(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const { name, city, address, zone } = this.form.getRawValue();
    const request: BarRequest = {
      name: name.trim(),
      city: toNullable(city),
      address: toNullable(address),
      zone: toNullable(zone),
    };

    const request$ = this.save(request);
    if (!request$) {
      return;
    }

    this.saving.set(true);
    this.saveError.set(null);

    request$.subscribe({
      next: (bar) => {
        void this.router.navigate(this.isEdit() ? ['/bars', bar.id] : ['/']);
      },
      error: () => {
        this.saveError.set('No se ha podido guardar el bar. Inténtalo de nuevo.');
        this.saving.set(false);
      },
    });
  }

  private save(request: BarRequest): Observable<Bar> | null {
    const id = this.id();
    if (id !== null) {
      return this.barService.updateBar(id, request);
    }
    const community = this.active.activeCommunity();
    return community ? this.barService.createBar(community.id, request) : null;
  }
}

function toNullable(value: string): string | null {
  const trimmed = value.trim();
  return trimmed ? trimmed : null;
}