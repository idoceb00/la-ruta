import { DecimalPipe } from '@angular/common';
import { Component, inject, input, linkedSignal, output } from '@angular/core';
import { BarTapaSummary } from '../bar.model';
import { BarTapaAddForm } from '../bar-tapa-add-form/bar-tapa-add-form';
import { BarTapaRemove } from '../bar-tapa-remove/bar-tapa-remove';
import { TapaReviewForm } from '../tapa-review-form/tapa-review-form';
import { TapaReviewRequest } from '../../tapas/tapa.model';
import { TapaService } from '../../tapas/tapa-service';
import { toNullable } from '../../shared/to-nullable';

interface PendingFav {
  tapaId: number;
  fav: boolean;
}

@Component({
  selector: 'app-bar-tapa-list',
  imports: [DecimalPipe, BarTapaAddForm, TapaReviewForm, BarTapaRemove],
  templateUrl: './bar-tapa-list.html',
  styleUrl: './bar-tapa-list.css',
})
export class BarTapaList {
  private readonly tapaService = inject(TapaService);

  readonly barId = input.required<number>();
  readonly tapas = input.required<BarTapaSummary[]>();

  // Tells the parent to reload the bar detail
  readonly changed = output<void>();

  // Only one row open at a time; collapses when a new list of tapas arrives
  protected readonly expandedTapaId = linkedSignal<BarTapaSummary[], number | null>({
    source: this.tapas,
    computation: () => null,
  });

  // Favourite change in flight, shown before the server confirms it
  protected readonly pendingFav = linkedSignal<BarTapaSummary[], PendingFav | null>({
    source: this.tapas,
    computation: () => null,
  });

  protected readonly favErrorTapaId = linkedSignal<BarTapaSummary[], number | null>({
    source: this.tapas,
    computation: () => null,
  });

  protected toggle(tapaId: number): void {
    this.expandedTapaId.update((current) => (current === tapaId ? null : tapaId));
  }

  protected isFav(tapa: BarTapaSummary): boolean {
    const pending = this.pendingFav();
    return pending?.tapaId === tapa.tapaId ? pending.fav : (tapa.myReview?.fav ?? false);
  }

  protected toggleFav(tapa: BarTapaSummary): void {
    if (this.pendingFav()) {
      return;
    }

    const review = tapa.myReview;
    const request: TapaReviewRequest = {
      rating: review?.rating ?? null,
      notes: toNullable(review?.notes ?? ''),
      fav: !(review?.fav ?? false),
    };

    // Unmarking the only thing left in the review would leave it empty: delete it instead
    const request$ =
      !request.fav && request.rating === null && request.notes === null
        ? this.tapaService.deleteMyTapaReview(this.barId(), tapa.tapaId)
        : this.tapaService.saveMyTapaReview(this.barId(), tapa.tapaId, request);

    this.pendingFav.set({ tapaId: tapa.tapaId, fav: request.fav });
    this.favErrorTapaId.set(null);

    request$.subscribe({
      next: () => this.changed.emit(),
      error: () => {
        this.pendingFav.set(null);
        this.favErrorTapaId.set(tapa.tapaId);
      },
    });
  }
}