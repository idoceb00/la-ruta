import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, input, numberAttribute, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { BarService } from '../bar-service';
import { BarLocationPipe } from '../bar-location-pipe';
import { Loading } from '../../shared/loading/loading';
import { ErrorBox } from '../../shared/error-box/error-box';

@Component({
  selector: 'app-bar-detail',
  imports: [DecimalPipe, RouterLink, BarLocationPipe, Loading, ErrorBox],
  templateUrl: './bar-detail.html',
  styleUrl: './bar-detail.css',
})
export class BarDetail {
  private readonly barService = inject(BarService);
  private readonly router = inject(Router);

  readonly id = input.required({ transform: numberAttribute });

  protected readonly bar = rxResource({
    params: () => this.id(),
    stream: ({ params: id }) => this.barService.getBar(id),
  });

  protected readonly confirmingDelete = signal(false);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  protected deleteBar(): void {
    if (this.deleting()) {
      return;
    }

    this.deleting.set(true);
    this.deleteError.set(null);

    this.barService.deleteBar(this.id()).subscribe({
      next: () => {
        void this.router.navigate(['/']);
      },
      error: () => {
        this.deleteError.set('No se ha podido eliminar el bar. Inténtalo de nuevo.');
        this.deleting.set(false);
      },
    });
  }
}