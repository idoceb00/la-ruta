import { DecimalPipe } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { BarTapaSummary } from '../bar.model';
import { BarTapaAddForm } from '../bar-tapa-add-form/bar-tapa-add-form';

@Component({
  selector: 'app-bar-tapa-list',
  imports: [DecimalPipe, BarTapaAddForm],
  templateUrl: './bar-tapa-list.html',
  styleUrl: './bar-tapa-list.css',
})
export class BarTapaList {
  readonly barId = input.required<number>();
  readonly tapas = input.required<BarTapaSummary[]>();

  // Tells the parent to reload the bar detail
  readonly changed = output<void>();
}