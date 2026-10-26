import { Component, input } from '@angular/core';

@Component({
  selector: 'app-loading',
  template: `<p class="text-stone-500">{{ message() }}</p>`,
  host: { class: 'block' },
})
export class Loading {
  readonly message = input('Cargando…');
}