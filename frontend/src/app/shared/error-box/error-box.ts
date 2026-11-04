import { booleanAttribute, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-error-box',
  template: `
    <div role="alert" class="rounded-xs border-[1.5px] border-wine bg-surface px-4 py-3">
      <p class="text-sm font-medium text-wine">{{ message() }}</p>
      @if (retryable()) {
        <button
          type="button"
          (click)="retry.emit()"
          class="mt-2 cursor-pointer text-xs font-extrabold tracking-wide text-ink uppercase underline underline-offset-2 hover:text-wine"
        >
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