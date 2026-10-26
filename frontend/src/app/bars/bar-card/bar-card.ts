import { DecimalPipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { Bar } from '../bar.model';
import { RouterLink } from '@angular/router';
import { BarLocationPipe } from '../bar-location-pipe';

@Component({
  selector: 'app-bar-card',
  imports: [DecimalPipe, RouterLink, BarLocationPipe],
  templateUrl: './bar-card.html',
  styleUrl: './bar-card.css',
})
export class BarCard {
  readonly bar = input.required<Bar>();
}