import { computed, Directive, input } from '@angular/core';

const BASE =
  'block w-full cursor-pointer rounded-xs px-4 py-3.5 text-center text-sm font-extrabold tracking-wide uppercase transition-colors disabled:cursor-not-allowed disabled:opacity-50';

const VARIANTS = {
  primary: 'bg-wine text-surface hover:bg-wine/90',
  secondary: 'border-[1.5px] border-ink bg-surface hover:bg-panel',
} as const;

export type ButtonVariant = keyof typeof VARIANTS;

@Directive({
  selector: 'button[appButton], a[appButton]',
  host: { '[class]': 'classes()' },
})
export class Button {
  readonly variant = input<ButtonVariant>('primary');

  protected readonly classes = computed(() => `${BASE} ${VARIANTS[this.variant()]}`);
}