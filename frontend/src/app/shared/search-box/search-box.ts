import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-search-box',
  template: `
    <div class="relative">
      <svg
        aria-hidden="true"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2.5"
        class="pointer-events-none absolute top-1/2 left-4 size-5 -translate-y-1/2 text-muted"
      >
        <circle cx="11" cy="11" r="7" />
        <path d="m20 20-4-4" />
      </svg>
      <input
        type="search"
        [value]="value()"
        (input)="onInput($event)"
        (keydown.escape)="clear()"
        [placeholder]="placeholder()"
        [attr.aria-label]="label()"
        enterkeyhint="search"
        autocomplete="off"
        class="w-full rounded-xs border-2 border-ink bg-surface py-3.5 pr-11 pl-12 text-lg placeholder:text-muted focus:border-wine focus:outline-none [&::-webkit-search-cancel-button]:appearance-none"
      />
      @if (value()) {
        <button
          type="button"
          (click)="clear()"
          aria-label="Borrar búsqueda"
          class="absolute top-1/2 right-3 -translate-y-1/2 cursor-pointer px-1 text-muted hover:text-ink"
        >
          ✕
        </button>
      }
    </div>
  `,
  host: { class: 'block' },
})
export class SearchBox {
  readonly value = input('');
  readonly placeholder = input('');
  readonly label = input.required<string>();

  readonly valueChange = output<string>();

  protected onInput(event: Event): void {
    this.valueChange.emit((event.target as HTMLInputElement).value);
  }

  protected clear(): void {
    this.valueChange.emit('');
  }
}