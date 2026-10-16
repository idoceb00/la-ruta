import { Component, computed, signal } from '@angular/core';
import { BarCard } from '../bar-card/bar-card';
import { Bar } from '../bar.model';

@Component({
  selector: 'app-bar-list',
  imports: [BarCard],
  templateUrl: './bar-list.html',
  styleUrl: './bar-list.css'
})
export class BarList {
  protected readonly bars = signal<Bar[]>([
    { id: 1, name: 'El Rebote', city: 'León', zone: 'Barrio Húmedo', rating: 9 },
    { id: 2, name: 'La Bicha', city: 'León', zone: 'Barrio Húmedo', rating: 8 },
    { id: 3, name: 'Bar Prueba', city: 'León', zone: null, rating: null }
  ]);

  protected readonly barCount = computed(() => this.bars().length);
}