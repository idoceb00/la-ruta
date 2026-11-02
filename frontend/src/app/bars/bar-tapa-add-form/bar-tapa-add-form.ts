import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, linkedSignal, output } from '@angular/core';
import { rxResource, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { form, FormField, maxLength, pattern, required } from '@angular/forms/signals';
import { debounceTime, distinctUntilChanged, map } from 'rxjs';
import { BarTapaSummary } from '../bar.model';
import { Tapa } from '../../tapas/tapa.model';
import { TapaService } from '../../tapas/tapa-service';

interface AddTapaFormModel {
  name: string;
}

@Component({
  selector: 'app-bar-tapa-add-form',
  imports: [FormField],
  templateUrl: './bar-tapa-add-form.html',
  styleUrl: './bar-tapa-add-form.css',
})
export class BarTapaAddForm {
  private readonly tapaService = inject(TapaService);

  readonly barId = input.required<number>();
  // Tapas already in the bar: used to avoid duplicates and to close after reload
  readonly tapas = input.required<BarTapaSummary[]>();

  readonly added = output<void>();

  // All of them reset whenever the parent sends a new list of tapas
  protected readonly open = linkedSignal({ source: this.tapas, computation: () => false });
  protected readonly saving = linkedSignal({ source: this.tapas, computation: () => false });
  protected readonly saveError = linkedSignal<BarTapaSummary[], string | null>({
    source: this.tapas,
    computation: () => null,
  });
  private readonly model = linkedSignal<BarTapaSummary[], AddTapaFormModel>({
    source: this.tapas,
    computation: () => ({ name: '' }),
  });

  protected readonly addForm = form(this.model, (path) => {
    required(path.name, { message: 'El nombre es obligatorio.' });
    pattern(path.name, /\S/, { message: 'El nombre es obligatorio.' });
    maxLength(path.name, 100, { message: 'Máximo 100 caracteres.' });
  });

  // Same comparison as the backend: trimmed and case-insensitive
  private readonly normalizedName = computed(() => normalize(this.model().name));

  protected readonly isDuplicate = computed(() => {
    const name = this.normalizedName();
    return name !== '' && this.tapas().some((tapa) => normalize(tapa.tapaName) === name);
  });

  // What is actually searched, once the user stops typing
  private readonly debouncedName = toSignal(
    toObservable(this.normalizedName).pipe(debounceTime(300), distinctUntilChanged()),
    { initialValue: '' },
  );

  private readonly catalogResults = rxResource({
    params: () => {
      const query = this.debouncedName();
      return this.open() && query ? query : undefined;
    },
    stream: ({ params: query }) => this.tapaService.searchTapas(query),
  });

  // Catalog matches that are not in the bar yet and differ from what is typed
  protected readonly suggestions = computed(() => {
    if (!this.catalogResults.hasValue()) {
      return [];
    }
    const inBar = new Set(this.tapas().map((tapa) => tapa.tapaId));
    const typed = this.normalizedName();
    return this.catalogResults
      .value()
      .filter((tapa) => !inBar.has(tapa.id) && normalize(tapa.name) !== typed);
  });

  protected selectSuggestion(tapa: Tapa): void {
    this.model.set({ name: tapa.name });
  }

  protected cancel(): void {
    this.model.set({ name: '' });
    this.saveError.set(null);
    this.open.set(false);
  }

  protected onSubmit(event: Event): void {
    event.preventDefault();
    if (this.addForm().invalid() || this.isDuplicate() || this.saving()) {
      return;
    }

    this.saving.set(true);
    this.saveError.set(null);

    this.tapaService.addTapaToBar(this.barId(), { tapaName: this.model().name.trim() }).subscribe({
      next: () => this.added.emit(),
      error: (error: unknown) => {
        this.saveError.set(
          error instanceof HttpErrorResponse && error.status === 409
            ? 'Esta tapa ya está en el bar.'
            : 'No se ha podido añadir la tapa. Inténtalo de nuevo.',
        );
        this.saving.set(false);
      },
    });
  }
}

function normalize(value: string): string {
  return value.trim().toLowerCase();
}