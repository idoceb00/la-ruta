import { DecimalPipe } from '@angular/common';
import { Component, input, linkedSignal, output } from '@angular/core';
import { BarTapaSummary } from '../bar.model';
import { BarTapaAddForm } from '../bar-tapa-add-form/bar-tapa-add-form';
import { TapaReviewForm } from '../tapa-review-form/tapa-review-form';

@Component({
  selector: 'app-bar-tapa-list',
  imports: [DecimalPipe, BarTapaAddForm, TapaReviewForm],
  templateUrl: './bar-tapa-list.html',
  styleUrl: './bar-tapa-list.css',
})
export class BarTapaList {
  readonly barId = input.required<number>();
  readonly tapas = input.required<BarTapaSummary[]>();

  // Tells the parent to reload the bar detail
  readonly changed = output<void>();

  // Only one row open at a time; collapses when a new list of tapas arrives
  protected readonly expandedTapaId = linkedSignal<BarTapaSummary[], number | null>({
    source: this.tapas,
    computation: () => null,
  });

  protected toggle(tapaId: number): void {
    this.expandedTapaId.update((current) => (current === tapaId ? null : tapaId));
  }
}