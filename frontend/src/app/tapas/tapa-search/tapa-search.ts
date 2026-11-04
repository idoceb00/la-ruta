import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { rxResource, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { debounce, distinctUntilChanged, map, timer } from 'rxjs';
import { ActiveCommunityService } from '../../communities/active-community-service';
import { Loading } from '../../shared/loading/loading';
import { ErrorBox } from '../../shared/error-box/error-box';
import { Button } from '../../shared/button/button';
import { SearchBox } from '../../shared/search-box/search-box';
import { HighlightText } from '../../shared/highlight-text/highlight-text';
import { Tapa } from '../tapa.model';
import { TapaService } from '../tapa-service';

@Component({
  selector: 'app-tapa-search',
  imports: [DecimalPipe, RouterLink, Loading, ErrorBox, Button, SearchBox, HighlightText],
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

  // What is actually sent to the API: waits while typing, but clears instantly
  protected readonly debouncedQuery = toSignal(
    toObservable(this.query).pipe(
      map((q) => q.trim()),
      debounce((q) => timer(q ? 300 : 0)),
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

  protected onSearch(value: string): void {
    this.query.set(value);
    this.selectedTapa.set(null);
  }

  protected selectTapa(tapa: Tapa): void {
    this.selectedTapa.set(tapa);
    this.query.set(tapa.name);
  }
}