import { Component, computed, inject, input, linkedSignal, output, signal } from '@angular/core';
import { form, FormField, maxLength } from '@angular/forms/signals';
import { Observable } from 'rxjs';
import { MyTapaReview } from '../bar.model';
import { TapaReviewRequest } from '../../tapas/tapa.model';
import { TapaService } from '../../tapas/tapa-service';
import { toNullable } from '../../shared/to-nullable';
import { Button } from '../../shared/button/button';
import { TextInput } from '../../shared/text-input/text-input';
import { ErrorBox } from '../../shared/error-box/error-box';
import { StarRating } from '../../shared/star-rating/star-rating';

interface TapaReviewFormModel {
  rating: number | null;
  notes: string;
  fav: boolean;
}

@Component({
  selector: 'app-tapa-review-form',
  imports: [FormField, Button, TextInput, ErrorBox, StarRating],
  templateUrl: './tapa-review-form.html',
  styleUrl: './tapa-review-form.css',
})
export class TapaReviewForm {
  private readonly tapaService = inject(TapaService);

  readonly barId = input.required<number>();
  readonly tapaId = input.required<number>();
  readonly review = input.required<MyTapaReview | null>();

  // Tells the parent to reload the bar detail
  readonly saved = output<void>();
  // Tells the parent to collapse the row
  readonly cancelled = output<void>();

  protected readonly saving = signal(false);
  protected readonly saveError = signal<string | null>(null);

  private readonly model = linkedSignal(() => toFormModel(this.review()));

  protected readonly reviewForm = form(this.model, (path) => {
    maxLength(path.notes, 1000, { message: 'Máximo 1000 caracteres.' });
  });

  protected readonly rating = computed(() => this.model().rating);

  protected setRating(rating: number | null): void {
    this.model.update((current) => ({ ...current, rating }));
  }

  protected onSubmit(event: Event): void {
    event.preventDefault();
    if (this.reviewForm().invalid() || this.saving()) {
      return;
    }

    const request$ = this.buildRequest();
    if (!request$) {
      // Empty form and no previous review: nothing to save or delete
      this.cancelled.emit();
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
    const { rating, notes, fav } = this.model();

    const request: TapaReviewRequest = {
      rating,
      notes: toNullable(notes),
      fav,
    };

    if (request.rating === null && request.notes === null && !request.fav) {
      return this.review()
        ? this.tapaService.deleteMyTapaReview(this.barId(), this.tapaId())
        : null;
    }
    return this.tapaService.saveMyTapaReview(this.barId(), this.tapaId(), request);
  }
}

function toFormModel(review: MyTapaReview | null): TapaReviewFormModel {
  return {
    rating: review?.rating ?? null,
    notes: review?.notes ?? '',
    fav: review?.fav ?? false,
  };
}