import { Pipe, PipeTransform } from '@angular/core';
import { Bar } from './bar.model';

@Pipe({ 
  name: 'barLocation' 
})
export class BarLocationPipe implements PipeTransform {
  transform(bar: Pick<Bar, 'city' | 'zone'>): string {
    return [bar.city, bar.zone].filter(Boolean).join(' · ');
  }
}