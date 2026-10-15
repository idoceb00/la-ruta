import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  imports: [RouterOutlet],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('La Ruta');

  protected changeTitle(): void {
    this.title.set(this.title() === 'La Ruta' ? 'Mi ruta de tapas' : 'La Ruta')
  }
}
