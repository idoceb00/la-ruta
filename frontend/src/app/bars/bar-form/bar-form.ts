import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, inject, input, linkedSignal, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { form, FormField, maxLength, pattern, required } from '@angular/forms/signals';
import { Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { ActiveCommunityService } from '../../communities/active-community-service';
import { Bar, BarRequest } from '../bar.model';
import { BarService } from '../bar-service';

interface BarFormModel {
  name: string;
  city: string;
  address: string;
  zone: string;
}

@Component({
  selector: 'app-bar-form',
  imports: [FormField, RouterLink, NgTemplateOutlet],
  templateUrl: './bar-form.html',
  styleUrl: './bar-form.css',
})
export class BarForm {
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

  // Starts from the loaded bar (or empty) and then follows what the user types
  private readonly model = linkedSignal(() => toFormModel(this.bar.value()));

  protected readonly barForm = form(this.model, (path) => {
    required(path.name, { message: 'El nombre es obligatorio.' });
    pattern(path.name, /\S/, { message: 'El nombre es obligatorio.' });
    maxLength(path.name, 100, { message: 'Máximo 100 caracteres.' });
    maxLength(path.city, 255, { message: 'Máximo 255 caracteres.' });
    maxLength(path.address, 255, { message: 'Máximo 255 caracteres.' });
    maxLength(path.zone, 255, { message: 'Máximo 255 caracteres.' });
  });

  protected readonly saving = signal(false);
  protected readonly saveError = signal<string | null>(null);

  protected onSubmit(event: Event): void {
    event.preventDefault();
    if (this.barForm().invalid() || this.saving()) {
      return;
    }

    const { name, city, address, zone } = this.model();
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

function toFormModel(bar: Bar | undefined): BarFormModel {
  return {
    name: bar?.name ?? '',
    city: bar?.city ?? '',
    address: bar?.address ?? '',
    zone: bar?.zone ?? '',
  };
}

function toNullable(value: string): string | null {
  const trimmed = value.trim();
  return trimmed ? trimmed : null;
}