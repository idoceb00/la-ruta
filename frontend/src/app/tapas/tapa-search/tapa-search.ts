import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { rxResource, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, map } from 'rxjs';
import { ActiveCommunityService } from '../../communities/active-community-service';
import { Loading } from '../../shared/loading/loading';
import { ErrorBox } from '../../shared/error-box/error-box';
import { Tapa } from '../tapa.model';
import { TapaService } from '../tapa-service';

@Component({
  selector: 'app-tapa-search',
  imports: [DecimalPipe, RouterLink, Loading, ErrorBox],
  templateUrl: './tapa-search.html',
  styleUrl: './tapa-search.css',
})
export class TapaSearch {
  private readonly tapaService = inject(TapaService);
  protected readonly active = inject(ActiveCommunityService);

  // What the user types, updated on every keystroke
  protected readonly query = signal('');

  // The catalog tapa the user picked; null while still searching
  protected readonly selectedTapa = signal<Tapa | null>(null);

  // What is actually sent to the API, once the user stops typing
  private readonly debouncedQuery = toSignal(
    toObservable(this.query).pipe(
      map((q) => q.trim()),
      debounceTime(300),
      distinctUntilChanged(),
    ),
    { initialValue: '' },
  );

  private readonly communityId = computed(() => this.active.activeCommunity()?.id);

  // Catalog matches: only while no tapa is selected and something is typed
  protected readonly catalog = rxResource({
    params: () => {
      if (this.selectedTapa()) {
        return undefined;
      }
      return this.debouncedQuery() || undefined;
    },
    stream: ({ params: query }) => this.tapaService.searchTapas(query),
  });

  // Bars of the active community serving the selected tapa
  protected readonly results = rxResource({
    params: () => {
      const tapa = this.selectedTapa();
      const communityId = this.communityId();
      return tapa && communityId !== undefined ? { communityId, tapaId: tapa.id } : undefined;
    },
    stream: ({ params }) => this.tapaService.getBarsWithTapa(params.communityId, params.tapaId),
  });

  protected onSearch(event: Event): void {
    this.query.set((event.target as HTMLInputElement).value);
    this.selectedTapa.set(null);
  }

  protected clearSearch(): void {
    this.query.set('');
    this.selectedTapa.set(null);
  }

  protected selectTapa(tapa: Tapa): void {
    this.selectedTapa.set(tapa);
    this.query.set(tapa.name);
  }
}