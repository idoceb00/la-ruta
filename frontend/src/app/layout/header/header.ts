import { Component, signal } from '@angular/core';

@Component({
  imports: [],
  selector: 'app-header',
  styleUrl: './header.css',
  templateUrl: './header.html',
})
export class Header {
  protected readonly title = signal('La Ruta');

  protected changeTitle(): void {
    this.title.set(this.title() === 'La Ruta' ? 'Mi ruta de tapas' : 'La Ruta')
  }
}
