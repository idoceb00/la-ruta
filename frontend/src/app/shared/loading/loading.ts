import { Component, input } from '@angular/core';

@Component({
  selector: 'app-loading',
  template: `
    <div role="status" class="flex items-center gap-2 text-muted">
      <span class="flex gap-1" aria-hidden="true">
        <span class="size-1.5 animate-bounce rounded-full bg-wine"></span>
        <span class="size-1.5 animate-bounce rounded-full bg-wine [animation-delay:150ms]"></span>
        <span class="size-1.5 animate-bounce rounded-full bg-wine [animation-delay:300ms]"></span>
      </span>
      <span class="text-sm">{{ message() }}</span>
    </div>
  `,
  host: { class: 'block' },
})
export class Loading {
  readonly message = input('Cargando…');
}