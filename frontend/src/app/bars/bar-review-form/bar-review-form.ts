import { Component, computed, inject, input, linkedSignal, output } from '@angular/core';
import { form, FormField, maxLength } from '@angular/forms/signals';
import { Observable } from 'rxjs';
import { BarService } from '../bar-service';
import { MyBarReview } from '../bar.model';
import { toNullable } from '../../shared/to-nullable';
import { Button } from '../../shared/button/button';
import { TextInput } from '../../shared/text-input/text-input';
import { ErrorBox } from '../../shared/error-box/error-box';
import { StarRating } from '../../shared/star-rating/star-rating';

interface NotesFormModel {
  notes: string;
}

@Component({
  selector: 'app-bar-review-form',
  imports: [FormField, Button, TextInput, ErrorBox, StarRating],
  templateUrl: './bar-review-form.html',
  styleUrl: './bar-review-form.css',
})
export class BarReviewForm {
  private readonly barService = inject(BarService);

  readonly barId = input.required<number>();
  readonly review = input.required<MyBarReview | null>();

  // Tells the parent to reload the bar detail
  readonly saved = output<void>();

  // All of them reset whenever a new review arrives from the parent
  protected readonly editing = linkedSignal({ source: this.review, computation: () => false });
  protected readonly saving = linkedSignal({ source: this.review, computation: () => false });
  protected readonly saveError = linkedSignal<MyBarReview | null, string | null>({
    source: this.review,
    computation: () => null,
  });

  // Rating tapped but not confirmed by the server yet (undefined = nothing pending)
  private readonly pendingRating = linkedSignal<MyBarReview | null, number | null | undefined>({
    source: this.review,
    computation: () => undefined,
  });

  protected readonly shownRating = computed(() => {
    const pending = this.pendingRating();
    return pending !== undefined ? pending : (this.review()?.rating ?? null);
  });

  // Starts from the current notes and then follows what the user types
  private readonly model = linkedSignal<NotesFormModel>(() => ({ notes: this.review()?.notes ?? '' }));

  protected readonly notesForm = form(this.model, (path) => {
    maxLength(path.notes, 1000, { message: 'Máximo 1000 caracteres.' });
  });

  protected rate(rating: number | null): void {
    if (this.saving()) {
      return;
    }
    this.pendingRating.set(rating);
    this.send(rating, this.review()?.notes ?? null);
  }

  protected cancel(): void {
    this.model.set({ notes: this.review()?.notes ?? '' });
    this.saveError.set(null);
    this.editing.set(false);
  }

  protected onSubmit(event: Event): void {
    event.preventDefault();
    if (this.notesForm().invalid() || this.saving()) {
      return;
    }
    this.send(this.shownRating(), toNullable(this.model().notes));
  }

  private send(rating: number | null, notes: string | null): void {
    let request$: Observable<void>;

    if (rating === null && notes === null) {
      if (!this.review()) {
        // Nothing saved and nothing to save
        this.pendingRating.set(undefined);
        this.editing.set(false);
        return;
      }
      request$ = this.barService.deleteMyReview(this.barId());
    } else {
      request$ = this.barService.saveMyReview(this.barId(), { rating, notes });
    }

    this.saving.set(true);
    this.saveError.set(null);

    request$.subscribe({
      next: () => this.saved.emit(),
      error: () => {
        this.saveError.set('No se ha podido guardar tu valoración. Inténtalo de nuevo.');
        this.saving.set(false);
        this.pendingRating.set(undefined);
      },
    });
  }
}