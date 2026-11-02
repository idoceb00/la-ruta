import { DecimalPipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { BarTapaSummary } from '../bar.model';

@Component({
  selector: 'app-bar-tapa-list',
  imports: [DecimalPipe],
  templateUrl: './bar-tapa-list.html',
  styleUrl: './bar-tapa-list.css',
})
export class BarTapaList {
  readonly tapas = input.required<BarTapaSummary[]>();
}