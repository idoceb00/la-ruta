import { booleanAttribute, Component, computed, input, output } from '@angular/core';

const STARS = 5;

@Component({
  selector: 'app-star-rating',
  template: `
    <div class="flex gap-0.5" [attr.role]="readOnly() ? 'img' : 'group'" [attr.aria-label]="label()">
      @for (star of stars; track star) {
        <span class="relative inline-block size-10">
          <svg viewBox="0 0 24 24" aria-hidden="true" class="absolute inset-0 size-10 text-line">
            <path [attr.d]="path" fill="currentColor" />
          </svg>
          <span class="absolute inset-y-0 left-0 overflow-hidden" [style.width.%]="fill(star)">
            <svg viewBox="0 0 24 24" aria-hidden="true" class="size-10 text-wine">
              <path [attr.d]="path" fill="currentColor" />
            </svg>
          </span>

          @if (!readOnly()) {
            @if (half()) {
              <button
                type="button"
                (click)="select(star * 2 + 1)"
                [disabled]="disabled()"
                [attr.aria-label]="optionLabel(star * 2 + 1)"
                class="absolute inset-y-0 left-0 w-1/2 cursor-pointer disabled:cursor-not-allowed"
              ></button>
              <button
                type="button"
                (click)="select(star * 2 + 2)"
                [disabled]="disabled()"
                [attr.aria-label]="optionLabel(star * 2 + 2)"
                class="absolute inset-y-0 right-0 w-1/2 cursor-pointer disabled:cursor-not-allowed"
              ></button>
            } @else {
              <button
                type="button"
                (click)="select(star + 1)"
                [disabled]="disabled()"
                [attr.aria-label]="optionLabel(star + 1)"
                class="absolute inset-0 cursor-pointer disabled:cursor-not-allowed"
              ></button>
            }
          }
        </span>
      }
    </div>
  `,
})
export class StarRating {
  readonly value = input<number | null>(null);
  // Highest score: 10 gives half-star steps, 5 gives whole-star steps
  readonly max = input(5);
  readonly readOnly = input(false, { transform: booleanAttribute });
  readonly disabled = input(false, { transform: booleanAttribute });

  // Emits the chosen score, or null when the current one is tapped again
  readonly valueChange = output<number | null>();

  protected readonly stars = [0, 1, 2, 3, 4];
  protected readonly path =
    'M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z';

  protected readonly half = computed(() => this.max() === STARS * 2);
  private readonly pointsPerStar = computed(() => this.max() / STARS);

  protected readonly label = computed(() => {
    const value = this.value();
    return value === null ? 'Sin nota' : `${value} de ${this.max()}`;
  });

  // How much of each star is filled, from 0 to 100
  protected fill(star: number): number {
    const points = (this.value() ?? 0) - star * this.pointsPerStar();
    return Math.min(Math.max(points / this.pointsPerStar(), 0), 1) * 100;
  }

  protected optionLabel(value: number): string {
    return `Valorar con ${value} de ${this.max()}`;
  }

  protected select(value: number): void {
    this.valueChange.emit(value === this.value() ? null : value);
  }
}