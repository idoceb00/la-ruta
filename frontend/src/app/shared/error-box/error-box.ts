import { booleanAttribute, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-error-box',
  template: `
    <div class="rounded-xl border border-red-300 bg-red-50 p-4 text-red-700">
      <p>{{ message() }}</p>
      @if (retryable()) {
        <button type="button" (click)="retry.emit()" class="mt-2 text-sm font-medium underline">
          Reintentar
        </button>
      }
    </div>
  `,
  host: { class: 'block' },
})
export class ErrorBox {
  readonly message = input.required<string>();
  readonly retryable = input(false, { transform: booleanAttribute });
  readonly retry = output<void>();
}