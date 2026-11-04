import { DecimalPipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Bar } from '../bar.model';
import { BarLocationPipe } from '../bar-location-pipe';
import { HighlightText } from '../../shared/highlight-text/highlight-text';

@Component({
  selector: 'app-bar-card',
  imports: [DecimalPipe, RouterLink, BarLocationPipe, HighlightText],
  templateUrl: './bar-card.html',
  styleUrl: './bar-card.css',
})
export class BarCard {
  readonly bar = input.required<Bar>();
  readonly query = input('');
}