import { Component, input } from '@angular/core';
import { Bar } from '../bar.model';

@Component({
  selector: 'app-bar-card',
  imports: [],
  templateUrl: './bar-card.html',
  styleUrl: './bar-card.css'
})
export class BarCard {
  readonly bar = input.required<Bar>();
}