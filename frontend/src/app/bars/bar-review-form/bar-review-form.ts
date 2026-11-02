import { Component, inject, input, linkedSignal, output } from '@angular/core';
import { form, FormField, maxLength, pattern } from '@angular/forms/signals';
import { Observable } from 'rxjs';
import { BarService } from '../bar-service';
import { MyBarReview } from '../bar.model';
import { toNullable } from '../../shared/to-nullable';

interface BarReviewFormModel {
  rating: string;
  notes: string;
}

@Component({
  selector: 'app-bar-review-form',
  imports: [FormField],
  templateUrl: './bar-review-form.html',
  styleUrl: './bar-review-form.css',
})
export class BarReviewForm {
  private readonly barService = inject(BarService);

  readonly barId = input.required<number>();
  readonly review = input.required<MyBarReview | null>();

  // Tells the parent to reload the bar detail
  readonly saved = output<void>();

  // Both reset to false whenever a new review arrives from the parent
  protected readonly editing = linkedSignal({ source: this.review, computation: () => false });
  protected readonly saving = linkedSignal({ source: this.review, computation: () => false });

  protected readonly saveError = linkedSignal<MyBarReview | null, string | null>({
    source: this.review,
    computation: () => null,
  });

  // Starts from the current review and then follows what the user types
  private readonly model = linkedSignal(() => toFormModel(this.review()));

  protected readonly reviewForm = form(this.model, (path) => {
    pattern(path.rating, /^\s*(?:[0-9]|10)\s*$/, {
      message: 'La nota debe ser un número entero del 0 al 10.',
    });
    maxLength(path.notes, 1000, { message: 'Máximo 1000 caracteres.' });
  });

  protected cancel(): void {
    this.model.set(toFormModel(this.review()));
    this.saveError.set(null);
    this.editing.set(false);
  }

  protected onSubmit(event: Event): void {
    event.preventDefault();
    if (this.reviewForm().invalid() || this.saving()) {
      return;
    }

    const request$ = this.buildRequest();
    if (!request$) {
      // Empty form and no previous review: nothing to save or delete
      this.editing.set(false);
      return;
    }

    this.saving.set(true);
    this.saveError.set(null);

    request$.subscribe({
      next: () => this.saved.emit(),
      error: () => {
        this.saveError.set('No se ha podido guardar tu valoración. Inténtalo de nuevo.');
        this.saving.set(false);
      },
    });
  }

  private buildRequest(): Observable<void> | null {
    const { rating, notes } = this.model();
    const trimmedRating = rating.trim();

    const request = {
      rating: trimmedRating === '' ? null : Number(trimmedRating),
      notes: toNullable(notes),
    };

    if (request.rating === null && request.notes === null) {
      return this.review() ? this.barService.deleteMyReview(this.barId()) : null;
    }
    return this.barService.saveMyReview(this.barId(), request);
  }
}

function toFormModel(review: MyBarReview | null): BarReviewFormModel {
  return {
    rating: review?.rating?.toString() ?? '',
    notes: review?.notes ?? '',
  };
}