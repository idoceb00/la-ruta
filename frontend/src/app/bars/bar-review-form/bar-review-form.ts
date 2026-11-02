import { Component, input, linkedSignal, signal } from '@angular/core';
import { form, FormField, maxLength, pattern } from '@angular/forms/signals';
import { MyBarReview } from '../bar.model';

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
  readonly review = input.required<MyBarReview | null>();

  protected readonly editing = signal(false);

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
    this.editing.set(false);
  }

  protected onSubmit(event: Event): void {
    event.preventDefault();
  }
}

function toFormModel(review: MyBarReview | null): BarReviewFormModel {
  return {
    rating: review?.rating?.toString() ?? '',
    notes: review?.notes ?? '',
  };
}