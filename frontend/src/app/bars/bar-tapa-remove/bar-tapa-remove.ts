import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, output, signal } from '@angular/core';
import { TapaService } from '../../tapas/tapa-service';

@Component({
  selector: 'app-bar-tapa-remove',
  templateUrl: './bar-tapa-remove.html',
  styleUrl: './bar-tapa-remove.css',
})
export class BarTapaRemove {
  private readonly tapaService = inject(TapaService);

  readonly barId = input.required<number>();
  readonly tapaId = input.required<number>();
  readonly tapaName = input.required<string>();

  // Tells the parent to reload the bar detail
  readonly removed = output<void>();

  protected readonly confirming = signal(false);
  protected readonly removing = signal(false);
  protected readonly removeError = signal<string | null>(null);

  protected remove(): void {
    if (this.removing()) {
      return;
    }

    this.removing.set(true);
    this.removeError.set(null);

    this.tapaService.removeTapaFromBar(this.barId(), this.tapaId()).subscribe({
      next: () => this.removed.emit(),
      error: (error: unknown) => {
        // Someone else already removed it: the result is the same, just reload
        if (error instanceof HttpErrorResponse && error.status === 404) {
          this.removed.emit();
          return;
        }
        this.removeError.set('No se ha podido quitar la tapa. Inténtalo de nuevo.');
        this.removing.set(false);
      },
    });
  }
}