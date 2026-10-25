import { DecimalPipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { Bar } from '../bar.model';

@Component({
  selector: 'app-bar-card',
  imports: [DecimalPipe],
  templateUrl: './bar-card.html',
  styleUrl: './bar-card.css',
})
export class BarCard {
  readonly bar = input.required<Bar>();

  protected readonly location = computed(() =>
    [this.bar().city, this.bar().zone].filter(Boolean).join(' · '),
  );
}